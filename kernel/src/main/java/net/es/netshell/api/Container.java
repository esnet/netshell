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
@ResourceType(
        type=ResourceTypes.CONTAINER
)
public class Container extends Resource {

    public Container() {
        super();
    }

    public Container(String name) {
        super(name);
    }

    private HashMap<String,String> resources = new HashMap<String,String>();


    public HashMap<String, String> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, String> resources) {
        this.resources = resources;
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
        this.resources.put(resource.getResourceName(),resource.getResourceClassName());
        resource.save(this);
        this.save(this);
    }

    final public Resource loadResource(String name) throws InstantiationException {
        if (this.resources.containsKey(name)) {
            Resource resource = Resource.findByName(this, name);
            return resource;
        }
        return null;
    }

    final public List<Resource> loadResources(HashMap<String,Object> query)
            throws InstantiationException, IOException {

        List<Resource> resources = Resource.findResources(this, query);
        return resources;
    }

    final public void deleteResource(Resource resource) throws InstantiationException, IOException {
        resource.delete(this);
    }

    public void deleteContainer() {
        DataBase db = BootStrap.getBootStrap().getDataBase();
        db.deleteCollection(this.getOwner(), this.getResourceName());
    }


    public static final Container createContainer (String user,
                                                   String name,
                                                   Class containerClass)
            throws IOException,IllegalAccessException, InstantiationException {

        if (BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return null;
        }
        BootStrap.getBootStrap().getDataBase().createCollection(user, name);
        if (containerClass == null) {
            containerClass = Container.class;
        }
        Container container = (Container) containerClass.newInstance();
        container.setResourceName(name);
        container.setOwner(user);
        container.saveResource(container);
        return container;
    }
    public static final Container createContainer (String user,String name) throws IOException {
        if (BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return null;
        }
        BootStrap.getBootStrap().getDataBase().createCollection(user, name);
        Container container = new Container(name);
        container.setOwner(user);
        container.saveResource(container);
        return container;
    }

    public static final Container createContainer (String name) throws IOException {
        User user = KernelThread.currentKernelThread().getUser();
        return Container.createContainer(user.getName(), name);
    }

    @JsonIgnore
    public static final Container getContainer(String name) {
        User user = KernelThread.currentKernelThread().getUser();
        return Container.getContainer(user.getName(),name);
    }

    @JsonIgnore
    public static final Container getContainer(String user,String name) {
        // First check the Resource cach
        Container container = null;
        try {
            container = (Container) Resource.findByName(user, name, name);
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


