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

/**
 * Created by bmah on 1/7/16.
 */
public class SdnInstallMeterRequest extends SdnRequest {
    @JsonIgnore
    public static String TYPE = "SdnInstallMeterRequest";

    public byte [] dpid;
    public long meter;
    public long cr;
    public long cbs;
    public long er;
    public long ebs;

    public byte[] getDpid() {
        return dpid;
    }

    public void setDpid(byte[] dpid) {
        this.dpid = dpid;
    }

    public long getMeter() {
        return meter;
    }

    public void setMeter(long meter) {
        this.meter = meter;
    }

    public long getCr() {
        return cr;
    }

    public void setCr(long cr) {
        this.cr = cr;
    }

    public long getCbs() {
        return cbs;
    }

    public void setCbs(long cbs) {
        this.cbs = cbs;
    }

    public long getEr() {
        return er;
    }

    public void setEr(long er) {
        this.er = er;
    }

    public long getEbs() {
        return ebs;
    }

    public void setEbs(long ebs) {
        this.ebs = ebs;
    }

    public SdnInstallMeterRequest() {
        setRequestType(TYPE);
    }
}
