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

import net.es.netshell.boot.BootStrap;
import net.es.netshell.kernel.exec.KernelThread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class providing methods to manipulate NetShell files
 */
public class FileUtils {

    /**
     * Normalize a file path:
     *  1) Makes sure that the path hides the host path to NetShell_ROOT
     *  2) parses . and ..
     * @param fileName The provided name is an NetShell file name, i.e, hides the host path to
     * NetShell root.
     * @return normalized pathName
     */
    static public String normalize(String fileName) {
        if (fileName == null) {
            return null;
        }
        String normalized;
		try {
			normalized = Paths.get(fileName).toFile().getCanonicalFile().toString();
	        if (normalized.startsWith(BootStrap.rootPath.toString())) {
	            normalized = normalized.substring(BootStrap.rootPath.toString().length());
	        } else {
	            if (fileName.startsWith(File.separator)) {
	                // Absolute file name
	                normalized = fileName;
	                if (fileName.length() == 1) {
	                    // Special case for "/"
	                    return fileName;
	                }
	            } else {
	                String currentDirectory = KernelThread.currentKernelThread().getCurrentDirectory();
	                if (currentDirectory == null) {
	                    // Assumes root
	                    normalized =  new File(File.separator, fileName).toString();
	                } else {
	                    normalized =  new File(currentDirectory, fileName).toString();
	                }
	            }
	        }


            // The host absolute path is needed in order to compute the canonical path
            normalized = new File(BootStrap.rootPath.toString(),
                                  normalized).getCanonicalFile().toString();
            // Prunes potential path for the host root added by the File.getCanonical has introduced.
            normalized = normalized.substring(BootStrap.rootPath.toString().length());
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return normalized;
    }

    /**
     * Returns a Path to a provided fileName that refers to a file on the host file system that can later be used
     * for performance I/O.
     * @param fileName The provided name is an NetShell file name, i.e, hides the host path to
     * NetShell root.
     * @return
     */
    static public Path toRealPath(String fileName) {
        if (fileName == null) {
            return null;
        }
        String realPathName = FileUtils.normalize(fileName);
        if ( ! Paths.get(realPathName).startsWith(BootStrap.rootPath)) {
            realPathName = new File (BootStrap.rootPath.toString(), realPathName).toString();
        }
        try {
            realPathName = new File(realPathName).getCanonicalFile().toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return Paths.get(realPathName);
    }

    /**
     * Verifies if fileName exists. The provided name is an NetShell file name, i.e, hides the host path to
     * NetShell root.
     * @param fileName The provided name is an NetShell file name, i.e, hides the host path to
     * NetShell root.
     * @return true if the file exists, false otherwise.
     */
    static public boolean exists(String fileName) {
        Path path = FileUtils.toRealPath(fileName);
        if (path == null) {
            return false;
        }
        return path.toFile().exists();
    }
}
