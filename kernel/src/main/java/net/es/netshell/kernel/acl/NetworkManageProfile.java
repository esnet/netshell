package net.es.netshell.kernel.acl;

import net.es.netshell.kernel.acl.UserAccessProfile;

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
	//dont need to check it again
	for(int i = 1; i<= maplist.length; i++){
	    if(maplist[i].equals("ipconfig")){
		this.type = IPCONFIG;
	    }
	    else if(maplist[i].equals("vconfig")){
		this.type = VCONFIG;
	    }
	    else if(maplist[i].equals("interface")){
		this.ethInterface = maplist[i+1];
	    }
	    else if(maplist[i].equals("vlanInterface")){
		this.vlanInterface = maplist[i+1];
	    }
	    /* Assuming that there could be multiple VLAN ids */
	    else if(maplist[i].equals("vlanId")){
		vlan_list = maplist[i+1].split(",");
		this.vlan = new ArrayList<Integer>(vlan_list.length);
		for (int j=0; j<vlan_list.length; j++){
		    this.vlan.add(j,Integer.parseInt(vlan_list[j]));
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

    public static boolean isPrivileged(String username, String map){	

	NetworkManageProfile new_user = new NetworkManageProfile(username, map);
	ArrayList<Integer> new_vlan = new_user.getVlan();
	String new_eth = new_user.getEth();

    	if (username.equals(null)) {
            // Not a user
            return false;
	// check for valid user, vlan list and eth list
	// TODO the new vlan could be a list, so compare lists
	} else if (/*vlan_flag ||*/ ethlist.containsValue(new_eth)) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}
    }
}
