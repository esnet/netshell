package net.es.netshell.kernel.networking;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;
import net.es.netshell.kernel.acl.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

/**
 * Author: sowmya
 * Date: 4/24/15
 * Time: 3:43 PM
 */
public class NetworkInterfaces {

    private final Logger logger = LoggerFactory.getLogger(NetworkInterfaces.class);

    private static HashMap<String,Boolean> availableInterfaces;

    private static NetworkInterfaces instance;

    private NetworkInterfaces(){
        availableInterfaces = new HashMap<String,Boolean>();
        availableInterfaces.put("eth1.10",false);
        availableInterfaces.put("eth2.10", false);
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
                logger.info("OK to change");
		System.out.println("OK to change \n");

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
