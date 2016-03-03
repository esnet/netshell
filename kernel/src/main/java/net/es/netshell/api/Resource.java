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
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Created by lomax on 5/21/14.
 */
public class Resource extends PersistentObject {
    private String resourceName;
    private String description;
    private List<String> parentResources;
    private List<String> childrenResources;
    @JsonIgnore
    private String creationStackTrace;

    public Resource() {
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
    public String getCreationStackTrace() {
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

    static public List<Resource> findByName(String collectiopn, String name) throws InstantiationException {

        HashMap<String, Object> query = new HashMap<String,Object>();
        query.put("resourceName",name);
        List<PersistentObject> objs = PersistentObject.findFromDatabase(collectiopn,query);
        // Translates object types and prunes what is not a Resource.
        ArrayList<Resource> resources = new ArrayList<Resource>();
        for (PersistentObject obj : objs) {
            if (obj instanceof Resource) {
                resources.add((Resource) obj);
            }
        }
        return resources;
    }



}
