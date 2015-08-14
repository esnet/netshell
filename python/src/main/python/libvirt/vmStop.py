import sys, getopt
import operator
import subprocess
import org.libvirt.Connect
import org.libvirt.Domain
import org.libvirt.Network

from net.es.netshell.vm import LibvirtVirtualMachineDescriptor
from net.es.netshell.vm import LibvirtManager
from net.es.netshell.vm import LibvirtVirtualMachine
from net.es.netshell.vm import VirtualMachineFactory
from net.es.netshell.vm import LibvirtSSHVirtualMachine

# Authored by amercian
# on 08/11/2015

#########################
# Stops the VM 		#
#########################

name = ""

def main(argv):
   global name
   try:
      opts, args = getopt.getopt(argv,"hn:",["help","name="])
      #TODO enter the eth address and start
   except getopt.GetoptError:
      #error if none of the options match
      print 'Incorrect Input Options.'
      print 'use for details: vmStop -h'
      sys.exit(2)
   for opt, arg in opts:
      if opt in ("-h", "--help") :
	print 'vmStop.py [options]... \n \t-h | --help \t\t\tprint this statement and exit \n \t-n | --name <VM name>\t\tname of virtual machine'
	sys.exit()
      elif opt in ("-n", "--name"):
	name = arg

if __name__ == "__main__":
   global name
   main(sys.argv[1:])

   cn = LibvirtManager("lxc")

   conn = cn.create("lxc")

   dom = conn.domainLookupByName(name)

   dom.shutdown()
   
