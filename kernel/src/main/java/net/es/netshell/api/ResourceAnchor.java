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


public class ResourceAnchor {
    private String containerOwner;
    private String containerName;
    private String resourceName;
    private String eid;

    public ResourceAnchor() {
    }

    public ResourceAnchor(String containerOwner, String containerName, String resourceName, String eid) {
        this.containerOwner = containerOwner;
        this.containerName = containerName;
        this.resourceName = resourceName;
        this.eid = eid;
    }

    public ResourceAnchor(String containerOwner, String containerName, Resource resource) {
        this.containerOwner = containerOwner;
        this.containerName = containerName;
        this.resourceName = resource.getResourceName();
        this.eid = resource.getEid();
    }

    public ResourceAnchor(Container container, Resource resource) {
        this.containerOwner = container.getOwner();
        this.containerName = container.getResourceName();
        this.resourceName = resource.getResourceName();
        this.eid = resource.getEid();
    }

    public String getContainerOwner() {
        return containerOwner;
    }

    public void setContainerOwner(String containerOwner) {
        this.containerOwner = containerOwner;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }
}
