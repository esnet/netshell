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

import java.io.*;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by amercian on 8/5/15.
 */

public class VMManageProfile extends UserAccessProfile{
/**
 * Class to identify the different VM Management Access Profiles based on Netshell Application Management
 */
    // List of upper bound on number of VMs
    // private static SetMultimap<String,String> vm_number = HashMultimap.create();
    private static Map<String, Integer> vm_number = new HashMap<String, Integer>();

    public static final String ROOT = "root";
    public static final String USER = "user";

    private String type;
    private int number;

    /**
     * Constructor types
     */
    public VMManageProfile(String username, String map){
	this.username = username;
	this.map = map;
	this.maplist = map.split(":");
	for(int i = 0; i< maplist.length; i++){
	    if(maplist[i].equals("root")){
		this.type = ROOT;
	    }
	    if(maplist[i].equals("user")){
		this.type = USER;
	    }
	    if(maplist[i].equals("number")){
		this.number = Integer.parseInt(maplist[i+1]);
	    }
	}
	this.vm_number.put(username,new Integer(number));
    }

    public void setType(String type){
	this.type = type;
    }

    public String getType(){
	return type;
    }

    public static boolean isPrivileged(String username, String map) throws IOException{
	
	VMManageProfile new_user = new VMManageProfile(username, map);
	String[] maplist = map.split(":");
	Integer number=0;
	for(int i=0; i<maplist.length; i++){
	    if(maplist[i].equals("number")){
		number = Integer.parseInt(maplist[i+1]);
	    }
	}

	String existingUserMap = readUserFile(username, "vm");
	VMManageProfile existingMap = new VMManageProfile(username, existingUserMap);

    	if (username.equals(null) || existingMap.getMap().equals(null)) {
            // Not a user
            return false;
	} else if (new_user.getType().equals("user") || new_user.getType().equals("root") && number < (Integer)existingMap.vm_number.get(username)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}	 
    }

}
