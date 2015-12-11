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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Created by lomax on 5/21/14.
 */
public abstract class NetworkProvider extends Resource {

    /**
     * Various defined provisioning profiles that network may implement
     */
    public static enum Layer2ProvisioningProfiles {
        BestEffort,           // No bandwidth guaranty.
        StrictBandwidth,      // Guaranted bandwidth but no burst allowed
        BandwidthWithBurst    // Guaranted bandwidth burst ok when possible
    }

    @JsonIgnore
    public static final String NETWORKS_DIR = "networks";

    public Path computePath (String srcNode, String dstNode, DateTime start, DateTime end) throws IOException {
        return null;
    }

    /**
     * Convenience method computing the path start now and ending one minute later.
     * @param srcNodeName
     * @param dstNodeName
     * @return
     */
    public Path computePath (String srcNodeName, String dstNodeName) throws IOException {
        DateTime start = DateTime.now();
        DateTime end = start.plusMinutes(1);
        return this.computePath(srcNodeName,dstNodeName,start,end);
    }

    /**
     * Returns the TopologyProvider of this network
     * @return
     */
    public TopologyProvider getTopologyProvider() {
        return null;
    }

    /**
     * Returns true if this network is capable of provisioning Layer 2 circuits.
     * @return  whether if this network is capable of provisioning Layer 2 circuits.
     */
    public boolean canProvisionLayer2() {
        return false;
    }

    /**
     * Returns true if this network is capable of provisioning and scheduling Layer 2 circuits a
     * @return  whether if this network is capable of provisioning Layer 2 circuits or
     * if the network is not capable of provisioning layer 2 circuits
     */
    public boolean canProvisionScheduledLayer2() {
        return false;
    }

    public boolean supportProfile (Layer2ProvisioningProfiles profile) {
        return false;
    }

    /**
     * Provision the provided layer 2 path providing start and end date.  If start and end date
     * are identical, the path is not bounder. Networks that do not support scheduled provisioning
     * are expected to throw an IOException when start and end dates are not equal. An IOException is
     * throwned when the Network is not capable of provisioning at the provided dates.
     * @param path rovided path to provision
     * @param profile desired profile. An IOException is thrown when the profile is not available
     * @return Provisioned path
     * @throws IOException when provisioning the provided path failed
     */
    public ProvisionedPath  provisionLayer2(Path path,
                                            Layer2ProvisioningProfiles profile ) throws IOException {
        return null;
    }

    /**
     *
     * @param path
     * @throws IOException
     */
    public void deprovisionLayer2(ProvisionedPath path) throws IOException {

    }

    /**
     * Returns the name of the domain as it is known in DNS
     * @return
     */
    public String getDnsName() {
        return null;
    }

}
