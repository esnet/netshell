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
package net.es.netshell.kernel.container;

import net.es.netshell.boot.BootStrap;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by lomax on 5/27/14.
 */
public final class Container {

    private String name;
    private Path path;
    private Container parentContainer;


    public Container (String name, Container parentContainer) {
        this(name);
        this.parentContainer = parentContainer;
    }

    public Container (String name) {

    }

    public Path getPath() {
        return Paths.get(BootStrap.rootPath.toString(),this.name);
    }

    public String getName() {
        return name;
    }

    public String getParentContainer() {
        if (this.parentContainer != null) {
            return parentContainer.getName();
        } else {
            return null;
        }
    }

    public ContainerACL getACL()  {
        throw new RuntimeException("not implemented");
    }

    public void setACL (ContainerACL acl) {

        Method method;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_setACL");

            KernelThread.doSysCall(this, method, acl);

        } catch (Exception e) {
            // Nothing particular to do.
            e.printStackTrace();
        }
    }

    @SysCall(
            name="do_setACL"
    )
    public void do_setACL(ContainerACL acl) throws IOException {
        // Check if user has the right to administrate this container
        ContainerACL realAcl = this.getACL();
        if (realAcl.canAdmin(KernelThread.currentKernelThread().getUser().getName())) {
            // Authorized, save the acl
            acl.store();
        } else {
            throw new SecurityException("not authorized to administrate this container");
        }
    }

    public String getShortName() {
        String[] items = this.name.split("/");
        return items[items.length - 1];
    }

}

