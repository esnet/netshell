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
package net.es.netshell.api;

import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.User;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by lomax on 5/21/14.
 */
public class Resource extends PersistentObject {
    private String resourceName;
    private String description;
    private List<String> parentResources;
    private List<String> childrenResources;
    private String owner;
    private HashMap<User,HashMap<String,ACL>> acls = new HashMap<User,HashMap<String,ACL>>();

    @JsonIgnore
    private String creationStackTrace;

    public Resource() {
        super();
        this.setCreationStackTrace();
    }

    public Resource(String resourceName) {
        this.checkValidResourceName(resourceName);
        this.setCreationStackTrace();
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Resource (Resource object) {
        this.setCreationStackTrace();

        if (object instanceof SecuredResource) {
            // Cannot clone SecuredResource
            throw new SecurityException("operation is not permitted");
        }
        this.resourceName = object.getResourceName();

        if (object.getParentResources() != null) {
            this.setParentResources(new ArrayList<String>());
            this.getParentResources().addAll(object.getParentResources());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! (o instanceof Resource)) return false;

        Resource resource = (Resource) o;
        return resourceName.equals(resource.resourceName);
    }

    @Override
    public int hashCode() {
        if (this.getResourceName() == null) {
            return super.hashCode();
        }
        return resourceName.hashCode();
    }

    @JsonIgnore
    public final String getCreationStackTrace() {
        return this.creationStackTrace;
    }

    @JsonIgnore
    private final void setCreationStackTrace() {
        // this.creationStackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
    }

    @Override
    public String toString() {
        return this.resourceName;
    }

    public final synchronized void setResourceName (String resourceName) {
        this.checkValidResourceName(resourceName);
        if (! (this instanceof SecuredResource) ||
                (this.resourceName == null) ||
                (KernelThread.currentKernelThread().isPrivileged())) {
            this.resourceName = resourceName;
        } else {
            throw new SecurityException("Operation not permitted");
        }
    }

    public final synchronized void setChildrenResources(List<String> childrenResources) {
        if (!(this instanceof SecuredResource) ||
              (this.childrenResources == null) ||
              (KernelThread.currentKernelThread().isPrivileged())) {
            this.childrenResources = childrenResources;
        } else {
            throw new SecurityException("Operation not permitted");
        }
    }

    public final synchronized void setParentResources(List<String> parentResources) {
        if (! (this instanceof SecuredResource) ||
              (this.parentResources == null) ||
              (KernelThread.currentKernelThread().isPrivileged())) {
            this.parentResources = parentResources;
        } else {
            throw new SecurityException("Operation not permitted");
        }
    }

    public final synchronized List<String> getParentResources() {
        if (this.parentResources == null) {
            return null;
        }
        if (!(this instanceof SecuredResource)) {
            return this.parentResources;
        }
        // This is a secured Resource. Clone the list first.
        return new ArrayList<String>(this.parentResources);
    }

    public final synchronized List<String> getChildrenResources() {
        if (this.childrenResources == null) {
            return null;
        }
        if (!(this instanceof SecuredResource)) {
            return this.childrenResources;
        }
        // This is a secured Resource. Clone the list first.
        return new ArrayList<String>(this.childrenResources);
    }

    private void checkValidResourceName(String name) {
        if (ResourceUtils.isValidResourceName(name)) {
            return;
        }
        throw new RuntimeException(name + " is invalid");
    }

    static public List<Resource> findByName(Container container, String name) throws InstantiationException {

        HashMap<String, Object> query = new HashMap<String,Object>();
        query.put("resourceName",name);
        List<PersistentObject> objs = PersistentObject.find(container.getOwner(),container.getResourceName(), query);
        // Translates object types and prunes what is not a Resource.
        ArrayList<Resource> resources = new ArrayList<Resource>();
        for (PersistentObject obj : objs) {
            if (obj instanceof Resource) {
                resources.add((Resource) obj);
            }
        }
        return resources;
    }

    public void save(Container container) throws IOException {
        this.save(container.getOwner(),container.getResourceName());
    }

    public final HashMap<User,HashMap<String,ACL>> getAcls() {
        User user =  KernelThread.currentKernelThread().getUser();
        if (user.isPrivileged() || (user.getName() == this.owner)) {
            // Can get acls
            return this.acls;
        } else {
            throw new SecurityException("not authorized to set acls");
        }
    }

    public final void setAcls(HashMap<User,HashMap<String,ACL>>acls) {
        User user =  KernelThread.currentKernelThread().getUser();
        if (user.isPrivileged() || (user.getName() == this.owner)) {
            // Can set acls
            this.acls = acls;
        } else {
            throw new SecurityException("not authorized to set acls");
        }
    }

    public final void checkAccess(Class aclClass, HashMap<String, Object> accessQuery) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (this.owner == currentUser.getName() || currentUser.isPrivileged()) {
            // Owner has all access rights
            return;
        }
        if ( ! this.acls.containsKey(currentUser)) {
            throw new SecurityException("not authorized");
        }
        if (this.acls.get(currentUser).containsKey(aclClass)) {
            ACL acl = this.acls.get(currentUser).get(aclClass);
            acl.checkAcces(currentUser, this, accessQuery);
            return;
        }
        throw new SecurityException("not authorized");
    }

    public final String getOwner() {
        return owner;
    }

    public final void setOwner(String owner) {
        if (KernelThread.currentKernelThread().getUser().isPrivileged()) {
            // setting the owner of a resource is restricted to privileged threads.
            this.owner = owner;
            return;
        }
        throw new SecurityException("not wuthorized");
    }

    @JsonIgnore
    public synchronized final ResourceACL getResourceACL(User user) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (this.owner == null || this.owner == currentUser.getName() || currentUser.isPrivileged()) {
            // Only the owner can manipulate the ACLs. When the object is being created and not
            // assigned to an owner yet, this operation is allowed.
            if ( ! this.acls.containsKey(user)) {
                return null;
            }
            if (this.acls.get(user).containsKey(ResourceACL.class)) {
                return (ResourceACL) this.acls.get(currentUser).get(ResourceACL.class);
            }
            return null;
        }
        throw new SecurityException("not allowed");
    }

    @JsonIgnore
    public synchronized final void setResourceACL(User user, ResourceACL acl) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (this.owner == null || this.owner == currentUser.getName() || currentUser.isPrivileged()) {
            // Only the owner can manipulate the ACLs. When the object is being created and not
            // assigned to an owner yet, this operation is allowed.
            HashMap <String,ACL> userAcls;
            if (this.acls.containsKey(user)) {
                userAcls = this.acls.get(user);
            } else {
                userAcls = new HashMap<String,ACL>();
                this.acls.put(user, userAcls);
            }
            userAcls.put(ResourceACL.class.getCanonicalName(),acl);
            return;
        }
        throw new SecurityException("not allowed");
    }

    @JsonIgnore
    public synchronized final void removeResourceACL(User user) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (this.owner == null || this.owner == currentUser.getName() || currentUser.isPrivileged()) {
            // Only the owner can manipulate the ACLs. When the object is being created and not
            // assigned to an owner yet, this operation is allowed.
            HashMap<Class, ACL> userAcls;
            if (this.acls.containsKey(user) &&
                    this.acls.get(user).containsKey(ResourceACL.class)) {
                this.acls.get(user).remove(ResourceACL.class);
                return;
            }
            return;
        }
        throw new SecurityException("not allowed");
    }


}
