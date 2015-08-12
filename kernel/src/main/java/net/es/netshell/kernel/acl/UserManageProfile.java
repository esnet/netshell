package net.es.netshell.kernel.acl;

/**
 * Created by amercian on 8/5/15.
 */
import java.io.*;

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
    public UserManageProfile() {super();}

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

    public static boolean isPrivileged(String username, String map) throws IOException{
	//User can have privileges to create only
	//User can have privileges to delete only or both
	String newtype = null;
	String[] maplist = map.split(":");
	for(int i = 0; i< maplist.length; i++){
	    if(maplist[i].equals("create")){
		newtype = CREATE;
	    }
	    if(maplist[i].equals("delete")){
		newtype = DELETE;
	    }
	}
	
	String existingUserMap = readUserFile(username, "user");
	UserManageProfile existingMap = new UserManageProfile(username, existingUserMap);

	if (username.equals(null) || existingMap.getMap().equals(null) || newtype.equals(null)) {
            // Not a user
            System.out.println("Null user");
            return false;
	} else if (existingMap.getType().equals(newtype)) {
            System.out.println("True case");
	    return true;
	} else {
	    // Any other case
            System.out.println("Any other case");
	    return false;
	}	 
    }

}
