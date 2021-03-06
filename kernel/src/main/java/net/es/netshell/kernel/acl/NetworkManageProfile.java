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

import net.es.netshell.kernel.acl.UserAccessProfile;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Created by amercian on 8/5/15.
 */

public class NetworkManageProfile extends UserAccessProfile {
/**
 * Class to identify the different Network Access Profiles based on Netshell Application Management
 */

    public static final String IPCONFIG = "ipconfig";
    public static final String VCONFIG = "vconfig";

    // List of eth interfaces
    private static SetMultimap<String,String> ethlist = HashMultimap.create();
    // List of vlan ids
    //private static Map<String,ArrayList<Integer>> vlanlist = new HashMap<String, ArrayList<Integer>>();
    private static SetMultimap<String,List<Integer>> vlanlist = HashMultimap.create();

    private String type;
    private String ethInterface;
    private String vlanInterface;
    // list of vlan   
    private List<Integer> vlan = new ArrayList<Integer>();
    //private Integer vlan;
  
     /**
     * Constructor types
     */

    public NetworkManageProfile() {super();}

    public NetworkManageProfile(String username, String map){
	this.username = username;
	this.map = map;
	this.maplist = map.split(":");
	String[] vlan_list;

	//this constructor is used only if maplist[0]="network"
	for(int i = 0; i<maplist.length; i++){
	    if(maplist[i].equals("ipconfig")){
		this.type = IPCONFIG;
	    }
	    else if(maplist[i].equals("vconfig")){
		this.type = VCONFIG;
	    }
	    if(maplist[i].equals("interface")){
		this.ethInterface = maplist[i+1];
	    }
	    if(maplist[i].equals("vlanInterface")){
		this.vlanInterface = maplist[i+1];
	    }
	    /* Assuming that there could be multiple VLAN ids */
	    if(maplist[i].equals("vlan")){
		if(maplist[i+1].contains(",")){
		    vlan_list = maplist[i+1].split(",");
		    //this.vlan = new ArrayList<Integer>(vlan_list.length);
		    for (int j=0; j<vlan_list.length; j++){
		        this.vlan.add(Integer.parseInt(vlan_list[j]));
	       	    }
		} else {
		    this.vlan.add(Integer.parseInt(maplist[i+1]));
		}
		/* Assuming vlan is only one value */
		//this.vlan = Integer.parseInt(maplist[i+1]);
	    }
	}
	this.ethlist.put(username,this.ethInterface);
        this.vlanlist.put(username, this.vlan);
    }

    public String getType(){
	return this.type;
    }

    public List<Integer> getVlan(){
	return this.vlan;
    }

    public String getEth(){
	return this.ethInterface;
    }

    public static boolean isPrivileged(String username, String map) throws IOException {	

	String[] maplist = map.split(":");
	String newEth="", newvlanInterface="";
	Integer vid=0;

	for(int i = 0; i<maplist.length; i++){
	    if(maplist[i].equals("interface")){
		newEth = maplist[i+1];
	    }
	    if(maplist[i].equals("vlanInterface")){
		newvlanInterface = maplist[i+1];
	    }
	    if(maplist[i].equals("vlan")){
		vid = Integer.parseInt(maplist[i+1]);
            }
	}
	
	String existingUserMap = readUserFile(username, "network");
	NetworkManageProfile existingMap = new NetworkManageProfile(username, existingUserMap);
		
	System.out.println("Checking for configurations \n");
    	if (username.equals(null) || existingMap.getMap().equals(null)) {
            // Not a user
       	    System.out.print("Null user \n");
     	    return false;
	// check for valid user, vlan list and eth list
	} else if (existingMap.getType().equals("ipconfig") && existingMap.ethlist.containsValue(newEth)) {
	    System.out.print("True ipconfig case \n");
	    return true;
	} else if (existingMap.getType().equals("vconfig") && existingMap.ethlist.containsValue(newEth) /*&& existingMap.vlanlist.containsValue(vid)*/ && existingMap.getVlan().contains(vid)) {
	    System.out.print("True vconfig case \n");
	    return true;
	}  else {
	    // Any other case
	    System.out.print("Other case \n");
	    return false;
	}
    }
}
