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

package net.es.netshell.kernel.networking;

import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * Author: sowmya
 * Date: 4/24/15
 * Time: 3:43 PM
 */
public class NetworkInterfaces {

    private final Logger logger = LoggerFactory.getLogger(NetworkInterfaces.class);

    private static NetworkInterfaces instance;

    private NetworkInterfaces(){

    }

    public static NetworkInterfaces getInstance(){
        if(instance != null){
            return instance;
        }else{
            createInstance();
            return instance;
        }

    }

    private static synchronized void createInstance(){
        if (instance == null){
            instance = new NetworkInterfaces();
        }
    }

    public boolean ipconfig (String interfaceName, OutputStream out, OutputStream err) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_iplist");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName) || currentUserAccess.isAccessPrivileged(currentUserName, String.format("network:ipconfig:interface:%s",interfaceName))) {
                logger.info("Authorized to execute ipconfig  \n");
                System.out.println("Authorized to execute ipconfig  \n");

                KernelThread.doSysCall(this,
                        method,
                        interfaceName, out, err);
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

    public boolean ipconfig (String interfaceName, InetAddress address) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_ipconfig");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
	    // Access per Application
	    UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName) || currentUserAccess.isAccessPrivileged(currentUserName, String.format("network:ipconfig:interface:%s",interfaceName))) {
                logger.info("Authorized to execute ipconfig  \n");
		        System.out.println("Authorized to execute ipconfig  \n");

                KernelThread.doSysCall(this,
                        method,
                        interfaceName,
                        address);
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
        //return false;
    }

    public boolean ipconfig (String interfaceName, int mtu) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_mtuconfig");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName) || currentUserAccess.isAccessPrivileged(currentUserName, String.format("network:ipconfig:interface:%s",interfaceName))) {
                logger.info("OK to change");
                System.out.println("OK to change \n");

                KernelThread.doSysCall(this,
                        method,
                        interfaceName,
                        mtu);
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
        //return false;
    }

    public boolean vconfig(String interfaceName, int vid) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_vconfig");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();

            String vlanInterfaceName = interfaceName+"."+vid;

            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
	    //Privilege per application
	    UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName) || currentUserAccess.isAccessPrivileged(currentUserName, String.format("network:vconfig:interface:%s:vlan:%s",interfaceName,Integer.toString(vid)))) {
                logger.info("OK to change");

		System.out.println("OK to change \n");
                KernelThread.doSysCall(this,
                        method,
                        interfaceName, vlanInterfaceName,
                        vid);
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
        //return false;
    }


    @SysCall(
            name="do_ipconfig"
    )
    public void do_ipconfig(String interfaceName, InetAddress address)throws IOException {
        logger.info("do_ipconfig entry");
        String ipAddr = address.getHostAddress();

        String cmd = "/sbin/ip addr add "+ipAddr+" dev "+interfaceName;
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
            name="do_mtuconfig"
    )
    public void do_mtuconfig(String interfaceName, int mtu)throws IOException {

        logger.info("Entered method do_mtuconfig");

        String cmd = "/sbin/ip link set mtu " + mtu + " dev " + interfaceName;
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
            name="do_iplist"
    )
    public void do_iplist(String interfaceName, OutputStream out, OutputStream err)throws IOException {
        logger.info("do_ipconfig entry");

        String cmd = "/sbin/ip addr list "+interfaceName;
        logger.info(cmd);

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        PrintStream resultStream = new PrintStream(out);
        //StringBuffer resultBuffer = new StringBuffer();
        while ((s = br.readLine()) != null) {
            System.out.println(s);

            resultStream.print(s);
            //resultBuffer.append(s);
        }





    }


    @SysCall(
            name="do_vconfig"
    )
    public void do_vconfig(String interfaceName, String vlanInterfaceName, int vlanId)throws IOException {
        logger.info("do_vconfig entry");

        String cmd = "/sbin/ip link add link "+interfaceName+" name "+vlanInterfaceName+ " type vlan id "+vlanId;
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
