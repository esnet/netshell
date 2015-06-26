#! /bin/sh
#
# ENOS, Copyright (c) 2015, The Regents of the University of California,
# through Lawrence Berkeley National Laboratory (subject to receipt of
# any required approvals from the U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this software,
# please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
#
# NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
# the U.S. Government has been granted for itself and others acting on its
# behalf a paid-up, nonexclusive, irrevocable, worldwide license in the
# Software to reproduce, prepare derivative works, and perform publicly and
# display publicly.  Beginning five (5) years after the date permission to
# assert copyright is obtained from the U.S. Department of Energy, and subject
# to any subsequent five (5) year renewals, the U.S. Government is granted
# for itself and others acting on its behalf a paid-up, nonexclusive,
# irrevocable, worldwide license in the Software to reproduce, prepare
# derivative works, distribute copies to the public, perform publicly and
# display publicly, and to permit others to do so.
#
# Run this script from the top level of an Open Daylight Karaf distribution.
#
# Open Daylight uses a Karaf configuration that essentially disables
# the Maven repository cache (by default found in ~/.m2/repository).
# Because the ODL distribution contains all the OSGi bundles needed
# to run anyway, there's no point in keeping an additional cache around
# because that just means storing another copy of all the bundles
# on the local disk.
#
# For ENOS development this doesn't work.  We build the Netshell and
# ENOS modules from source, at which point the OSGi bundles land in
# the local Maven repository.  A stock ODL distribution either won't
# be able to find our modules or won't know to look for updated
# versions.
#
# Therefore in this environment we want to re-enable the cache.
# The easiest way to do this is to not explicitly set
# org.ops4j.pax.url.mvn.localRepository and let Karaf just do its
# default behavior.  This script makes that change.
#
sed -i.bak -E -e 's/^(org\.ops4j\.pax\.url\.mvn\.localRepository=.*)/#&/' etc/org.ops4j.pax.url.mvn.cfg
