#!/bin/python
#
# ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
# of the University of California, through Lawrence Berkeley National
# Laboratory (subject to receipt of any required approvals from the
# U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this
# software, please contact Berkeley Lab's Innovation & Partnerships
# Office at IPO@lbl.gov.
#
# NOTICE.  This Software was developed under funding from the
# U.S. Department of Energy and the U.S. Government consequently retains
# certain rights. As such, the U.S. Government has been granted for
# itself and others acting on its behalf a paid-up, nonexclusive,
# irrevocable, worldwide license in the Software to reproduce,
# distribute copies to the public, prepare derivative works, and perform
# publicly and display publicly, and to permit other to do so.
#

import sys
from net.es.netshell.boot import BootStrap
from net.es.netshell.kernel.exec import KernelThread
from java.nio.file import Paths
from org.python.util import jython

def realPathName(path):
    return Paths.get(sys.netshell_root.toString(), path)

def pwd():
    return sys.netshell_root

def cd(path):
    sys.netshell_root = realPathName(path)

sys.netshell_root = BootStrap.rootPath

# Fix for not having $JARFILE/Lib in path
jythonjar = jython().getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
if not jythonjar + "/Lib" in sys.path:
    # We should really do os.path.join(jython()..., 'Lib') here but we can't
    # import os (chicken & egg)
    sys.path.append(jythonjar + '/Lib')
    sys.path.append(jythonjar + '/Lib/site-packages')

if not sys.netshell_root.toString() + "/lib" in sys.path:
	sys.path.append (sys.netshell_root.toString() + "/lib")

# Make sure user home directory is in the path
homepath = KernelThread.currentKernelThread().getUser().getHomePath().toString()
if homepath not in sys.path:
    sys.path = sys.path + [ homepath ]

