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
    private String destination;
    private String maURI;
    private String maKey;
    private String maUser;
    private String source;

    public OutputStream getOutputStream() {

        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    private OutputStream outputStream;

    public String getSource() {

        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    public String getMaKey() {

        return maKey;
    }

    public String getMaUser() {

        return maUser;
    }

    public String getDestination() {

        return destination;
    }

    public void setDestination(String destination) {

        this.destination = destination;
    }

    public String getMaURI() {

        return maURI;
    }

    public void setMaURI(String maURI) {

        this.maURI = maURI;
    }

    private Bwctl(String source, String destination){
        this.source=source;
        this.destination = destination;

    }

    /**
     * Method to run a standard bwctl test with default tool=iperf3, test_duration=10s
     * */
    public boolean runSingleTest() {
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
                writeToOutputStream( "OK to run bwctl test \n");

                KernelThread.doSysCall(this,
                        method,
                        source, destination);
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            writeToOutputStream(e.getMessage());
            return false;
        }
        catch (Exception e) {
            writeToOutputStream(e.getMessage());
            return false;
        }
    }

    /**
     * System call section
     * */

    @SysCall(
            name="do_runbwctl"
    )
    public void do_runbwctl(String source, String destination, OutputStream out) throws IOException {
 logger.info("Entered method do_runbwctl");

        //TODO: Change default tool to iperf3.
        String cmd = "bwctl -s " + source + "  -c " + destination +" -T iperf -t 10 -a 1 --parsable --verbose ";
        writeToOutputStream("Running command:\n"+cmd);
        logger.info(cmd);


        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            writeToOutputStream(e.getMessage());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s = br.readLine()) != null) {
            writeToOutputStream(s);
        }
    }



    private void writeToOutputStream(String message)  {

        byte[] byteMessage = message.getBytes();
        try {
            outputStream.write(byteMessage);
        } catch (IOException e) {
            logger.info("Error writing to outputstream"+e.getMessage());
            e.printStackTrace(); //default to standard out
        }

    }
    


}


