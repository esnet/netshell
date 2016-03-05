package net.es.netshell.api;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.users.User;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lomax on 3/4/16.
 */
public class ResourceACL extends ACL {
    private HashMap<User,HashMap<String,Boolean>> resourceACLs = null;
    @JsonIgnore
    public static final String READ = "read";
    @JsonIgnore
    public static final String WRITE = "write";
    @JsonIgnore
    public static final String ACCESS = "access";

    public ResourceACL() {
        super();
    }

    public HashMap<User, HashMap<String,Boolean>> getResourceACL() {
        return this.resourceACLs;
    }

    public void setResourceACL(HashMap<User, HashMap<String,Boolean>> resourceACL) {
        this.resourceACLs = resourceACL;
    }

    public HashMap<User, HashMap<String, Boolean>> getResourceACLs() {
        return resourceACLs;
    }

    public void setResourceACLs(HashMap<User, HashMap<String, Boolean>> resourceACLs) {
        this.resourceACLs = resourceACLs;
    }

    public void checkAccess(User currentUser, Resource resource, HashMap<String, Object> accessQuery) {

        if ( this.resourceACLs.containsKey(currentUser)) {
            HashMap<String,Boolean> acl = this.resourceACLs.get(currentUser);
            if (accessQuery.containsKey(ResourceACL.ACCESS)) {
                String access = (String) accessQuery.get(ResourceACL.ACCESS);
                if (acl.containsKey(access)) {
                    if (acl.containsKey(access) && acl.get(access).booleanValue()) {
                        // access is granted
                        return;
                    }
                }
            }
        }
        throw new SecurityException("not authorized");
    }
}
