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
package net.es.netshell.controller.client;

import net.es.netshell.api.Resource;

public class SdnControllerClientFlowHandleImpl  extends Resource implements SdnControllerClientFlowHandle {
    public boolean valid;
    public byte[] dpid;
    public short tableId;
    public String flowId;


    public SdnControllerClientFlowHandleImpl() {
        super();
    }

    public SdnControllerClientFlowHandleImpl(String flowId) {
        super (flowId);
        this.flowId = flowId;
        this.valid = true;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return this.valid;
    }

    public byte[] getDpid() {
        return dpid;
    }

    public void setDpid(byte[] dpid) {
        this.dpid = dpid;
    }

    public short getTableId() {
        return tableId;
    }

    public void setTableId(short tableId) {
        this.tableId = tableId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.setResourceName(flowId);
        this.flowId = flowId;
    }

    public void invalidate() {
        this.valid = false;
    }
}