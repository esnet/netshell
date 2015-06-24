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
# on 06/20/2015

#################################################################
# VM Configuration: Used to create/config VM, config SSH
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

def lxcUbuntuCreate():
   global name
   global os
   subprocess.call(["sudo", "lxc-create", "-t", "{}".format(os), "-n", "{}".format(name)])
   #Beware that the xml config file has to be changed accordingly
   #/var/lib/libvirt/lxc/vmName/rootfs 

def lxcCentosCreate():
   global name
   #LXC is properly an activated SE-Linux
   print "Entering Centos VM Creation ..."
   fileToSearch = "/etc/selinux/config"
   subprocess_cmd("cp /etc/selinux/config /etc/selinux/config.bk")
   textToSearch1 = "SELINUX=enforcing"
   textToSearch2 = "SELINUX=disabled"
   textToReplace = "SELINUX=permissive"
   for line in fileinput.input(fileToSearch, inplace=True):
      newline = line.replace(textToSearch1, textToReplace).strip()
      print newline

   #activate the installed services to start automatically at system startup
   subprocess_cmd("systemctl enable libvirt-guests; systemctl enable libvirtd; systemctl start libvirt-guests; systemctl start libvirtd")

   #create Centos 7 repository from host repository
   subprocess.call(["mkdir", "/var/lib/libvirt/lxc/{}/etc/yum.repos.d/".format(name), "-p"])
   subprocess_cmd("cat /etc/yum.repos.d/CentOS-Base.repo|sed s/'$releasever'/7/g > /var/lib/libvirt/lxc/{}/etc/yum.repos.d/CentOS-Base.repo".format(name))

   #install core components
   subprocess_cmd("yum groupinstall core --installroot=/var/lib/libvirt/lxc/{}/ --nogpgcheck -y".format(name))
   subprocess_cmd("yum install plymouth libselinux-python --installroot=/var/lib/libvirt/lxc/{}/ --nogpgcheck -y".format(name))
   print "Created VM repository!!"

def lxcCentosConfig():
   global name
   #Config the VM with temporary password and SSH capability
   command = "/var/lib/libvirt/lxc/{}/".format(name)
   print "Starting VM Configuration ... "
   subprocess.call(["chroot",command,"/bin/bash","-c","echo NEWROOTPW |passwd root --stdin"])
   print "Changed password successfully"
   subprocess.call(["chroot",command,"/bin/bash","-c","""echo "pts/0" >>/etc/securetty"""])
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_selinux.so close"/"#session    required     pam_selinux.so close"/g /etc/pam.d/login"""])
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_selinux.so open"/"#session    required     pam_selinux.so open"/g /etc/pam.d/login"""])
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_loginuid.so"/"#session    required     pam_loginuid.so"/g /etc/pam.d/login"""])
 
   # login ssh
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_selinux.so close"/"#session    required     pam_selinux.so close"/g /etc/pam.d/sshd"""])
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_loginuid.so"/"#session    required     pam_loginuid.so"/g /etc/pam.d/sshd"""])
   subprocess.call(["chroot",command,"/bin/bash","-c","""sed -i s/"session    required     pam_selinux.so open env_params"/"#session    required     pam_selinux.so open env_params"/g /etc/pam.d/sshd"""])

   f = open("{}/etc/sysconfig/network".format(command),"w")
   f.write("NETWORKING=yes \nHOSTNAME=lxc.der-linux-admin.de\n")
   f.close()

   f = open("{}/etc/sysconfig/network-scripts/ifcfg-eth0".format(command), "w")
   f.write("DEVICE=eth0 \nBOOTPROTO=dhcp \nONBOOT=yes\n")
   f.close()

   subprocess.call(["chroot",command,"/bin/bash","-c","systemctl enable sshd"])
 
   subprocess.call(["chroot",command,"/bin/bash","-c","systemctl disable avahi-daemon"])
   subprocess.call(["chroot",command,"/bin/bash","-c","systemctl disable auditd"])

   print "Configured the VM!!"


