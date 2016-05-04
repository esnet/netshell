/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2016, The Regents
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
 *
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

    /**
     * Creates a ResourceAnchor for the given resource.
     * @param resource
     * @return
     */
    @JsonIgnore
    public ResourceAnchor getResourceAnchor(Resource resource) throws InstantiationException {
        Resource stored = Resource.findByName(this, resource.getResourceName());

        ResourceAnchor anchor = new ResourceAnchor(this.getOwner(),
                                                   this.getResourceName(),
                                                   resource.getResourceName());
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
        this.save(this);
    }

    final public void saveResources(List<Resource> resources) throws IOException {
        for (Resource resource : resources) {
            resource.save(this);
        }
        this.save(this);
    }

    final public Resource loadResource(String name) throws InstantiationException {
            Resource resource = Resource.findByName(this, name);
            return resource;
    }

    final public List<Resource> loadResources(Map<String,Object> query)
            throws InstantiationException, IOException {

        List<Resource> resources = Resource.findResources(this, query);
        return resources;
    }

    final public void deleteResource(Resource resource) throws InstantiationException, IOException {
        resource.delete(this);
    }

    final public void deleteResource(String resourceName) throws InstantiationException {
        Resource.delete(this.getOwner(),this.getResourceName(),resourceName);
    }

    public void deleteContainer() {
        DataBase db = BootStrap.getBootStrap().getDataBase();
        db.deleteCollection(this.getOwner(), this.getResourceName());
    }


    public static final Container createContainer (String user,
                                                   String name,
                                                   Class containerClass)
            throws IOException,IllegalAccessException, InstantiationException {

        if (BootStrap.getBootStrap().getDataBase().collectionExists(user, name)) {
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
        String userName;
        if (user == null) {
            userName = "admin";
        } else {
            userName = user.getName();
        }
        return Container.createContainer(userName, name);
    }

    @JsonIgnore
    public static final Container getContainer(String name) throws IOException {
        User user = KernelThread.currentKernelThread().getUser();
        String userName;
        if (user == null) {
            userName = "admin";
        } else {
            userName = user.getName();
        }
        return Container.getContainer(userName, name);
    }

    @JsonIgnore
    public static final Container getContainer(String user,String name) throws IOException {
        // First check the Resource cach
        Container container = null;
        try {
            container = (Container) Resource.findByName(user, name, name);
        } catch (InstantiationException e) {
            // Could not instanciate container, assume it was not created
        }
        if (container == null) {
            Container.createContainer(user,name);
            try {
                container = (Container) Resource.findByName(user, name, name);
            } catch (InstantiationException e) {
                throw new IOException("cannot create container");
            }
        }
        if (container == null) {
            throw new IOException("Cannot find container");
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
    public static final Resource fromAnchor(ResourceAnchor anchor) throws InstantiationException, IOException {
        Container container = Container.getContainer(anchor.getContainerOwner(), anchor.getContainerName());
        Resource resource = container.loadResource(anchor.getResourceName());
        return resource;
    }
    /**
     * Returns the resource referred by the resource anchor. The anchor is provided as a Map containing the
     * values of the keys containerOwner, containerName, resourceName.
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
            ResourceAnchor anchor = new ResourceAnchor(containerOwner,containerName,resourceName);
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

    public void insert(Container container, Map<String,Object> query) throws IOException, InstantiationException {
        List<Resource> resources = container.loadResources(query);
        this.saveResources(resources);
    }
}


