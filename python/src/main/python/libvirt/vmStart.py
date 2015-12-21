# ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
# of the University of California, through Lawrence Berkeley National
# Laboratory (subject to receipt of any required approvals from the
# U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this
# software, please contact Berkeley Lab's Innovation & Partnerships
# Office at IPO@lbl.gov.
#
# NOTICE.  This Software was developed under funding from the
# U.S. Department of Energy and the U.S. Government consequently retains
# certain rights. As such, the U.S. Government has been granted for
# itself and others acting on its behalf a paid-up, nonexclusive,
# irrevocable, worldwide license in the Software to reproduce,
# distribute copies to the public, prepare derivative works, and perform
# publicly and display publicly, and to permit other to do so.

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
# Starts the VM 	#
#########################

name = ""

def main(argv):
   global name
   try:
      opts, args = getopt.getopt(argv,"hn:",["help","name="])
      #TODO enter the eth address and start that as well
   except getopt.GetoptError:
      #error if none of the options match
      print 'Incorrect Input Options.'
      print 'use for details: vmStart -h'
      sys.exit(2)
   for opt, arg in opts:
      if opt in ("-h", "--help") :
	print 'vmStart.py [options]... \n \t-h | --help \t\t\tprint this statement and exit \n \t-n | --name <VM name>\t\tname of virtual machine'
	sys.exit()
      elif opt in ("-n", "--name"):
	name = arg

if __name__ == "__main__":
   global name
   main(sys.argv[1:])

   cn = LibvirtManager("lxc")

   conn = cn.create("lxc")

   dom = conn.domainLookupByName(name)
   net = conn.networkLookupByName(ethName)

   flag = dom.create()
   #net.create()
   if(flag == 0):
	print "Domain started!"
   else:
	print "Domain failed!"
