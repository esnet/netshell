import sys
# Need to customize this path according to deployment?
sys.path.append('/var/netshell/lib')

# Fix for not having $JARFILE/Lib in path
from org.python.util import jython
# We should really do os.path.join(jython()..., 'Lib') here but we can't
# import os;
sys.path.append(jython().getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + '/Lib')
