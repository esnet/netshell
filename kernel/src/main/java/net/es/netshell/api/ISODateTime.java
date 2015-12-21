/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
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
 */
package net.es.netshell.api;

import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;

/**
 * Created by lomax on 5/28/14.
 */
public class ISODateTime implements Comparable {
    private String isoDateTime;
    private DateTime dateTime;

    public ISODateTime() {

    }

    public ISODateTime(String utc) {
        // This DateTime constructor takes a local timestamp not an UTC. Timestamps are in milliseconds
        this.dateTime = new DateTime(Long.parseLong(utc + "000"));
        this.isoDateTime = this.dateTime.toString();
    }

    public String getIsoDateTime() {
        return this.isoDateTime;
    }

    public void setIsoDateTime(String isoDateTime) {
        this.isoDateTime = isoDateTime;
        this.dateTime = DateTime.parse(isoDateTime);
    }

    public String toString() {
        return this.isoDateTime;
    }

    public DateTime toDateTime() {
        return this.dateTime;
    }

    @Override
    public int compareTo(Object o) {
        return this.dateTime.compareTo((ReadableInstant) o);
    }
}
