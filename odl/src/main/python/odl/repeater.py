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
# By default, mininet creates a simple network with one switch
# having two ports, with one host on each port.  This netshell
# script sets up the ODL controller to hook those two ports up
# back to back, basically making the switch a wire.
#
# This is even more trivial than the nearly-ubiquitous MAC learning
# bridge example, but it demonstrates how to make the controller do
# things in a proactive way.
from java.util import LinkedList

from net.es.netshell.odl import Controller

from org.opendaylight.controller.sal.core import Node
from org.opendaylight.controller.sal.core import NodeConnector

from org.opendaylight.controller.sal.match import Match
from org.opendaylight.controller.sal.match import MatchType

from org.opendaylight.controller.sal.action import Action
from org.opendaylight.controller.sal.action import Output

from org.opendaylight.controller.sal.flowprogrammer import Flow

# Controller behaves something like a singleton class in that
# there is at most one instance of it.  Get that.  We haven't
# addressed security much at this point, but when we do, it will
# probably take the form of controlling access to this instance
# object or its methods.
cont = Controller.getInstance()

# Get a reference to the switch and to its connectors (switch
# ports).  The connectors come to us in a HashSet, which we
# need to convert to an array to do anything useful with.
switches = cont.getNetworkDevices()
s0 = switches[0]
conn0 = s0.nodeConnectors.toArray()

# Create match rules.  We make two rules, one to match the
# packets coming in on each of the two connectors.
m0 = Match()
m0.setField(MatchType.IN_PORT, conn0[0])

m1 = Match()
m1.setField(MatchType.IN_PORT, conn0[1])

# Create two action lists, one to send packets to each of the two
# connectors.  See org.opendaylight.controller.sal.action.ActionType
# for a list of all possible actions.
fwd0 = LinkedList()
fwd0.add(Output(conn0[0]))

fwd1 = LinkedList()
fwd1.add(Output(conn0[1]))

# From these match rules and actions, create the two flow rules.
# For each port we take all packets arriving on that connector
# and forward to the other connector.
flow0 = Flow(m0, fwd1)
flow1 = Flow(m1, fwd0)

# Push the flows to the switch.   Use "dpctl dump-flows" from within
# mininet to confirm the flows were pushed correctly (as well as just
# verifying that the switch now forwards packets correctly between the
# two ports).
cont.addFlow(switches[0].node, flow0)
cont.addFlow(switches[0].node, flow1)

