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

import org.libvirt.*;
import org.libvirt.Connect;

import net.es.netshell.api.PersistentObject;

/**
 * created by amercian on 06/10/2015
 */
//whether should be abstract
public class LibvirtManager { 
    /**
     * Object that defines the components of Virtual Machine
     */
    private String virtualMachineFactory;

  /**
   * Constructor that sets the different values from the  user input
   */
  public LibvirtManager() { }

  public LibvirtManager(String virtualMachineFactory) {
       	this.virtualMachineFactory = virtualMachineFactory;
    }

    /**
     * Creating the base connection for introducing virtual machine domains
     * @param virtualMachineFactory type of Hypervisor
     * @return Returns the connection class that can be used to define domains
     * @throws Libvirtexception when connection fails
     */
    public Connect create(String virtualMachineFactory) throws LibvirtException {
	Connect conn = null;
	
	//Include other connect formats to scale qemu, kvm etc.
	try {
	    conn = new Connect(String.format("%s:///",virtualMachineFactory));
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }
	return conn;
    }

    /**
     * Process remote access to defined domains 
     * @param virtualMachineFactory type of hypervisor
     * @param ip Remote URI
     * @return connection
     */	
    public Connect secureConnectDomain(String virtualConnectFactory, String ip) throws LibvirtException {
	Connect conn = null;
	try {
            conn = new Connect(String.format("%s+ssh:///root@%s",virtualMachineFactory, ip));
	} catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }
	return conn;
    }


    /**
     * Destroys the Connection, After the completion of Virtual Machine usage.
     * @param conn the connection variable
     */
    public void delete(Connect conn) throws LibvirtException {
	try {
	    conn.close();
	}catch (LibvirtException e) {
            System.out.println("exception caught:"+e);
            System.out.println(e.getError());
        }
 
    }

    /**
     * Setting the Factory Name which is the only requirement for creating a connection
     * @param virtualMachineFactory defines the hypervisor/linux container
     */
    public void setVirtualMachineFactory(String virtualMachineFactory){
	this.virtualMachineFactory = virtualMachineFactory;
    }

    /**
     * Gets the Factory Name 
     * @return Returns the virtualMachineFactory name
     */
    public String getVirtualMachineFactory() {
	return this.virtualMachineFactory;
    }
}
