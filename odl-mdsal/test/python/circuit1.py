__author__ = 'bmah'
#
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

#
# Set up an end-to-end circuit on the SDN testbed, with VLAN
# translation to get across OSCARS circuits, etc.  No MAC translation
# used here.
#
# Extremely specific to one particular configuration of the ESnet testbed,
# but is useful for illustrating how to perform OpenFlow operations
#
from java.math import BigInteger
from net.es.netshell.odlmdsal.impl import OdlMdsalImpl
from net.es.netshell.odlcorsa.impl import OdlCorsaImpl

from org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924 import MacAddress

omi = OdlMdsalImpl.getInstance()
oci = OdlCorsaImpl.getInstance()

# Get the switches in the ODL world
swDENV = omi.getNetworkDeviceByDpid(BigInteger("02010064656e7601", 16).longValue())
swAOFA = omi.getNetworkDeviceByDpid(BigInteger("020100616f666101", 16).longValue())

# Get interesting node connectors
portDENV23 = omi.getNodeConnector(swDENV, "23")
portDENV24 = omi.getNodeConnector(swDENV, "24")
portAOFA23 = omi.getNodeConnector(swAOFA, "23")
portAOFA24 = omi.getNodeConnector(swAOFA, "24")

# Get the MAC addresses of the DTN data plane interfaces
macLBL = MacAddress("00:60:DD:44:2B:2C")
macBNL = MacAddress("00:60:DD:44:2B:18")
macANL = MacAddress("00:60:DD:44:2B:14")
macBroadcast = MacAddress("FF:FF:FF:FF:FF:FF")

# Set up green meters (no rate limiting) on switches
meterDENV = oci.createGreenMeter(swDENV, 1L)
meterAOFA = oci.createGreenMeter(swAOFA, 1L)

# LBL to BNL direction, unicast and broadcast
fr1 = oci.createTransitVlanMacCircuit(swDENV, 10, BigInteger("0"), macBNL, portDENV24.getId(), 1994, macBNL, portDENV23.getId(), 582, 0, 0, 1)
fr1b = oci.createTransitVlanMacCircuit(swDENV, 10, BigInteger("0"), macBroadcast, portDENV24.getId(), 1994, macBroadcast, portDENV23.getId(), 582, 0, 0, 1)
fr2 = oci.createTransitVlanMacCircuit(swAOFA, 10, BigInteger("0"), macBNL, portAOFA23.getId(), 582, macBNL, portAOFA24.getId(), 116, 0, 0, 1)
fr2b = oci.createTransitVlanMacCircuit(swAOFA, 10, BigInteger("0"), macBroadcast, portAOFA23.getId(), 582, macBroadcast, portAOFA24.getId(), 116, 0, 0, 1)

# BNL to LBL direction, unicast and broadcast
fr3 = oci.createTransitVlanMacCircuit(swAOFA, 10, BigInteger("0"), macLBL, portAOFA24.getId(), 116, macLBL, portAOFA23.getId(), 582, 0, 0, 1)
fr3b = oci.createTransitVlanMacCircuit(swAOFA, 10, BigInteger("0"), macBroadcast, portAOFA24.getId(), 116, macBroadcast, portAOFA23.getId(), 582, 0, 0, 1)
fr4 = oci.createTransitVlanMacCircuit(swDENV, 10, BigInteger("0"), macLBL, portDENV23.getId(), 582, macLBL, portDENV24.getId(), 1994, 0, 0, 1)
fr4b = oci.createTransitVlanMacCircuit(swDENV, 10, BigInteger("0"), macBroadcast, portDENV23.getId(), 582, macBroadcast, portDENV24.getId(), 1994, 0, 0, 1)

# lbl-diskpt1% ip link add link eth2 name eth2.1994 type vlan id 1994
# lbl-diskpt1% ifconfig eth2.1994 192.168.188.1/24
# bnl-diskpt1% ip link add link eth2 name eth2.116 type vlan id 116
# bnl-diskpt1% ifconfig eth2.116 192.168.188.2/24
# lbl-diskpt1% ping 192.168.188.2
