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

/**
 * Opaque handle to a flow entry.
 * Objects implementing this interface have a portable (controller-neutral)
 * reference to a live flow on a NetFlow controller.
 *
 * The flow handle can be either "valid" or not.  The validity bit allows us
 * to keep references to the flow handle in multiple data structures (for
 * example lists corresponding to end hosts affected by the flow entry) but
 * not have to explicitly purge all those references if the flow is deleted.
 */
public interface SdnControllerClientFlowHandle {
    /**
     * Check the validity flag of this flow handle.
     * @return boolean true if the flow handle is validf
     */
    public boolean isValid();

    /**
     * Invalidate the flow handle, presumably because the corresponding flow
     * has been deleted.
     */
    public void invalidate();
}
