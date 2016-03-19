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

import com.mongodb.client.FindIterable;
import net.es.netshell.boot.BootStrap;
import net.es.netshell.kernel.exec.KernelThread;
import net.es.netshell.kernel.exec.annotations.SysCall;
import net.es.netshell.kernel.users.User;
import net.es.netshell.kernel.users.Users;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lomax on 5/27/14.
 */
public class Container extends Resource {

    public Container() {
        super();
    }

    public Container(String name) {
        super(name);
    }

    private HashMap<String,List<String>> resourceACLs = null;

    public HashMap<String, List<String>> getResourceACL() {
        return resourceACLs;
    }

    public void setResourceACL(HashMap<String, List<String>> resourceACL) {
        this.resourceACLs = resourceACL;
    }

    public void checkAccess(User currentUser, Resource resource, HashMap<String, Object> accessQuery) {

        if ( this.resourceACLs.containsKey(currentUser)) {
            List<String> acl = this.resourceACLs.get(currentUser);
            if (accessQuery.containsKey(ResourceACL.ACCESS)) {
                String access = (String) accessQuery.get(ResourceACL.ACCESS);
                if (acl.contains(access)) {
                    // Access is granted by ACL
                    return;
                }
            }
        }
        throw new SecurityException("not authorized");
    }

    /**
     * Creates a ResourceAnchor for the given resource.
     * @param resource
     * @return
     */
    @JsonIgnore
    public ResourceAnchor getResourceAnchor(Resource resource) throws InstantiationException {
        Resource stored = Resource.findByName(this, resource.getResourceName());
        if (! stored.getEid().equals(resource.getEid())) {
            throw new SecurityException("incorrect eid");
        }
        ResourceAnchor anchor = new ResourceAnchor(this.getOwner(),
                                                   this.getResourceName(),
                                                   resource.getResourceName(),
                                                   resource.getEid());
        return anchor;
    }

    /**
     * Creates a ResourceAnchor for the given resource.
     * @param resourceName
     * @return
     */
    @JsonIgnore
    public ResourceAnchor getResourceAnchor(String resourceName) throws InstantiationException {
        Resource resource = this.loadResource(resourceName);
        if (resource == null) {
            throw new InstantiationException(resourceName + " does not exist.");
        }
        return this.getResourceAnchor(resource);
    }

    final public void saveResource(Resource resource) throws IOException {
        resource.save(this);
    }

    final public Resource loadResource(String name) throws InstantiationException {
        Resource resource = Resource.findByName(this, name);
        return resource;
    }

    final public void deleteResource(Resource resource) throws InstantiationException, IOException {
        resource.delete(this);
    }

    public void deleteContainer() {
        DataBase db = BootStrap.getBootStrap().getDataBase();
        db.deleteCollection(this.getOwner(), this.getResourceName());
    }


    public static final void createContainer (String user,String name) throws IOException {
        if (BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return;
        }
        BootStrap.getBootStrap().getDataBase().createCollection(user, name);
        Container container = new Container(name);
        container.setOwner(user);
        container.saveResource(container);
    }

    public static final void createContainer (String name) throws IOException {
        User user = KernelThread.currentKernelThread().getUser();
        Container.createContainer(user.getName(), name);
        return;
    }

    @JsonIgnore
    public static final Container getContainer(String name) {
        User user = KernelThread.currentKernelThread().getUser();
        return Container.getContainer(user.getName(),name);
    }

    @JsonIgnore
    public static final Container getContainer(String user,String name) {
        Container container = null;
        if (! BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return null;
        }
        HashMap<String,Object> query = new HashMap<String,Object>();
        query.put("resourceName", name);;
        try {
            List<PersistentObject> containers = PersistentObject.find(user,name, query);
            for (PersistentObject obj : containers) {
                if (obj instanceof Container) {
                    container = (Container)obj;
                    if (container.getResourceName().equals(name)) {
                        return container;
                    }
                }
            }
        } catch (InstantiationException e) {
            return null;
        }
        return container;
    }

    /**
     * Returns the resource referred by the resource anchor.
     * @param anchor
     * @return the Resource referred by the resource anchor.
     * @throws InstantiationException if the resource cannot be loaded from the database.
     * @throws SecurityException if the anchor is invalid
     */
    @JsonIgnore
    public static final Resource fromAnchor(ResourceAnchor anchor) throws InstantiationException {
        Container container = Container.getContainer(anchor.getContainerOwner(),anchor.getContainerName());
        Resource resource = container.loadResource(anchor.getResourceName());
        if (resource != null) {
            if (resource.getEid().equals(anchor.getEid()))  {
                return resource;
            }
            throw new SecurityException("invalid eid");
        }
        return null;
    }
    /**
     * Returns the resource referred by the resource anchor. The anchor is provided as a Map containing the
     * values of the keys containerOwner, containerName, resourceName, eid.
     * @param anchorMap
     * @return the Resource referred by the resource anchor.
     * @throws InstantiationException if the resource cannot be loaded from the database.
     * @throws SecurityException if the anchor is invalid
     */
    @JsonIgnore
    public static final Resource fromAnchor(Map<String,String> anchorMap) throws InstantiationException {
        try {
            String containerOwner = anchorMap.get("containerOwner");
            String containerName = anchorMap.get("containerName");
            String resourceName = anchorMap.get("resourceName");
            String eid = anchorMap.get("eid");
            ResourceAnchor anchor = new ResourceAnchor(containerOwner,containerName,resourceName,eid);
            return Container.fromAnchor(anchor);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Save the state of the container.
     */
    public void save() throws IOException {
        this.saveResource(this);
    }


}


