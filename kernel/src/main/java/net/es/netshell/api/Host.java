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

/**
 * Mostly a stub for now. Perhaps this class is not useful
 */
public class Host extends Node {

    private String name;

    public Host ()  { super ();}

    public Host (String name) {
        super(name);
        this.name = name;
    }

    public Host(Host host) {
        super(host);
        this.name = host.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.setResourceName(name);
    }
}
