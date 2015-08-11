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
    private static Map<String,ArrayList<Integer>> vlanlist = new HashMap<String, ArrayList<Integer>>();

    private String type;
    private String ethInterface;
    private String vlanInterface;
    private ArrayList<Integer> vlan;
 
    // list of vlan   
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
	    if(maplist[i].equals("vconfig")){
		this.type = VCONFIG;
	    }
	    if(maplist[i].equals("interface")){
		this.ethInterface = maplist[i+1];
	    }
	    if(maplist[i].equals("vlanInterface")){
		this.vlanInterface = maplist[i+1];
	    }
	    /* Assuming that there could be multiple VLAN ids */
	    if(maplist[i].equals("vlanId")){
		if(maplist[i+1].contains(",")){
		    vlan_list = maplist[i+1].split(",");
		    this.vlan = new ArrayList<Integer>(vlan_list.length);
		    for (int j=0; j<vlan_list.length; j++){
		        this.vlan.add(j,Integer.parseInt(vlan_list[j]));
	       	    }
		} else {
		    this.vlan.add(Integer.parseInt(maplist[i+1]));
		}
	    }
	}
	this.ethlist.put(username,this.ethInterface);
        this.vlanlist.put(username, this.vlan);
    }

    public String getType(){
	return this.type;
    }

    public ArrayList<Integer> getVlan(){
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
	    if(maplist[i].equals("vlanId")){
		vid = Integer.parseInt(maplist[i+1]);
            }
	}
	
	String existingUserMap = readUserFile(username, "network");
	NetworkManageProfile existingMap = new NetworkManageProfile(username, existingUserMap);
		
    	if (username.equals(null) || existingMap.getMap().equals(null)) {
            // Not a user
            return false;
	// check for valid user, vlan list and eth list
	} else if (existingMap.ethlist.containsValue(newEth) && existingMap.vlanlist.containsValue(vid)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}
    }
}
