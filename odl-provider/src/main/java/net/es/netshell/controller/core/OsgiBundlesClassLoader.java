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

package net.es.netshell.controller.core;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bmah on 12/18/14.
 */
public class OsgiBundlesClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(OsgiBundlesClassLoader.class);
    private Bundle[] bundles;

    public OsgiBundlesClassLoader(Bundle [] buns, ClassLoader parent) {
        super(parent);
        bundles = buns;

        for (Bundle b : buns) {
            logger.debug("Looking at bundle {}", b.getSymbolicName());
        }
    }

    public Class findClass(String className) throws ClassNotFoundException {
        try {
            // First try the default class loader
            return super.findClass(className);
        }
        catch (ClassNotFoundException e) {
            // Look for the class from the bundles.
            // XXX Security issue here?
            for (Bundle b : bundles) {
                try {
                    return b.loadClass(className);
                }
                catch (ClassNotFoundException e2) {
                    // ignore
                }
            }
            // Can't find the class, re-throw the exception
            throw e;
        }
    }

}
