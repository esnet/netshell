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

import org.jgrapht.GraphPath;
import org.joda.time.DateTime;

/**
 * Created by lomax on 6/12/14.
 */
public class Path  {
    private DateTime start;
    private DateTime end;
    private GraphPath graphPath;
    private long maxReservable;

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public GraphPath getGraphPath() {
        return graphPath;
    }

    public void setGraphPath(GraphPath graphPath) {
        this.graphPath = graphPath;
    }

    public long getMaxReservable() {
        return maxReservable;
    }

    public void setMaxReservable(long maxReservable) {
        this.maxReservable = maxReservable;
    }
}

