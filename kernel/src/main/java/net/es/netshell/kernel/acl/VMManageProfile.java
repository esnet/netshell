package net.es.netshell.kernel.acl;

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
	    else if(maplist[i].equals("user")){
		this.type = USER;
	    }
	    else if(maplist[i].equals("number")){
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

    public static boolean isPrivileged(String username, String map){
	
	VMManageProfile new_user = new VMManageProfile(username, map);
	String[] maplist = map.split(":");
	Integer number=0;
	for(int i=0; i<maplist.length; i++){
	    if(maplist[i].equals("number")){
		number = Integer.parseInt(maplist[i+1]);
	    }
	}
    	if (username.equals(null)) {
            // Not a user
            return false;
	} else if (new_user.getType().equals("user") || new_user.getType().equals("root") && number < (Integer)vm_number.get(username)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}	 
    }

}
