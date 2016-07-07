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
package net.es.netshell.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hacksaw
 */
public abstract class BundleVersionServiceImpl implements BundleVersionService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public BundleVersionResource getVersion() {
        return this.load();
    }

    private BundleVersionResource load() {
        final Properties properties = new Properties();

        // Load the properties file containing our github versioning information.
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("git.properties");
        if (resourceAsStream != null) {
            try { properties.load(resourceAsStream); } catch (IOException ex) { }
        }

        if (properties.isEmpty()) {
            logger.error("Failed to load git.properties file " + this.getClass().getName());
        }

        // Load the bundle build information for the more genric POM attributes.
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        if (bundleContext != null) {
            Dictionary headers = bundleContext.getBundle().getHeaders();
            Enumeration<?> headerKeys = headers.keys();
            while (headerKeys.hasMoreElements()) {
                Object key = headerKeys.nextElement();
                properties.put(key, headers.get(key));
            }
        }

        return new BundleVersionResource(properties);
    }
}
