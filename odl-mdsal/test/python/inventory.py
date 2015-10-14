#
# ENOS, Copyright (c) 2015, The Regents of the University of California,
# through Lawrence Berkeley National Laboratory (subject to receipt of any
# required approvals from the U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this software,
# please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
#
# NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
# the U.S. Government has been granted for itself and others acting on its
# behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
# to reproduce, prepare derivative works, and perform publicly and display
# publicly.  Beginning five (5) years after the date permission to assert
# copyright is obtained from the U.S. Department of Energy, and subject to
# any subsequent five (5) year renewals, the U.S. Government is granted for
# itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
# worldwide license in the Software to reproduce, prepare derivative works,
# distribute copies to the public, perform publicly and display publicly, and
# to permit others to do so.
#
__author__ = 'bmah'

#
# Get switch and ports (node and node connectors) inventory on MD-SAL
#
from java.math import BigInteger
from net.es.netshell.odlmdsal.impl import OdlMdsalImpl
from org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819 import FlowCapableNode
from org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819 import FlowCapableNodeConnector

omi = OdlMdsalImpl.getInstance()

ss = omi.getNetworkDevices()
for s in ss:
    fcn = s.getAugmentation(FlowCapableNode)
    if fcn:
        spl = s.getId().getValue().split(":")
        print "dpid " + str(int(spl[1])) + " (" + BigInteger(spl[1]).toString(16) + ") : " + fcn.getManufacturer() + " / " + fcn.getHardware() + " @ " + fcn.getIpAddress().getIpv4Address().getValue()
        conns = omi.getNodeConnectors(s)
        for conn in conns:
            fcnc = conn.getAugmentation(FlowCapableNodeConnector)
            if fcnc:
                print "  " + conn.getId().getValue() + " => " + fcnc.getName() + " (port " + str(fcnc.getPortNumber().getUint32()) + ")"

