package net.es.netshell.kernel.networking;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;
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
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to change");

                KernelThread.doSysCall(this,
                        method,
                        interfaceName,
                        address);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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


}