#! /bin/sh
#
# ENOS, Copyright (c) 2014, The Regents of the University of
# California, through Lawrence Berkeley National Laboratory (subject
# to receipt of any required approvals from the U.S. Dept. of Energy).
# All rights reserved.
#
# If you have questions about your rights to use or distribute this
# software, please contact Berkeley Lab's Technology Transfer
# Department at TTD@lbl.gov.
#
# NOTICE.  This software is owned by the U.S. Department of Energy.
# As such, the U.S. Government has been granted for itself and others
# acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
# license in the Software to reproduce, prepare derivative works, and
# perform publicly and display publicly.  Beginning five (5) years
# after the date permission to assert copyright is obtained from the
# U.S. Department of Energy, and subject to any subsequent five (5)
# year renewals, the U.S. Government is granted for itself and others
# acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
# license in the Software to reproduce, prepare derivative works,
# distribute copies to the public, perform publicly and display
# publicly, and to permit others to do so.
#

JAVA=java

if [ "x$NETSHELL_CONF" = "x" ]; then
    export NETSHELL_CONF=./netshell.json
fi
echo "Setting NETSHELL_CONF to $NETSHELL_CONF"
if [ "x$NETSHELL_HOME" = "x" ]; then
    export NETSHELL_HOME=$PWD
fi
echo "Setting NETSHELL_HOME to $NETSHELL_HOME"
if [ "x$NETSHELL_LOGLEVEL" = "x" ]; then
    export NETSHELL_LOGLEVEL=info
fi
echo "Setting NETSHELL_LOGLEVEL to $NETSHELL_LOGLEVEL"

SYSTEM_PROPS="-Dnetshell.configuration=${NETSHELL_CONF} -Dorg.slf4j.simpleLogger.defaultLogLevel=${NETSHELL_LOGLEVEL} -Dnetshell.rootdir=${NETSHELL_ROOTDIR} -Dorg.slf4j.simpleLogger.showDateTime=true"

$JAVA $SYSTEM_PROPS -jar $NETSHELL_HOME/target/NETSHELL-1.0-SNAPSHOT.one-jar.jar
