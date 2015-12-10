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

package net.es.netshell.kernel.users;

/**
 * Created by lomax on 5/16/14.
 */
public class UserProfile {
    /**
     * Representation of a user in the password file.
     * Essentially this is analogous to a single line in /etc/passwd on a UNIX system.
     */

    private String username; // Username, must be a valid UNIX filename.
    private String password; // Encrypted password
    private String privilege; // Privilege, currently either "root" or "user"
    private String name; // Name of user
    private String organization; // Organization of User
    private String email; // Email of User

    public String getName() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getRealName() {
        return name;
    }

    public void setRealName(String name) {
        this.name = name;
    }

    public String getorganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getemail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserProfile(String line) {
        String[] elements = line.split(":");
        if (elements.length != 6) {
            // Incorrect format. Ignore
            return;
        }
        username = elements[0];
        password = elements[1];
        privilege = elements[2];
        name = elements[3];
        organization = elements[4];
        email = elements[5];
    }

    public UserProfile(String username, String password, String privilege, String name, String organization, String email) {
        this.username = username;
        this.password = password;
        this.privilege = privilege;
        this.name = name;
        this.organization = organization;
        this.email = email;
    }

    @Override
    public String toString() {
        String line = "";
        line += username + ":";
        line += password + ":";
        line += privilege + ":";
        line += name + ":";
        line += organization + ":";
        line += email;

        return line;
    }

}
