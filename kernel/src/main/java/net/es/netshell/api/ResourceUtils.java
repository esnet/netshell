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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper methods to manipulate Resources.
 */
public class ResourceUtils {


    /**
     * Verifies that a name is valid for a ResourceName. ResourceName can be used as part of an URL
     * @param name
     * @return
     */
    public static boolean isValidResourceName(String name) {
        if (name == null) {
            // This can happen when creating a new Resource
            return true;
        }
        Pattern pattern = Pattern.compile("[<>?]");
        Matcher matcher = pattern.matcher(name);
        return ! matcher.find();
    }

    /**
     * Special handling of characters such as *
     * @param name
     * @return
     */
    public static String normalizeResourceName (String name) {
        String tmp = name.replace("*","_any_");
        return tmp;
    }

    public static String parseResourceName (String name) {
        String tmp = name.replace("_any_","*");
        return tmp;
    }


}
