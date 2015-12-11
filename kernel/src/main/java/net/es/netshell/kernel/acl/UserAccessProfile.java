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
package net.es.netshell.kernel.acl;

/**
 * Created by amercian on 7/7/15.
 */
import net.es.netshell.api.*;
import net.es.netshell.configuration.NetShellConfiguration;
import net.es.netshell.kernel.security.FileACL;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to identify the different User Access Profiles based on Netshell Application Management
 * Currently support Network, User and VM Management profiles
 */

public class UserAccessProfile {
    public static final String NETWORK = "network";
    public static final String USER = "user";
    public static final String VM = "vm";

    public String username;
    public String map;
    public String[] maplist;

    /**
     * Constructor types
     */
    public UserAccessProfile() {    }

    public UserAccessProfile(String username, String map) {
	this.username = username;
	this.map = map; 
        this.maplist = map.split(":");//do not use colon. 
	// the map that is entered with colons
    }

    public UserAccessProfile(String line) {
        String[] elements = line.split("-");
        if (elements.length != 2) {
            // Incorrect format. Ignore
            return;
        }
        username = elements[0];
	map = elements[1];
    }

    @Override
    public String toString() {
        String line = "";
        line += username + "-";
	line += map;
        return line;
    }

    public void setMap(String map){
	this.map = map;
    }

    public String getMap(){
	return this.map;
    }

    public void setName(String username){
	this.username = username;
    }

    public String getName(){
	return this.username;
    }

    public static String readUserFile(String username, String access) throws IOException {
       	Path aclFilePath = FileUtils.toRealPath(String.format("/etc/acl/%s",access));
        File aclFile = new File(aclFilePath.toString());
        aclFile.getParentFile().mkdirs();
        if (!aclFile.exists()) {
            // File does not exist yet, create it.
            if (!aclFile.createNewFile()) {
                // File could not be created, return a RuntimeError
                throw new RuntimeException("Cannot create " + aclFilePath.toString());
            }
        }
        BufferedReader reader = new BufferedReader(new FileReader(aclFile));
        String line = null;

        while ((line = reader.readLine()) != null) {
            UserAccessProfile p = new UserAccessProfile(line);
            if (p.getName().equals(username)) {
                return p.getMap(); 
            }
            else {
		return null;
            }
        }
	return null;
    }
}
