/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.osgi;

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
            logger.info("Looking at bundle {}", b.getSymbolicName());
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