def main(argv):
   global name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask
   try:
      opts, args = getopt.getopt(argv,"hn:m:c:o:s:e:i:a:g:t:",["help","name=","memory=","cpu=","container=","os=","ethName=","ip=","mac=","gateway=","netmask"])
   except getopt.GetoptError:
      #Not getting this error
      print 'Incorrect Input Options.'
      print 'use for details: vmManager.py -h'
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
	print 'vmManager [options]... \n options ([m] = mandatory): \n \t-h | --help \t\t\tprint this statement and exit \n \t-n | --name <VM name>\t\t[m]name of virtual machine \n \t-m | --memory <memory>\t\tassigned memory \n \t-c | --cpu <CPU nodes>\t\tnumber of cpu nodes \n \t-o | --container <container>\t[m]hypervisor type \n \t-s | --os <OS>\t\t\t[m]OS type \n \t-e | --ethName <ethernet name>\t[m]ethernet name \n \t-i | --ip <IP address>\t\tip address \n \t-a | --mac <MAC address>\tmac address \n \t-g | --gateway <Gateway>\tgateway address \n \t-t | --netmask <netmask>\tnetmask for routing '
	sys.exit()
      elif opt in ("-n", "--name"):
	name = arg
      elif opt in ("-m", "--memory"):
	mem = arg
      elif opt in ("-c", "--cpu"):
	cpu = arg
      elif opt in ("-o", "--container"):
	container = arg
      elif opt in ("-s", "--os"):
	os = arg
      elif opt in ("-e", "--ethname"):
	ethName = arg
      elif opt in ("-i", "--ip"):
	ip = arg
      elif opt in ("-a", "--mac"):
	mac = arg
      elif opt in ("-g", "--gateway"):
	gateway = arg
      elif opt in ("-t", "--netmask"):
	netmask = arg

   print name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask

   #Default settingss
   if(name == "" or mem == 0 or cpu == 0 or container == "" or os == "" or ethName == "" or ip == "" or mac == "" or gateway == "" or netmask == ""):
      defaultVM()

def defaultVM():
   global name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask
   if(name == ""):
      name = "centos-7"
   if(mem == 0):
      mem = 300000
   if(cpu == 0):
      cpu = 1
   if(container == ""):
      container = "lxc"
   if(os == ""):
      os = "centos"
   if(ethName == ""):
      ethName = "default1"
   if(ip == ""):
      ip = "192.168.121.100"
   if(mac == ""):
      mac = "" #Libvirt will give a default randomly generated mac address
   if(gateway == ""):
      gateway = "192.168.121.1" 
   if(netmask == ""):
      netmask = "255.255.255.0" #Class A  
   print name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask

def secureShell():
   global ip
   ## SSH 
   ssh = LibvirtSSHVirtualMachine()
   #SSH with password (not recommended)
   ssh.setPassword("NEWROOTPW")
   ssh.commandExecute(ip, "ls -l")
   #change hostname
   newname = "test"
   ssh.commandExecute(ip, "hostname {}".format(newname))
   ssh.disconnectSession(session)
   print "Changed host name of VM ", name

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

if __name__ == "__main__":
   global name, mem, cpu, container, os, ethName, ip, mac, gateway, netmask   
   main(sys.argv[1:])

   #Creating the rootfs file system of Container
   if container == 'lxc':
      print "Centos 7 Creation"
      lxcCentosCreate()
      lxcCentosConfig()

   print "Entering Libvirt Manager..."
   cn = LibvirtManager(container) 
   vm = LibvirtVirtualMachine(name, mem, cpu, ethName, ip, gateway, mac, netmask)

   print "Setting VM parameters..."
   cn.setVirtualMachineFactory(container)
   vm.setName(name)
   vm.setNetworkName(ethName)
   vm.setIP(ip)

   print "Creating VM connection..."
   conn = cn.create(container)

   xml_domain = vm.xmlDomain(name, mem, cpu, cn.getVirtualMachineFactory(), vm.getNetworkName())

   xml_network = vm.xmlNetwork(ethName, ip, gateway, mac, netmask)

   #manually start lxc on host to be observable by lxc tools
   print "Creating Network"
   net = vm.createNetwork(conn, xml_network)
   print "Creating Domain"
   dom = vm.create(conn, xml_domain)

   #Restart Network in remote host (issue after testing)
   command = "/var/lib/libvirt/lxc/{}/".format(name)
   subprocess.call(["chroot",command,"/bin/bash","-c","service network restart"]) 
 
   #SSH Capability
   print "Creating Secure Shell link"
   secureShellKeyGen(vm)

    ### Delete only if necessary
#   vm.deleteNetwork(net)

#   vm.delete(dom)
   print "Closing Connection"
   cn.delete(conn)
