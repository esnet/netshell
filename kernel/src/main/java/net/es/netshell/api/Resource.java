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
import net.es.netshell.kernel.users.User;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.awt.image.Kernel;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Base Resource Class that all NetShell resources must extend. I
 */
@ResourceType(
        type=ResourceTypes.RESOURCE
)
public class Resource extends PersistentObject {

    private String description;
    private ResourceAnchor parentResourceAnchor;
    private Map<String, ResourceAnchor> childrenResourceAnchors;
    private String owner;
    private  Map<User,Map<String,ACL>> acls = new HashMap<User,Map<String,ACL>>();
    private String resourceType;

    @JsonIgnore
    public String creationStackTrace;
    @JsonIgnore
    private static ResourceCache cache = new ResourceCache();

    public Resource() {
        super();
        // this.setCreationStackTrace();
    }

    public Resource(String resourceName) {
        super(resourceName);
        this.setCreationStackTrace();
    }

    public Resource(String resourceName,String resourceClassName) {
        super(resourceName);
        this.setCreationStackTrace();
        this.setResourceClassName(resourceClassName);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceType() {
        if (this.resourceType == null) {
            // Retrieve resource type from annotation.
            ResourceType type = this.getClass().getAnnotation(ResourceType.class);
            if (type != null) {
                this.resourceType = type.type();
            }
        }
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Resource (Resource object) {
        super (object.getResourceName());
        this.setCreationStackTrace();
        if (object.getParentResourceAnchor() != null) {
            this.setParentResourceAnchor(object.getParentResourceAnchor());
        }
    }


    @JsonIgnore
    public final String getCreationStackTrace() {
        return this.creationStackTrace;
    }

    @JsonIgnore
    public static ResourceCache getCache() {
        if (!KernelThread.currentKernelThread().isPrivileged()) {
            throw new SecurityException("not authorized to get Resource cache");
        }
        return Resource.cache;
    }

    @JsonIgnore
    private final void setCreationStackTrace() {
        // this.creationStackTrace = Arrays.toString(Thread.currentThread().getStackTrace());
    }

    public final synchronized void setChildrenResourceAnchors(Map<String, ResourceAnchor> childrenResourceAnchors) {
        this.childrenResourceAnchors = childrenResourceAnchors;
    }

    public final synchronized void setParentResourceAnchor(ResourceAnchor parentResourceAnchor) {
        this.parentResourceAnchor = parentResourceAnchor;
    }

    public final synchronized ResourceAnchor getParentResourceAnchor() {
        return this.parentResourceAnchor;
    }

    public final synchronized Map<String, ResourceAnchor> getChildrenResourceAnchors() {
        return this.childrenResourceAnchors;
    }

    static public Resource findByName(Container container, String name) throws InstantiationException {
        return Resource.findByName(container.getOwner(), container.getResourceName(), name, null);
    }

    static public Resource findByName(Container container, String name, Class resourceClass) throws InstantiationException {
        return Resource.findByName(container.getOwner(), container.getResourceName(), name, resourceClass);
    }

    static public Resource findByName(String containerOwner,
                                      String containerName,
                                      String name) throws InstantiationException {
        return Resource.findByName(containerOwner,containerName,name,null);

    }
    static public Resource findByName(String containerOwner,
                                      String containerName,
                                      String name,
                                      Class resourceClass) throws InstantiationException {

        Resource obj = Resource.cache.getCachedObject(containerOwner,containerName, name);
        if (obj != null) {

            if (resourceClass != null) {
                if (obj.getClass().equals(resourceClass)) {
                    return obj;
                }
            } else {
                return obj;
            }
        }

        // Not in the cache. Must load it from the database
        Map<String, Object> query = new HashMap<String,Object>();
        query.put("resourceName",name);
        List<PersistentObject> objs = PersistentObject.find(containerOwner,containerName, query,resourceClass);
        // Translates object types and prunes what is not a Resource.
        ArrayList<Resource> resources = new ArrayList<Resource>();
        if (objs.size() > 0) {
            Resource resource = (Resource) objs.get(0);
            if (objs.get(0) instanceof Resource) {
                Resource.cache.cacheObject(containerOwner,containerName,resource);
                return resource;
            }
        }
        return null;
    }

    static public List<Resource> findResources(Container container, Map<String,Object> query)
            throws InstantiationException, IOException {
        // Since the query is complex, it needs to be done by the database. This means that the cache
        // must first be sync'ed.
        Resource.cache.sync();
        List<PersistentObject> objs = PersistentObject.find(container.getOwner(), container.getResourceName(), query);
        // Translates object types and prunes what is not a Resource.
        ArrayList<Resource> resources = new ArrayList<Resource>();
        for (PersistentObject obj : objs) {
            resources.add((Resource) obj);
        }
        return resources;
    }

    public void save(Container container) throws IOException {
        Resource.cache.cacheObject(container,this);
    }

    public void delete(Container container) throws IOException {
        try {
            super.delete(container.getOwner(),container.getResourceName());
            Resource.cache.removeCachedObject(container,this.getResourceName());
        } catch (InstantiationException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static void delete(Container container, String resourceName) throws IOException {
        try {
            PersistentObject.delete(container.getOwner(),container.getResourceName(),resourceName);
            Resource.cache.removeCachedObject(container, resourceName);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public final Map<User,Map<String,ACL>> getAcls() {
        User user =  KernelThread.currentKernelThread().getUser();
        if (KernelThread.currentKernelThread().isPrivileged() || (user.getName() == this.owner)) {
            // Can get acls
            return this.acls;
        } else {
            throw new SecurityException("not authorized to set acls");
        }
    }

    public final void setAcls(Map<User,Map<String,ACL>>acls) {
        User user =  KernelThread.currentKernelThread().getUser();
        if (KernelThread.currentKernelThread().isPrivileged() || (user.getName() == this.owner)) {
            // Can set acls
            this.acls = acls;
        } else {
            throw new SecurityException("not authorized to set acls");
        }
    }

    public final void checkAccess(Class aclClass, Map<String, Object> accessQuery) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (KernelThread.currentKernelThread().isPrivileged() || this.owner == currentUser.getName()) {
            // Owner has all access rights
            return;
        }
        if ( ! this.acls.containsKey(currentUser)) {
            throw new SecurityException("check access - not authorized");
        }
        if (this.acls.get(currentUser).containsKey(aclClass)) {
            ACL acl = this.acls.get(currentUser).get(aclClass);
            acl.checkAcces(currentUser, this, accessQuery);
            return;
        }
        throw new SecurityException("check access - not authorized");
    }

    public final String getOwner() {
        return owner;
    }

    public final void setOwner(String owner) {
        if (KernelThread.currentKernelThread().isPrivileged()) {
            // setting the owner of a resource is restricted to privileged threads.
            this.owner = owner;
            return;
        }
        throw new SecurityException("set owner - not authorized");
    }

    @JsonIgnore
    public synchronized final ResourceACL getResourceACL(User user) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (KernelThread.currentKernelThread().isPrivileged() || this.owner == null || this.owner == currentUser.getName()) {
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
        throw new SecurityException("get resource - not allowed");
    }

    @JsonIgnore
    public synchronized final void setResourceACL(User user, ResourceACL acl) {
        User currentUser = KernelThread.currentKernelThread().getUser();
        if (KernelThread.currentKernelThread().isPrivileged() || this.owner == null || this.owner == currentUser.getName()) {
            // Only the owner can manipulate the ACLs. When the object is being created and not
            // assigned to an owner yet, this operation is allowed.
            Map <String,ACL> userAcls;
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
        if (KernelThread.currentKernelThread().isPrivileged() || this.owner == null || this.owner == currentUser.getName()) {
            // Only the owner can manipulate the ACLs. When the object is being created and not
            // assigned to an owner yet, this operation is allowed.
            Map<Class, ACL> userAcls;
            if (this.acls.containsKey(user) &&
                    this.acls.get(user).containsKey(ResourceACL.class)) {
                this.acls.get(user).remove(ResourceACL.class);
                return;
            }
            return;
        }
        throw new SecurityException("not allowed");
    }


    /**
     * Returns the resource, if any, associated with the ResourceAnchor.
     * @param anchor
     * @return
     */
    public final static Resource toResource(ResourceAnchor anchor) throws IOException {
        Container container = Container.getContainer(anchor.getContainerOwner(),anchor.getContainerName());
        if (container == null) {
            return null;
        }
        try {
            Resource resource = container.loadResource(anchor.getResourceName());
            return resource;
        } catch (InstantiationException e) {
            return null;
        }
    }
}
