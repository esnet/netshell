import sys, getopt
import operator

# Authored by amercian
# on 06/08/2015

#Global variable
vmName = "" 

def setVMname(name):
     vmName = name

def getVMname():
     return vmName

def XMLConfig(vmName, mem, cpu, container):
     xmlconfig = """
     <domain type='{4}'>
       <name>{0}</name>
       <memory>{1}</memory>
       <os>
         <type>exe</type>
         <init>/sbin/init</init>
       </os>
       <vcpu>{2}</vcpu>
       <clock offset='utc'/>
       <on_poweroff>destroy</on_poweroff>
       <on_reboot>restart</on_reboot>
       <on_crash>destroy</on_crash>
       <devices>
         <emulator>/usr/lib/libvirt/libvirt_lxc</emulator>
         <filesystem type='mount'>
           <source dir='/var/lib/lxc/{0}/rootfs'/>
           <target dir='/'/>
         </filesystem>
         <interface type='network'>
           <source network='default'/>
         </interface>
         <console type='pty' />
       </devices>
     </domain>
     """
     xml_string = xmlconfig.format(vmName, mem, cpu, container)
     return xml_string

def XMLNetworkConfig(EthName, ip, gateway, mac):
   network_config = """
   <interface type='ethernet' name='{0}'>
     <start mode='onboot'/>
     <mac address='{3}'/>
     <protocol family='ipv4'>
       <ip address="{1}" prefix="24"/>
       <route gateway="{2}"/>
     </protocol>
   </interface>
   """
   xml_network_string = network_config.format(EthName, ip, gateway, mac)
   return xml_network_string

def main(self, argv):
    try:
	opts, args = getopt.getopt(argv,"hn:m:c:o:e:i:g:x:",["help","name=","memory=","cpu=","container=","eth=","ip=","gateway=","mac="])
    except getopt.GetoptError:
	print 'Incorrect Input Options.'
	print 'vmXMLDesc.py -n <VM name> -m <memory> -c <CPU nodes> -o <container> -e <ethernetName> -i <IP Address> -g <gateway> -x <MAC>'
	sys.exit(2)
    for opt, arg in opts:
	if opt == '-h':
	    print 'vmXMLDesc.py -n <VM name> -m <memory> -c <CPU nodes> -o <container> -e <ethernetName> -i <IP Address> -g <gateway> -x <MAC>' 
	    sys.exit()
	elif opt in ("-n", "--name"):
	    name = arg
	elif opt in ("-m", "--memory"):
	    mem = arg
	elif opt in ("-c", "--cpu"):
	    cpu = arg
	elif opt in ("-o", "--container"):
	    container = arg
	elif opt in ("-e", "--ethernet"):
	    container = ethName
	elif opt in ("-i", "--ip"):
	    container = ip
	elif opt in ("-g", "--gateway"):
	    container = gateway
	elif opt in ("-x", "--mac"):
	    container = mac

    xml_string = self.XMLConfig(name, mem, cpu, container)
    network_xml_string = self.NetworkXMLConfig(ethName, ip, gateway, mac)

    setVMname(name)

    #Include Default selections
    #Check to see alternate naming

if __name__ == "__main__":
	main(sys.argv[1:])
