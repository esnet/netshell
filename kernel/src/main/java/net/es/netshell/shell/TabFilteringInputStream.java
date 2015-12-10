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

package net.es.netshell.shell;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lomax on 10/6/14.
 */
public class TabFilteringInputStream extends InputStream {
    private InputStream in;
    private boolean filters = false;
    private int count;

    public TabFilteringInputStream (InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (this.filters && count > 0) {
            count--;
            return (' ');
        }
        int v = this.in.read();
        if (!filters) {
            return v;
        }
        if (v == 9) {  // TAB / COMPLETE
            count = 3;
            return (' ');
        }
        return v;
    }

    public void setFilters(boolean filters) {
        this.filters = filters;
    }

    @Override
    public int available() throws IOException {
        if (this.filters && this.count > 0) {
            return this.count;
        }
        return super.available();
    }
}
