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
# on 08/11/2015

#################################################################
# VM SSH Key Gen: Used to create and config SSH			#
#################################################################

#Global variable
name = "" 
ip = ""

def subprocess_cmd(command):
   process = subprocess.Popen(command,stdout=subprocess.PIPE, shell=True)
   proc_stdout = process.communicate()[0].strip()
   print proc_stdout



def secureShellKeyGen(vm):
   global name, ip
   #SSH with key-gen pair (recommended)
   ssh = LibvirtSSHVirtualMachine()
   ssh.createAuth(ip,"rsa",vm)
   #This line changes if ubuntu
   subprocess_cmd("mkdir /var/lib/libvirt/lxc/{}/root/.ssh".format(name))
   subprocess.call(["sudo","cp","{}/.ssh/id_rsa.pub".format(expanduser("~")),"/var/lib/libvirt/lxc/{}/root/.ssh/authorized_keys".format(name)]) 
   session = ssh.createSessionAuth(ip)
   #Any Processes 
   ssh.disconnectSession(session)
   print "Created Key-Gen Authentication"
   #To enable login to VM using CLI change permissions
   subprocess.call(["sudo","chmod","a-rwx","{}/.ssh/id_rsa".format(expanduser("~"))])
   subprocess.call(["sudo","chmod","u+rw","{}/.ssh/id_rsa".format(expanduser("~"))])
   print "Added to connected hosts"

def main(argv):
   global name, ip
   try:
      opts, args = getopt.getopt(argv,"hn:i:",["help","name=","ip="])
   except getopt.GetoptError:
      #error if none of the options match
      print 'Incorrect Input Options.'
      print 'use for details: vmSSHKeyGen.py -h'
      sys.exit(2)
   for opt, arg in opts:
      if opt in ("-h", "--help") :
	print 'vmSSHKeyGen.py [options]... \n \t-h | --help \t\t\tprint this statement and exit \n \t-n | --name <VM name>\t\tname of virtual machine \n \t-i | --ip \t\tIP address of VM'
	sys.exit()
      elif opt in ("-n", "--name"):
	name = arg
      elif opt in ("-i", "--ip"):
	ip = arg

if __name__ == "__main__":
   global name, ip
   main(sys.argv[1:])

   cn = LibvirtManager("lxc")
   vm = LibvirtVirtualMachine()

   conn = cn.create("lxc")
   dom = vm.domainLookUp(conn, name)

   #Restart Network in remote host (issue after testing)
   #command = "/var/lib/libvirt/lxc/{}/".format(name)
   #subprocess.call(["chroot",command,"/bin/bash","-c","service network restart"]) 
 
   #SSH Capability
   print "Creating Secure Shell link"
   secureShellKeyGen(vm)

   print "Created Key-Gen for automated SSH"
