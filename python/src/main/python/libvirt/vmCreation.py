import os
import vmXMLDesc
from org.libvirt import Connect
from org.libvirt import ConnectAuthDefault
from org.libvirt import Domain
from org.libvirt import DomainInfo
from org.libvirt import DomainSnapshot
from org.libvirt import LibvirtException
from org.libvirt import Network

# Authored by amercian
# on 06/08/2015

def lxcCreation(vmName):
    subprocess.call(["lxc-create", "-t", "centos", "-n", "{}".format(vmName)])

#Libvirt Connection
def lxcConnect():
    try:
	conn = Connect("lxc:///")
    except LibvirtException:
        print "Exceptiton caught"

    #after connection is set, get hypervisor capabilities
    try:
        cap = conn.getCapabilities()
        print "Capabilities:", cap
    except LibvirtException:
        print "Exception caught in  getCapabilities"

   
    print "Host Name:", conn.getHostName()
    type =  conn.getType()

    print "free memory: ", conn.getFreeMemory()

    print "Node Info"
    print conn.nodeInfo()

    NodeInformation = str(conn.nodeInfo())

    nodeFields =  NodeInformation.split('\n')
    for i in range(0,len(nodeFields)):
       nodes = nodeFields[i].split(':')
       if(nodes[0] == "nodes"):
          print "Cells Free Memory: ", conn.getCellsFreeMemory(0, int(nodes[1]))

    #Get type of virtualization =  virConnectOpen 
    print "Virtualization  type: ", conn.getURI(), " with Version ", conn.getVersion()

    print "Libvirt Version: ", conn.getLibVirVersion()

    print "Is URI encrypted? ", conn.isEncrypted()
    return conn

#Define Domain based on xml string
def DomainCreate(conn, xml_string):
    try:
        dom = conn.domainDefineXML(xml_string)
        print "domain defined"
    except LibvirtException:
        print "Exception caught in Domain creation"

    #starting the domain
    try:
        dom.create()
        print "domain started"
    except LibvirtException:
        print "Exception caught in Domain start"

    try:
       domain = conn.domainLookupByName("vm2")
       print "Domain raw name: ", domain
    except LibvirtException:
        print "Exception caught in Domain Lookup"

    print "List of current Domains"
    print conn.listDomains()

    print "Number of defined Domains: ", conn.numOfDefinedDomains

def NetworkDomain(conn, xml_network_string):
    try:
        network_dom = conn.networkCreateXML(network_xml)
        print "Create Virtual Network details: ", network_dom
    except LibvirtException:
        print "Exceptiton caught in Network creation"


    #start the network
    try:
        network_dom.create()
    except LibvirtException:
        print "Exceptiton caught in Network start"

    print "Number of Defined but down Interfaces: ", conn.numOfDefinedInterfaces()
    print "List of Interfaces: ", conn.listInterfaces()
    print "List of defined Interfaces: ", conn.listDefinedInterfaces()
    print "List of Networks: ", conn.listNetworks()


