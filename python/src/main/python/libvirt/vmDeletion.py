from org.libvirt import Connect
from org.libvirt import Domain
from org.libvirt import LibvirtException
from org.libvirt import Network

# authored by amercian
# on 06/08/2015

def interfaceDestroy(conn, ifaceName):
    iface = conn.interfaceLookupByName("{}".ifaceName)

    #Deactivating interface before close
    try:
           iface.destroy()
    except LibvirtException:
           print "Exceptiton caught in Interface deletion"

    #undefining an interface (use if required)
    iface.free()

def domainDestroy(conn, domain):
    try:
       domain.destroy()
    except LibvirtException:
       print "Exception caught in Domain destroy"

    print "List of current Domains"
    print conn.listDomains()

def connectionClose(conn):
    try:
       conn.close()
    except LibvirtException:
       print "Exceptiton caught in Connection Close" 
