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

import org.codehaus.jackson.annotate.JsonIgnore;

import java.math.BigInteger;

/**
 * Created by bmah on 1/7/16.
 */
public class SdnForwardRequest extends SdnRequest {

    @JsonIgnore
    public static String TYPE = "SdnForwardRequest";

    public class L2TranslationOutput {
        public String outPort;
        public int vlan;
        public String dstMac; // XXX type?
    };

    public byte [] dpid;

    public int priority;
    public BigInteger c;

    public String inPort;
    public int vlan1;
    public String srcMac1;  // XXX type?
    public String dstMac1;  // XXX type?

    public L2TranslationOutput [] outputs;

    public int pcp;
    public int queue;
    public int meter;

    public byte[] getDpid() {
        return dpid;
    }

    public void setDpid(byte[] dpid) {
        this.dpid = dpid;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BigInteger getC() {
        return c;
    }

    public void setC(BigInteger c) {
        this.c = c;
    }

    public String getInPort() {
        return inPort;
    }

    public void setInPort(String inPort) {
        this.inPort = inPort;
    }

    public int getVlan1() {
        return vlan1;
    }

    public void setVlan1(int vlan1) {
        this.vlan1 = vlan1;
    }

    public String getSrcMac1() {
        return srcMac1;
    }

    public void setSrcMac1(String srcMac1) {
        this.srcMac1 = srcMac1;
    }

    public String getDstMac1() {
        return dstMac1;
    }

    public void setDstMac1(String dstMac1) {
        this.dstMac1 = dstMac1;
    }

    public L2TranslationOutput[] getOutputs() {
        return outputs;
    }

    public void setOutputs(L2TranslationOutput[] outputs) {
        this.outputs = outputs;
    }

    public int getPcp() {
        return pcp;
    }

    public void setPcp(int pcp) {
        this.pcp = pcp;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getMeter() {
        return meter;
    }

    public void setMeter(int meter) {
        this.meter = meter;
    }

    public SdnForwardRequest() {
        setRequestType(TYPE);
    }
}
