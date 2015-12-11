# -*- Mode: python -*-
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
