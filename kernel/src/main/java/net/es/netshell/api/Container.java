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
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by lomax on 5/27/14.
 */
public class Container extends Resource {

    @JsonIgnore
    public final static String CONTAINER_RESOURCE = "container.resource";

    public Container() {
        super();
    }

    public Container(String name) {
        super(name);
    }

    private HashMap<User,List<String>> resourceACLs = null;



    public HashMap<User, List<String>> getResourceACL() {
        return resourceACLs;
    }

    public void setResourceACL(HashMap<User, List<String>> resourceACL) {
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
    final public void save(Resource resource) {

    }

    final Resource load() {
        Resource resource = null;

        return resource;
    }

    public static final void createContainer (User user,String name) throws IOException {
        if (BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return;
        }
        BootStrap.getBootStrap().getDataBase().createCollection(user, name);
        Container container = new Container(CONTAINER_RESOURCE);
        container.save(user,name);
    }

    public static final void createContainer (String name) throws IOException {
        User user = KernelThread.currentKernelThread().getUser();
        Container.createContainer(user,name);
        return;
    }


    @JsonIgnore
    public static final Container getContainer(User user,String name) {
        String collectionName = user.getName() + "_" + name;
        Container container = null;
        if (BootStrap.getBootStrap().getDataBase().collectionExists(user,name)) {
            return null;
        }

        HashMap<String,Object> query = new HashMap<String,Object>();
        query.put("resourceName", name);;
        try {
            List<PersistentObject> containers = PersistentObject.find(user,collectionName, query);
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


}


