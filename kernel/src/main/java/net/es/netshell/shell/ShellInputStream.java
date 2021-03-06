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

package net.es.netshell.shell;

import jline.console.ConsoleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lomax on 2/20/14.
 */
public class ShellInputStream extends InputStream {

    private InputStream in = null;
    private OutputStream echoOut = null;
    private boolean last = false;
    private boolean doEcho = false;
    private boolean eofHack = true;
    private ConsoleReader consoleReader = null;

    public boolean isDoCompletion() {
        return doCompletion;
    }

    public void setDoCompletion(boolean doCompletion) {
        this.doCompletion = doCompletion;
    }

    private boolean doCompletion = true;

    public boolean isDoEcho() {
        return doEcho;
    }

    public void setDoEcho(boolean doEcho) {
        this.doEcho = doEcho;
    }

    public OutputStream getEchoOut() {
        return echoOut;
    }

    public void setEchoOut(OutputStream echoOut) {
        this.echoOut = echoOut;
    }

    public boolean isEofHack() {
        return eofHack;
    }

    public void setEofHack(boolean eofHack) {
        this.eofHack = eofHack;
    }

    private final Logger logger = LoggerFactory.getLogger(ShellInputStream.class);

    public ShellInputStream(InputStream in, OutputStream echoOut) {
        this.in = in;
        this.echoOut = echoOut;
    }

    public ShellInputStream(InputStream in,
                            ConsoleReader     consoleReader) {
        this.in = in;
        this.consoleReader = consoleReader;
    }

    public int read() throws IOException {
        if (this.last) {
            this.last = false;
            return -1;
        }
        int c = this.in.read();
        logger.debug("c = {}", c);
        if (this.doEcho && this.echoOut != null) {
            this.echoOut.write(c);
            this.echoOut.flush();
        }
        switch (c) {
            case 13:
                if (this.doEcho && this.echoOut != null) {
                    this.echoOut.write('\n');
                    this.echoOut.flush();
                }
                if (eofHack) {
                    this.last = true;
                }
                return 10;
        }
        return c;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int index=0;
        while (true) {
            int c = this.in.read();
            if (c==13) c=10;
            if (c==10) {
                b[index++] = (byte) c;
                return index;
            }
            if (index < b.length) {
                b[index++] = (byte) c;
            } else {
                return index;
            }
        }
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        String prompt;
        String line;
        if (this.doCompletion) {
            prompt = consoleReader.getPrompt();
            line = consoleReader.readLine("\000\000\000\000");
        } else {
            prompt = consoleReader.getPrompt();
            line = consoleReader.readLine("\000\000\000\000");
        }
        if (line == null) return -1;
        for (int i=0; i < line.length(); ++i) {
             b[off+i] = (byte) line.charAt(i);
        }
        b[line.length()] = 10;
        if (this.doCompletion) {
            consoleReader.setPrompt(prompt);
        } else {
            consoleReader.setPrompt(prompt);
        }
        return line.length() + 1;
    }

    @Override
    public long skip(long n) throws IOException {
        logger.debug("skip {}", n);
        return this.in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        logger.debug("readlimit");
        this.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        logger.debug("reset");
        this.in.reset();
    }

    @Override
    public boolean markSupported() {
        logger.debug("markSupported");
        return this.in.markSupported();
    }

    public InputStream getSourceInputStream() {
        return this.in;
    }
}
