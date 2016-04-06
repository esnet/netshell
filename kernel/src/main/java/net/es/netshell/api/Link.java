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
 * Generic Link class
 */

@ResourceType(
        type=ResourceTypes.LINK
)
public class Link extends  Resource {
    public enum Types {SITE,PEERING,INTERNAL}
    public static final String LINKS_DIR = "links";
    protected double  weight;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Link(Link link) {
        super(link);
    }

    public Link(String linkName) {
        super(linkName);
    }

    public Link() {
        super();
    }

    public static String buildName(String srcNodeName, String srcPortName,
                                   String dstNodeName, String dstPortName) {
        return srcNodeName + "--" + srcPortName + "--" + dstNodeName + "--" + dstPortName;
    }

    public static String nameToSrcNode(String name) {
        String[] v = name.split("--");
        return v[0];
    }

    public static String nameToSrcPort(String name) {
        String[] v = name.split("--");
        return v[1];
    }

    public static String nameToDstNode(String name) {
        String[] v = name.split("--");
        return v[2];
    }

    public static String nameToDstPort(String name) {
        String[] v = name.split("--");
        return v[3];
    }

}

