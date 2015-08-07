package net.es.netshell.kernel.acl;

/**
 * Created by amercian on 8/5/15.
 */

/**
 * Class to identify the different User Management Profiles based on Netshell Application Management
 */

public class UserManageProfile extends UserAccessProfile {
    public static final String CREATE = "create";
    public static final String DELETE = "delete";

    private String type;

    /**
     * Constructor types
     */
    public UserManageProfile(String username, String map){
	this.username = username;
	this.map = map;
	this.maplist = map.split(":");
	for(int i = 0; i< maplist.length; i++){
	    if(maplist[i].equals("create")){
		this.type = CREATE;
	    }
	    if(maplist[i].equals("delete")){
		this.type = DELETE;
	    }
	}
    }

    public void setType(String type){
	this.type = type;
    }

    public String getType(){
	return this.type;
    }

    public static boolean isPrivileged(String username, String map){
	//User can have privileges to create only
	//User can have privileges to delete only or both
	
	UserManageProfile new_user = new UserManageProfile(username, map);

	if (username.equals(null)) {
            // Not a user
            return false;
	} else if (new_user.getType().equals("create") || new_user.getType().equals("delete")) {
	    return true;
	} else {
	    // Any other case
	    return false;
	}	 
    }

}
