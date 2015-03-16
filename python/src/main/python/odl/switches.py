# -*- Mode: python -*-
#
# Emit an inventory of all OpenFlow switches connected to the controller.
#
from java.util import LinkedList

from net.es.netshell.odl import Controller

from org.opendaylight.controller.sal.core import Node
from org.opendaylight.controller.sal.core import NodeConnector
from org.opendaylight.controller.sal.utils import HexEncode

cont = Controller.getInstance()

# Get a reference to the switch and to its connectors (switch
# ports).  The connectors come to us in a HashSet, which we
# need to convert to an array to do anything useful with.
switches = cont.getNetworkDevices()

for s in switches:

    conns = s.nodeConnectors.toArray()
    spans = s.spanPorts.toArray()
    print "DPID: " + HexEncode.bytesToHexStringFormat(s.dataLayerAddress) + " Ports: " + str(len(conns)) + \
          " SPANs: " + str(len(spans))
