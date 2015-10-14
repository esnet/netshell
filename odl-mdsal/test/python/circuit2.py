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

# Set up end-to-end flow on testbed, Take 2
# More complicated, using OVS switch going eastbound (but not westbound)
# We do MAC translation across the wide area circuit between hardware switches.

from java.math import BigInteger
from net.es.netshell.odlmdsal.impl import OdlMdsalImpl
from net.es.netshell.odlcorsa.impl import OdlCorsaImpl

from org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924 import MacAddress

omi = OdlMdsalImpl.getInstance()
oci = OdlCorsaImpl.getInstance()

# Get the switches in the ODL world
hwDENV = omi.getNetworkDeviceByDpid(BigInteger("02010064656e7601", 16).longValue())
ovsDENV = omi.getNetworkDeviceByDpid(BigInteger("01020064656e7601", 16).longValue())
hwAOFA = omi.getNetworkDeviceByDpid(BigInteger("020100616f666101", 16).longValue())
ovsAOFA = omi.getNetworkDeviceByDpid(BigInteger("010200616f666101", 16).longValue())

# Get interesting node connectors
hwDENV1 = omi.getNodeConnector(hwDENV, "1")
hwDENV2 = omi.getNodeConnector(hwDENV, "2")
hwDENV23 = omi.getNodeConnector(hwDENV, "23")
hwDENV24 = omi.getNodeConnector(hwDENV, "24")
ovsDENVeth10 = omi.getNodeConnector(ovsDENV, "eth10")
ovsDENVeth11 = omi.getNodeConnector(ovsDENV, "eth11")
hwAOFA1 = omi.getNodeConnector(hwAOFA, "1")
hwAOFA23 = omi.getNodeConnector(hwAOFA, "23")
hwAOFA24 = omi.getNodeConnector(hwAOFA, "24")
ovsAOFAeth10 = omi.getNodeConnector(ovsAOFA, "eth10")

# Get the MAC addresses of the DTN data plane interfaces
macLBL = MacAddress("00:60:DD:44:2B:2C")
macBNL = MacAddress("00:60:DD:44:2B:18")
macANL = MacAddress("00:60:DD:44:2B:14")
macBroadcast = MacAddress("FF:FF:FF:FF:FF:FF")
macTranslateW = MacAddress("00:00:00:00:00:01")
macTranslateE = MacAddress("00:00:00:00:00:02")

# Set up green meters (no rate limiting) on switches
meterDENV = oci.createGreenMeter(hwDENV, 1L)
meterDENV2 = oci.createGreenMeter(hwDENV, 2L)

meterAOFA = oci.createGreenMeter(hwAOFA, 1L)

# LBL to BNL direction, unicast and broadcast through the software switch denv-ovs
# Translate BNL addresses going eastbound over the WAN
fr1 = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBNL, hwDENV24.getId(), 1994, macTranslateE, hwDENV1.getId(), 10, 0, 0, 1)
fr1b = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBroadcast, hwDENV24.getId(), 1994, macBroadcast, hwDENV1.getId(), 10, 0, 0, 1)

# Use the following two flows to bypass the OVS switch
#fr1 = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBNL, hwDENV24.getId(), 1994, macTranslateE, hwDENV23.getId(), 582, 0, 0, 1)
#fr1b = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBroadcast, hwDENV24.getId(), 1994, macBroadcast, hwDENV23.getId(), 582, 0, 0, 1)

# Use the following two flows to force their packets to generate a PACKET_IN
#fr1 = oci.sendVlanMacToController(hwDENV, 10, BigInteger("0"), macBNL, hwDENV24.getId(), 1994)
#fr1b = oci.sendVlanMacToController(hwDENV, 10, BigInteger("0"), macBroadcast, hwDENV24.getId(), 1994)

fr2 = omi.createTransitVlanMacCircuit(ovsDENV, 10, BigInteger("0"), macTranslateE, ovsDENVeth10.getId(), 10, macTranslateE, ovsDENVeth10.getId(), 11, 0, 0, 1)
fr2b = omi.createTransitVlanMacCircuit(ovsDENV, 10, BigInteger("0"), macBroadcast, ovsDENVeth10.getId(), 10, macBroadcast, ovsDENVeth10.getId(), 11, 0, 0, 1)

fr3 = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macTranslateE, hwDENV1.getId(), 11, macTranslateE, hwDENV23.getId(), 582, 0, 0, 1)
fr3b = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBroadcast, hwDENV1.getId(), 11, macBroadcast, hwDENV23.getId(), 582, 0, 0, 1)

fr4 = oci.createTransitVlanMacCircuit(hwAOFA, 10, BigInteger("0"), macTranslateE, hwAOFA23.getId(), 582, macBNL, hwAOFA24.getId(), 116, 0, 0, 1)
fr4b = oci.createTransitVlanMacCircuit(hwAOFA, 10, BigInteger("0"), macBroadcast, hwAOFA23.getId(), 582, macBroadcast, hwAOFA24.getId(), 116, 0, 0, 1)

# BNL to LBL direction, unicast and broadcast
# This circuit uses only hardware switches, and skips the software switches.
# Unicast MAC addresses translated going westbound across the wide area.
fr4r = oci.createTransitVlanMacCircuit(hwAOFA, 10, BigInteger("0"), macLBL, hwAOFA24.getId(), 116, macTranslateW, hwAOFA23.getId(), 582, 0, 0, 1)
fr4rb = oci.createTransitVlanMacCircuit(hwAOFA, 10, BigInteger("0"), macBroadcast, hwAOFA24.getId(), 116, macBroadcast, hwAOFA23.getId(), 582, 0, 0, 1)

fr5r = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macTranslateW, hwDENV23.getId(), 582, macLBL, hwDENV24.getId(), 1994, 0, 0, 1)
fr5rb = oci.createTransitVlanMacCircuit(hwDENV, 10, BigInteger("0"), macBroadcast, hwDENV23.getId(), 582, macBroadcast, hwDENV24.getId(), 1994, 0, 0, 1)

# lbl-diskpt1% ip link add link eth2 name eth2.1994 type vlan id 1994
# lbl-diskpt1% ifconfig eth2.1994 192.168.188.1/24
# bnl-diskpt1% ip link add link eth2 name eth2.116 type vlan id 116
# bnl-diskpt1% ifconfig eth2.116 192.168.188.2/24
# lbl-diskpt1% ping 192.168.188.2
