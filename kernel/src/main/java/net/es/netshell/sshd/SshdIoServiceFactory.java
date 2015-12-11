/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
 * of the University of California, through Lawrence Berkeley National
 * Laboratory (subject to receipt of any required approvals from the
 * U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this
 * software, please contact Berkeley Lab's Innovation & Partnerships
 * Office at IPO@lbl.gov.
 *
 * NOTICE.  This Software was developed under funding from the
 * U.S. Department of Energy and the U.S. Government consequently retains
 * certain rights. As such, the U.S. Government has been granted for
 * itself and others acting on its behalf a paid-up, nonexclusive,
 * irrevocable, worldwide license in the Software to reproduce,
 * distribute copies to the public, prepare derivative works, and perform
 * publicly and display publicly, and to permit other to do so.
 */

package net.es.netshell.sshd;

import net.es.netshell.configuration.NetShellConfiguration;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.io.IoAcceptor;
import org.apache.sshd.common.io.IoConnector;
import org.apache.sshd.common.io.IoHandler;
import org.apache.sshd.common.io.IoServiceFactory;
import org.apache.sshd.common.io.mina.MinaAcceptor;
import org.apache.sshd.common.io.mina.MinaConnector;
import org.apache.sshd.common.util.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SSHD IoServiceFactory. Mina's default two IoServiceFactory (mina and nio2) cannot be used due
 * a problem with the thread creation within the ThreadPool they use: the thread pool is configured
 * with no pre-started threads. Threads are then dynamically created when need. This cannot work
 * with NetShell security model since threads are set with a specific user ThreadGroup: once an Thread is
 * created for a user, it will not be able to be changed to another user and therefore creating SecurityExceptions
 * when another user is trying to log into NetShell and re-using threads from the pool.
 *
 * The current implementation creates a ThreadPool with a fixed size and immediately pre start
 * all of them while being privileged. This is not a perfect solution. Using a custom ThreadFactory
 * running with a background thread to create the thread would be better
 */
public class SshdIoServiceFactory implements IoServiceFactory {
    private final Logger logger = LoggerFactory.getLogger(SshdIoServiceFactory.class);
    private final FactoryManager manager;
    private final ExecutorService executor;
    private final IoProcessor<NioSession> ioProcessor;
    private boolean closing = false;
    private boolean closed = false;

    public static final int DEFAULT_NB_WORKER_THREADS = 20;

    public SshdIoServiceFactory(FactoryManager manager) {
        this.manager = manager;
        int nbOfWorkerThreads = NetShellConfiguration.getInstance().getGlobal().getSshNbWorkerThreads();
        if (nbOfWorkerThreads == 0) {
            nbOfWorkerThreads = DEFAULT_NB_WORKER_THREADS;
        }
        this.executor = Executors.newFixedThreadPool( nbOfWorkerThreads);
        ((ThreadPoolExecutor) this.executor).prestartAllCoreThreads();
        // Set a default reject handler
        ((ThreadPoolExecutor) this.executor).setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        this.ioProcessor = new SimpleIoProcessorPool<NioSession>(NioProcessor.class, getNioWorkers());
    }

    public IoConnector createConnector(IoHandler handler) {
        return new MinaConnector(manager, handler, ioProcessor);
    }

    public IoAcceptor createAcceptor(IoHandler handler) {
        return new MinaAcceptor(manager, handler, ioProcessor);
    }

    public CloseFuture close(boolean immediately) {
        try {
            closing = true;
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            closed = true;
        } catch (Exception e) {
            logger.debug("Exception caught while closing executor", e);
        }
        return CloseableUtils.closed();
    }

    public int getNioWorkers() {
        String nioWorkers = manager.getProperties().get(FactoryManager.NIO_WORKERS);
        if (nioWorkers != null && nioWorkers.length() > 0) {
            int nb = Integer.parseInt(nioWorkers);
            if (nb > 0) {
                return nb;
            }
        }
        return FactoryManager.DEFAULT_NIO_WORKERS;
    }

    public boolean isClosing() { return closing; }
    public boolean isClosed() { return closed; }
}
