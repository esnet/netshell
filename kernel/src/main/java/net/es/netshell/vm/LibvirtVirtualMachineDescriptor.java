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
package net.es.netshell.vm;

/** 
 * created by amercian on 06/10/2015
 */

import net.es.netshell.api.PersistentObject;

public class LibvirtVirtualMachineDescriptor extends PersistentObject {
    private int cpu;
    private long memory;
    private String name, ethName, ip, gateway, mac, netmask, bridgeName, bridgeIP;

    public LibvirtVirtualMachineDescriptor() {

    }
    /**
     * Constructor that initializes all the input parameters of specific virtual domain
     * @param name
     * @param memory
     * @param cpu
     * @param ethName
     * @param ip
     * @param gateway
     * @param mac
`````* @param netmask
     * @param bridgeName
     * @param bridgeIP
     */ 
    public LibvirtVirtualMachineDescriptor (String name, long memory, int cpu, String ethName, String ip, String gateway, String mac, String netmask, String bridgeName, String bridgeIP) {
        this.name = name;
        this.memory = memory;
        this.cpu = cpu;
	this.ethName = ethName;
        this.ip = ip;
        this.gateway = gateway;
        this.mac = mac;
	this.netmask = netmask;
	this.bridgeName = bridgeName;
	this.bridgeIP = bridgeIP;
    }

    /**
     * Function to set the name of the domain, useful for lookup purposes
     * @param name Name of Domain
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the Domain name
     * @return the name of domain
     */
    public String getName(){
        return this.name;
    }

    /**
     * Function to set the name of the network
     * @param name Name of Network 
     */
    public void setNetworkName(String ethName) {
        this.ethName = ethName;
    }

    /**
     * Get the Network name
     * @return the name of network domain
     */
    public String getNetworkName(){
        return this.ethName;
    }

    /**
     * Function to set the IP address: useful for SSH 
     * @param ip IP address
     */
    public void setIP(String ip) {
        this.ip = ip;
    }

    /**
     * Get the IP address
     * @return the IP address
     */
    public String getIP(){
        return this.ip;
    }
}
