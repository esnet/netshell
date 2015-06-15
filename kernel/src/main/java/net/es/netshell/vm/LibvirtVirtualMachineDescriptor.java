package net.es.netshell.vm;

/** 
 * created by amercian on 06/10/2015
 */

import net.es.netshell.api.PersistentObject;

public class LibvirtVirtualMachineDescriptor extends PersistentObject {
    private int memory, cpu;
    private String name, ethName, ip, gateway, mac, netmask;

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
     */ 
    public LibvirtVirtualMachineDescriptor (String name, int memory, int cpu, String ethName, String ip, String gateway, String mac, String netmask) {
        this.name = name;
        this.memory = memory;
        this.cpu = cpu;
	this.ethName = ethName;
        this.ip = ip;
        this.gateway = gateway;
        this.mac = mac;
	this.netmask = netmask;
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
