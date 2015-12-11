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

import net.es.netshell.vm.LibvirtManager;
import net.es.netshell.vm.LibvirtVirtualMachine;

/** 
 *  created by amercian on 06/10/2015
 */
public abstract class VirtualMachineFactory extends LibvirtManager {

     /**
      * Factory to consolidate differnt type of hypervisors that can be scaled to use. 
      * getFactory will return function call to appropriate constructor defined
      * @param virtualMachineFactory the different types of hypervisors 
      * @return corresponding Constructor function call
      */
     private static LibvirtVirtualMachine libvirtVirtualMachine;
     
     public VirtualMachineFactory(){super();}

     public static LibvirtVirtualMachine getFactory(String virtualMachineFactory){
	if (virtualMachineFactory.equals("lxc")){
	    //return the constructor of Libvirt Virtual Machine
	    if (LibvirtVirtualMachine.libvirtVirtualMachine == null) {
 	        return new LibvirtVirtualMachine();
	    }
	}
	// TODO if needed to be extended
	
	/*if (virtualMachineFactory.equals("qemu"))
	    return new QemuVirtualMachine(); */
	return null;
     }
}
