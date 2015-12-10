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
package net.es.netshell.kernel.ovs;

import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: sowmya
 * Date: 10/30/15
 * Time: 1:41 PM
 */
public class Openvswitch {

    private final Logger logger = LoggerFactory.getLogger(Openvswitch.class);

    private static Openvswitch instance;

    private Openvswitch(){

    }

    public static Openvswitch getInstance(){
        if(instance != null){
            return instance;
        }else{
            createInstance();
            return instance;
        }

    }

    private static synchronized void createInstance(){
        if (instance == null){
            instance = new Openvswitch();
        }
    }

    /**
    * Methods to add and remove ovs bridges
    * */
    public boolean ovs_addbridge (String bridgeName) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_addbridge");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to change");
                System.out.println("OK to change \n");

                KernelThread.doSysCall(this,
                        method,
                        bridgeName);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ovs_removebridge (String bridgeName) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_removebridge");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to change");
                System.out.println("OK to change \n");

                KernelThread.doSysCall(this,
                        method,
                        bridgeName);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Methods to add and remove ports from ovs bridges
     * ovs_addport accepts bridge name, portname and tag name. Tag name can b
     * */


    public boolean ovs_addport (String bridgeName, String portName, String tag) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_addport");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to change");
                System.out.println("OK to change \n");

                KernelThread.doSysCall(this,
                        method,
                        bridgeName,portName,tag);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean ovs_removeport (String bridgeName, String portName) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_removeport");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to change");
                System.out.println("OK to change \n");

                KernelThread.doSysCall(this,
                        method,
                        bridgeName,portName);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * System call section
     * */

     @SysCall(
            name="do_addbridge"
    )
    public void do_addbridge(String bridgeName)throws IOException {

        logger.info("Entered method do_addbridge");

        String cmd = "/bin/ovs-vsctl add-br " + bridgeName;
        logger.info(cmd);

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        }
    }

    @SysCall(
            name="do_removebridge"
    )
    public void do_removebridge(String bridgeName)throws IOException {

        logger.info("Entered method do_removebridge");

        String cmd = "/bin/ovs-vsctl del-br " + bridgeName;
        logger.info(cmd);

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        }
    }

    @SysCall(
            name="do_addport"
    )
    public void do_addport(String bridgeName, String portName, String tag)throws IOException {

        logger.info("Entered method do_addport");
        String cmd = "/bin/ovs-vsctl add-port " + bridgeName + " "+portName;
        if(tag != null && !tag.equals("") && !tag.equals(" ")){
            cmd = cmd + " tag=" + tag;
        }
        logger.info(cmd);

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        }
    }

    @SysCall(
            name="do_removeport"
    )
    public void do_removeport(String bridgeName, String portName)throws IOException {

        logger.info("Entered method do_removeport");
        String cmd = "/bin/ovs-vsctl del-port " + bridgeName + " "+portName;
        logger.info(cmd);

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        }
    }

}
