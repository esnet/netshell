import sys, getopt
import operator
from net.es.netshell.vm import LibvirtVirtualMachineDescriptor
from net.es.netshell.vm import VirtualMachine
from net.es.netshell.vm import LibvirtVirtualMachine
from net.es.netshell.vm import VirtualMachineFactory

# Authored by amercian
# on 06/08/2015

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

def lxcCreate(name, os):
	global name
	global os
	subprocess.call(["sudo", "lxc-create", "-t", "{}".format(os), "-n", "{}".format(name)])


def main(argv):
	global name, mem, cpu, container
	try:
		opts, args = getopt.getopt(argv,"hn:m:c:o:s:e:i:a:g:t:",["help","name=","memory=","cpu=","container=","os=","ethName=","ip=","mac=","gateway=","netmask"])
	except getopt.GetoptError:
        	print 'Incorrect Input Options.'
        	print 'use for details: vmManager.py -h'
        	sys.exit(2)
	for opt, arg in opts:
		if opt == '-h':
			 print 'vmManager [options]... \n options ([m] = mandatory): \n \t-n | --name <VM name>\t\t[m]name of virtual machine \n \t-m | --memory <memory>\t\tassigned memory \n \t-c | --cpu <CPU nodes>\t\tnumber of cpu nodes \n \t-o | --container <container>\t[m]hypervisor type \n \t-s | --os <OS>\t\t[m]OS type \n \t-e | --ethName <ethernet name>\t[m]ethernet name \n \t-i | --ip <IP address>\t\tip address \n \t-a | --mac <MAC address>\tmac address \n \t-g | --gateway <Gateway>\tgateway address \n \t-t | --netmask <netmask>\tnetmask for routing '
			sys.exit()
		elif opt in ("-n", "--name"):
			print "argument at this line: ", arg
			NAME = arg
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

	#Include Default selections
	#Check to see alternate naming

if __name__ == "__main__":
	print "main: ", sys.argv
	main(sys.argv[1:])
	
	#Creating the rootfs file system of Container
	if container == 'lxc':
	     lxcCreate(name, os)

	cn = LibvirtManager(container) 
	vm = LibvirtVirtualMachineescriptor(name, mem, cpu, ethName, ip, gateway, mac, netmask)
       
	vm.setName(name)
	vm.setEthName(ethName)
     
	conn = cn.create(container)

	xml_domain = vm.xmlDomain(name, mem, cpu, container)

	xml_network = vm.xmlNetwork(ethName, ip, gateway, mac, netmask)

       #manually start lxc on host to be observable by lxc tools
	net = vm.createNetwork(conn, xml_network)
	dom = vm.create(conn, xml_domain)

	vm.deleteNetwork(conn, net)

	vm.delete(conn, dom)

	cn.delete(conn)
