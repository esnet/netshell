import sys, getopt
import operator
import subprocess, shlex
import select
import fileinput
from os.path import expanduser
from net.es.netshell.vm import LibvirtVirtualMachineDescriptor
from net.es.netshell.vm import LibvirtManager
from net.es.netshell.vm import LibvirtVirtualMachine
from net.es.netshell.vm import VirtualMachineFactory
from net.es.netshell.vm import LibvirtSSHVirtualMachine

# Authored by amercian
# on 06/08/2015

#################################################################
# VM Manager: Modify as per requirements
#################################################################

#Global variable
vmName = "" 
name = ""
mem = 0
cpu = 0
container = ""
os = ""
ethName = ""
ip = ""
mac = ""
gateway = ""
netmask = ""

def subprocess_cmd(command):
    process = subprocess.Popen(command,stdout=subprocess.PIPE, shell=True)
    proc_stdout = process.communicate()[0].strip()
    print proc_stdout

def main(argv):
   global name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask
   try:
      #opts, args = getopt.getopt(argv,"hn:o:s:e:i:",["help","name=","container=","os=","ethName=","ip="])
   except getopt.GetoptError:
      #Not getting this error
      print 'Incorrect Input Options.'
      print 'use for details: vmManager.py -h'
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
	print 'vmManager [options]... '
	sys.exit()
      elif opt in ("-n", "--name"):
	name = arg
      elif opt in ("-o", "--container"):
	container = arg
      elif opt in ("-s", "--os"):
	os = arg
      elif opt in ("-e", "--ethname"):
	ethName = arg
      elif opt in ("-i", "--ip"):
	ip = arg

   #print name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask

def secureShell():
   ## SSH 
   ssh = LibvirtSSHVirtualMachine()
   #SSH with password (not recommended)
   ssh.setPassword("NEWROOTPW")
   ssh.commandExecute(ip, "ls -l")
   #change hostname
   newname = "test"
   ssh.commandExecute(ip, "hostname {}".format(newname))

def secureShellOpen():
   global ip
   #SSH with key-gen pair (recommended)
   ssh = LibvirtSSHVirtualMachine()
   session = ssh.createSessionAuth(ip)
   return ssh

def secureShellClose(ssh):
   #Close session 
   ssh.disconnectSession(session)

if __name__ == "__main__":
   global name, ethName, ip, container

   #Modidy main based on functionality input
#   main(sys.argv[1:])

   cn = LibvirtManager() 
   vm = LibvirtVirtualMachine()

   container = cn.getVirtualMachineFactory()
   name = vm.setName(name)
   ethName = vm.getNetworkName(ethName)
   ip = vm.getIP(ip)

   conn = cn.create(container)

   #TODO LookupDomainName function

   #SSH Login session
   ssh = secureShellOpen()
   
   #Enter SSH functionality as needed
   # ... 
    
   #delete ssh session
   secureShellClose(ssh)   

   vm.deleteNetwork(net)

   vm.delete(dom)

   cn.delete(conn)
