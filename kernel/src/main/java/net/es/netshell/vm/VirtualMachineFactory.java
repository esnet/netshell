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
