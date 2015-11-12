package net.es.netshell.kernel.perfsonar;

import net.es.netshell.kernel.acl.UserAccess;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
/**
 * Author: sowmya
 * Date: 10/30/15
 * Time: 1:41 PM
 */
public class Bwctl {

    private final Logger logger = LoggerFactory.getLogger(Bwctl.class);

    private static Bwctl instance;

    private Bwctl(){

    }

    public static Bwctl getInstance(){
        if(instance != null){
            return instance;
        }else{
            createInstance();
            return instance;
        }

    }

    private static synchronized void createInstance(){
        if (instance == null){
            instance = new Bwctl();
        }
    }

    /**
     * Method to run a standard bwctl test with default tool=iperf3, test_duration=10s
     * */
    public boolean runBwctlTest (String source, String destination, OutputStream out, OutputStream err) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_runbwctl");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to run test");
                System.out.println("OK to run bwctl test \n");

                KernelThread.doSysCall(this,
                        method,
                        source, destination,out, err);
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


    public boolean runPersistentBwctlTest (String source, String destination, String user, String key, String dburi,OutputStream out, OutputStream err) {
        Method method = null;
        try {
            method = KernelThread.getSysCallMethod(this.getClass(), "do_runpersistentbwctl");

            KernelThread kt = KernelThread.currentKernelThread();
            String currentUserName = kt.getUser().getName();



            logger.info("current user {}", currentUserName);
            Users currentUsers = Users.getUsers();
            // Access per Application
            UserAccess currentUserAccess = UserAccess.getUsers();
            if (currentUsers.isPrivileged(currentUserName)) {
                logger.info("OK to run test");
                System.out.println("OK to run bwctl test \n");

                KernelThread.doSysCall(this,
                        method,
                        source, destination,user,key,dburi,out,err);
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
            name="do_runbwctl"
    )
    public void do_runbwctl(String source, String destination, OutputStream out, OutputStream err) throws IOException {

        logger.info("Entered method do_runbwctl");

        PrintStream resultStream = new PrintStream(out);

        //TODO: Change default tool to iperf3.
        String cmd = "bwctl -s " + source + "  -c " + destination +" -T iperf3 -t 10 -a 1 --parsable --verbose ";
        logger.info(cmd);
        resultStream.println(cmd);


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
            resultStream.println(s);
        }
    }

    @SysCall(
            name="do_runpersistentbwctl"
    )
    public void do_runpersistentbwctl(String source, String destination, String user, String key, String dburi,OutputStream out, OutputStream err) throws IOException {

        logger.info("Entered method do_runpersistentbwctl");

        PrintStream resultStream = new PrintStream(out);

        String[] cmd = {"/bin/sh", "-c",
                       "bwctl -s " + source + "  -c " + destination +" -T iperf3 -t 10 -a 1 --parsable --verbose" +
                     " |& " +
                     "esmond-ps-pipe --user "+user +" --key "+key+ " -U "+dburi};
        String cmdString = Arrays.toString(cmd);
        logger.info(cmdString);
        resultStream.println(cmdString);

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
            resultStream.println(s);
        }
    }



}


