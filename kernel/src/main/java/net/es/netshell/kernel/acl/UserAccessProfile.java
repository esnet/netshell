package net.es.netshell.kernel.acl;

/**
 * Created by amercian on 7/7/15.
 */

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
	return map;
    }

    public void setName(String username){
	this.username = username;
    }

    public String getName(){
	return username;
    }
}
