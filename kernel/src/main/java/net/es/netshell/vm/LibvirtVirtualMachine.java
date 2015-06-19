package net.es.netshell.vm;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.Network;
import org.libvirt.LibvirtException;

import net.es.netshell.vm.LibvirtManager;
import net.es.netshell.vm.LibvirtSSHVirtualMachine;
/**
 * creating by amercian on 06/10/2015
 */
public class LibvirtVirtualMachine extends LibvirtVirtualMachineDescriptor {

    /**
     * Functionality of LibvirtVirtual Machine is similar to Domain functionality
     * in Libvirt JAVA API (Create, define, destroy and execute commands in domains)
     */
    //private String virtualMachineFactory; 
    public static String libvirtVirtualMachine;
    //Create function calls to perform SSH via domain instance
    public LibvirtSSHVirtualMachine vmSSH = new LibvirtSSHVirtualMachine();

    public LibvirtVirtualMachine() { super(); }
    public LibvirtVirtualMachine(String libvirtVirtualMachine) {
	this.libvirtVirtualMachine = libvirtVirtualMachine;	
    }
    /**
     * Constructor that calls the LibvirtVirtualMachineDescriptor
     */
    public LibvirtVirtualMachine(String name, int memory, int cpu, String ethName, String ip, String gateway, String mac, String netmask) {
	super(name, memory, cpu, ethName, ip, gateway, mac, netmask);
    }	
    /**
     * Generate an XML string to create a domain based on user input.
     * @param name
     * @param memory 
     * @param cpu
     * @param virtualMachineFactory 
     * @return xml string for Domain
     */
    public String xmlDomain(String name, int memory, int cpu, String virtualMachineFactory, String ethName){
	String xml_domain = String.format("<domain type='%s'><name>%s</name><memory>%d</memory><os><type>exe</type><init>/sbin/init</init></os><vcpu>%d</vcpu><clock offset='utc'/><on_poweroff>destroy</on_poweroff><on_reboot>restart</on_reboot><on_crash>destroy</on_crash><devices><emulator>/usr/lib/libvirt/libvirt_lxc</emulator><filesystem type='mount'><source dir='/var/lib/lxc/%s/rootfs'/><target dir='/'/></filesystem><interface type='network'><source network='%s'/></interface><console type='pty' /></devices></domain>",virtualMachineFactory, name, memory, cpu, name, ethName);

	return xml_domain;
    }

    /**
     * Generate an XML string to create a network domain based on user input.
     * @param ethName
     * @param ip
     * @param gateway
     * @param mac 
     * @param netmask
     * @return xml string for Network
     */
    public String xmlNetwork(String ethName, String ip, String gateway, String mac, String netmask) {
	String xml_network = String.format("<network><name>'%s'</name><bridge name='virbr0'/><forward mode='nat'/><ip address='192.168.121.10' netmask='%s'><dhcp><range start='%s' end='%s'/></dhcp></ip></network>",ethName,netmask,ip,ip);
	return xml_network;
    }

    /**
     * Creates a domain based on the input requirements via the xml string generated
     * @param conn Connect class that is the specified hypervisor
     * @param xml_string XML format of domain description
     * @return Domain class created 
     */	
    public Domain create(Connect conn, String xml_domain) throws LibvirtException{
	Domain dom = null;
	try {
   	    dom = conn.domainDefineXML(xml_domain);	
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }
	int domain_connect = dom.create();
	if(domain_connect == 0){
	    System.out.println("Domain creation failed!");
	}
	return dom;
    }

    public void delete(Domain dom) throws LibvirtException {
	try {
	    dom.destroy();	
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }

    }

    /**
     * Creates a network domain based on the input requirements via the xml string generated
     * @param conn Connect class that is the specified hypervisor
     * @param xml_network XML format of network domain description
     * @return Network Domain class created 
     */	
    public Network createNetwork(Connect conn, String xml_network) throws LibvirtException{
	Network net = null;
	try {
	    net = conn.networkCreateXML(xml_network);
	    net.create();
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }	
	return net;
    }

    public void deleteNetwork(Network net) throws LibvirtException{
	try {
	    net.destroy();
	    net.free();
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }

    }
} 
