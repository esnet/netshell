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
from java.nio.file import Paths

def realPathName(path):
    return Paths.get(sys.netshell_root.toString(), path)

def pwd():
    return sys.netshell_root

def cd(path):
    sys.netshell_root = realPathName(path)

sys.netshell_root = BootStrap.rootPath

# Fix for not having $JARFILE/Lib in path
if not sys.netshell_root.toString() + "/lib" in sys.path:
    from org.python.util import jython
    # We should really do os.path.join(jython()..., 'Lib') here but we can't
    # import os (chicken & egg)
    path=jython().getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
    sys.path.append(path + '/Lib')
    sys.path.append(path + '/Lib/site-packages')