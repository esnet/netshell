/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2016, The Regents
 * of the University of California, through Lawrence Berkeley National
 * Laboratory (subject to receipt of any required approvals from the
 * U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this
 * software, please contact Berkeley Lab's Innovation & Partnerships
 * Office at IPO@lbl.gov.
 *
 * NOTICE.  This Software was developed under funding from the
 * U.S. Department of Energy and the U.S. Government consequently retains
 * certain rights. As such, the U.S. Government has been granted for
 * itself and others acting on its behalf a paid-up, nonexclusive,
 * irrevocable, worldwide license in the Software to reproduce,
 * distribute copies to the public, prepare derivative works, and perform
 * publicly and display publicly, and to permit other to do so.
 *
 */

package net.es.netshell.controller.intf;

import java.util.Arrays;

/**
 * Cooked Ethernet frame class
 *
 * Amazingly, OpenDaylight MD-SAL doesn't seem to have any packet parsing
 * utilities.  This is a very simple Ethernet frame parser that
 * understands IEEE 802.1q VLAN tagging (but not QinQ).  It can convert
 * packets from their raw (array of bytes) representation to and from a
 * cooked EthernetFrame object.
 */
public class EthernetFrame {

    // Sizes of things
    public static final int MIN_ETHERNET_HEADER_SIZE = 14;
    public static final int MAC_ADDRESS_SIZE = 6;
    public static final int ETHERTYPE_SIZE = 2;
    public static final int VLAN_HEADER_SIZE = 4;
    public static final int VLAN_TCI_SIZE = 2;

    // Positions of things (where known)
    public static final int DST_MAC_START = 0;
    public static final int SRC_MAC_START = DST_MAC_START + MAC_ADDRESS_SIZE;

    public static final int BASE_ETHERTYPE_START = SRC_MAC_START + MAC_ADDRESS_SIZE;
    public static final int BASE_PAYLOAD_START = BASE_ETHERTYPE_START + ETHERTYPE_SIZE;

    public static final int VLAN_8021Q_START = SRC_MAC_START + MAC_ADDRESS_SIZE;
    public static final int VLAN_8021Q_TCI_START = VLAN_8021Q_START + ETHERTYPE_SIZE;
    public static final int VLAN_ETHERTYPE_START = VLAN_8021Q_START + VLAN_HEADER_SIZE;
    public static final int VLAN_PAYLOAD_START = VLAN_ETHERTYPE_START + ETHERTYPE_SIZE;

    // Other interesting constants
    public static int ETHERTYPE_IPV4 = 0x0800;
    public static int ETHERTYPE_ARP = 0x0806;
    public static int ETHERTYPE_VLAN = 0x8100; // VLAN TPID
    public static int ETHERTYPE_IPV6 = 0x86dd;
    public static int ETHERTYPE_LLDP = 0x88cc;

    // Parts of an Ethernet frame
    private byte[] dstMac;
    private byte[] srcMac;
    private int etherType;
    private int pcp;
    private int dei;
    private int vid;
    private byte[] payload;

    public byte[] getDstMac() {
        return dstMac;
    }

    public void setDstMac(byte[] dstMac) {
        this.dstMac = dstMac;
    }

    public byte[] getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(byte[] srcMac) {
        this.srcMac = srcMac;
    }

    public int getEtherType() {
        return etherType;
    }

    public void setEtherType(int etherType) {
        this.etherType = etherType;
    }

    public int getPcp() {
        return pcp;
    }

    public void setPcp(int pcp) {
        this.pcp = pcp;
    }

    public int getDei() {
        return dei;
    }

    public void setDei(int dei) {
        this.dei = dei;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public static EthernetFrame packetToFrame(final byte [] bytes) {

        if (bytes == null || bytes.length < MIN_ETHERNET_HEADER_SIZE) {
            return null;
        }

        EthernetFrame f = new EthernetFrame();

        // Get the source and destination MAC addresses; these are easy
        f.dstMac = Arrays.copyOfRange(bytes, DST_MAC_START, DST_MAC_START + MAC_ADDRESS_SIZE);
        f.srcMac = Arrays.copyOfRange(bytes, SRC_MAC_START, SRC_MAC_START + MAC_ADDRESS_SIZE);

        // Grab what we think is the packet's EtherType.  We have different code processing
        // paths depending on whether it's a VLAN tagged packet or not.
        byte[] etBytes = Arrays.copyOfRange(bytes, BASE_ETHERTYPE_START, BASE_ETHERTYPE_START + ETHERTYPE_SIZE);
        int et = ((etBytes[0] & 0xff) << 8) + (etBytes[1] & 0xff);

        if (et == ETHERTYPE_VLAN) {
            // VLAN tagged packet, throw away the TPID and then fill in the "real" etherType,
            // plus VLAN fields.
            etBytes =  Arrays.copyOfRange(bytes, VLAN_ETHERTYPE_START, VLAN_ETHERTYPE_START + ETHERTYPE_SIZE);
            f.etherType = ((etBytes[0] & 0xff) << 8) + (etBytes[1] & 0xff);

            byte[] tciBytes = Arrays.copyOfRange(bytes, VLAN_8021Q_TCI_START, VLAN_8021Q_TCI_START + VLAN_TCI_SIZE);
            int tci = ((tciBytes[0] & 0xff) << 8) + (tciBytes[1] & 0xff);
            f.pcp = (tci >> 13) & 0x7;
            f.dei = (tci >> 12) & 0x1;
            f.vid = tci & 0xfff;
            f.payload = Arrays.copyOfRange(bytes, VLAN_PAYLOAD_START, bytes.length);
        }
        else {
            // No VLAN tag, so use the value we just got as the EtherType, clear the VLAN fields.
            f.etherType = et;
            f.pcp = 0;
            f.dei = 0;
            f.vid = 0;
            f.payload = Arrays.copyOfRange(bytes, BASE_PAYLOAD_START, bytes.length);
        }

        return f;
    }

    public byte [] toPacket() {
        // Compute the correct packet size, depending on whether there's a VLAN tag.
        int packetLength = MIN_ETHERNET_HEADER_SIZE + this.payload.length;
        if (this.vid != 0) {
            packetLength += VLAN_HEADER_SIZE;
        }

        // Make a space for the packet
        byte[] bytes = new byte[packetLength];
        // Copy source and destination Ethernet headers
        System.arraycopy(this.dstMac, 0, bytes, DST_MAC_START, MAC_ADDRESS_SIZE);
        System.arraycopy(this.srcMac, 0, bytes, SRC_MAC_START, MAC_ADDRESS_SIZE);

        // Now figure out if this packet needs a VLAN frame.
        if (this.vid != 0) {
            bytes[VLAN_8021Q_START] = (byte) ((ETHERTYPE_VLAN >> 8) & 0xff);
            bytes[VLAN_8021Q_START + 1] = (byte) (ETHERTYPE_VLAN & 0xff);
            int tci = (this.pcp & 0x7) << 13 |
                    (this.dei & 0x1) << 12 |
                    (this.vid & 0xfff);
            bytes[VLAN_8021Q_TCI_START] = (byte) ((tci >> 8) & 0xff);
            bytes[VLAN_8021Q_TCI_START + 1] = (byte) (tci & 0xff);
            bytes[VLAN_ETHERTYPE_START] = (byte) ((this.etherType >> 8) & 0xff);
            bytes[VLAN_ETHERTYPE_START + 1] = (byte) (this.etherType & 0xff);
            System.arraycopy(this.payload, 0, bytes, VLAN_PAYLOAD_START, this.payload.length);
        }
        else {
            bytes[BASE_ETHERTYPE_START] = (byte) ((this.etherType >> 8) & 0xff);
            bytes[BASE_ETHERTYPE_START + 1] = (byte) (this.etherType & 0xff);
            System.arraycopy(this.payload, 0, bytes, BASE_PAYLOAD_START, this.payload.length);
        }
        return bytes;
    }

    /**
     * Utility function to get a human-readable version of a MAC address
     * @param bytes array of bytes
     * @return string representation
     */
    public static String byteString(final byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format(":%02x", b));
        }
        return sb.substring(1);
    }


}
