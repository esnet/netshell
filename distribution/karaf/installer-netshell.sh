#! /bin/sh
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

#
# ENOS/Netshell install / restart script.
# Takes one argument, which is the pathname to a Karaf distribution
#
KARAF_EXEC="bin/client -u karaf"
UNTAR="tar -xvf"
UNZIP="unzip"

KARAF_DISTRIBUTION=$1
if [ "x$1" == "x" ]; then
  echo "Must provide pathname to Karaf distribution"
  exit 1
fi
KARAF_DIR=`echo $KARAF_DISTRIBUTION | sed -e 's,.*/,,' | sed -E 's/(.zip|.tar.gz)$//'`

#
# Locate and kill old Karafs...
#
echo "Kill Netshell Karaf..."
pids=`pidof java`
for pid in $pids; do
  if ps -hwww $pid | grep karaf; then
    # With extreme prejudice...
    kill -9 $pid
  fi
done
echo "done"

#
# Clear out old Karaf
#
echo -n "Removing Netshell Karaf..."
rm -rf ${KARAF_DIR}
echo "done"

#
# Unpack Karaf
#
echo "Unpack $KARAF_DISTRIBUTION..."
if echo $KARAF_DISTRIBUTION | grep -e '.zip$' > /dev/null; then
  $UNZIP $KARAF_DISTRIBUTION
elif echo $KARAF_DISTRIBUTION | grep -e '.tar.gz$' > /dev/null; then
  $UNTAR $KARAF_DISTRIBUTION
else
  echo "unknown distribution type"
  exit 1
fi

cd ${KARAF_DIR}
echo "done"

#
# Karaf Pre-Startup Fixups
#
# Force the use of the Eclipse Equinox OSGi framework (rather than the default Apache Felix).
# Equinox is the default for Open Daylight.  The two frameworks have at least one important
# difference, that their framework BundleProtectionDomain express different formats of URIs
# for the path to an OSGi bundle.  Felix's implementation returns a URI that reflects how a
# bundle was installed into the OSGi container (for example "mvn:org.example/...").
# Equinox's class reflects a path on the local filesystem where the bundle contents
# are cached.
#
# This difference has an implication for our Jython support.  To locate certain Python
# files included in the Jython OSGi bundle, we need to be able to find the path to the
# Jython JAR file.  This exists in the Karaf's cache, but only the Equinox framework
# gives us the path we need to find it.  There does not appear to be a way to resolve
# the URI returned by Felix's BundleProtectionDomain into a pathname on the local filesystem,
# so we need to force the use of Equinox.
#
echo -n "Patching custom.properties..."
echo "karaf.framework=equinox" >> etc/custom.properties
echo "done"

#
# Allow the use of the local .m2/repository Maven cache, extremely useful if not outright required
# for developers writing OSGi bundles to use with Netshell.
#
# XXX This section is a no-op because the definition of org.ops4j.pax.url.mvn.defaultLocalRepoAsRemote
# is commented out in the default Karaf configuration.
#
echo -n "Patching org.ops4j.pax.url.mvn.cfg..."
sed -i.bak -E -e 's/^(org\.ops4j\.pax\.url\.mvn\.defaultLocalRepoAsRemote=)(.*)/\1true/' etc/org.ops4j.pax.url.mvn.cfg

#
# Change OSGi server port numbers for this Karaf instance to not collide with the ports being used
# by an ODL instance (if any) running in a separate Karaf instance running on the same host.
#
echo -n "Patching org.apache.karaf.management.cfg..."
sed -i.bak -E -e 's/(rmiRegistryPort\s*=\s*)(.*)/\11098/' -e 's/(rmiServerPort\s*=\s*)(.*)/\144443/' etc/org.apache.karaf.management.cfg
echo "done"
echo -n "Patching org.apache.karaf.shell.cfg..."
sed -i.bak -E -e 's/(sshPort\s*=\s*)(.*)/\18100/' etc/org.apache.karaf.shell.cfg
echo "done"

#
# Start Karaf
#
echo -n "Starting karaf in background..."
nohup bin/karaf server > nohup.out &
echo "done"

#
# Wait for karaf to start
#
echo -n "Waiting for karaf to start..."
until ${KARAF_EXEC} version; do
  sleep 5
done
echo "done"

#
# Karaf Post-Startup Package Installation
#
declare -a commands=(
"+++"

# Force a refresh of the following bundle's bindings, principally so it can find some
# Apache MINA packages.  If we don't do this before installing our netshell-kernel
# bundle, the bundle activator will fail.
"bundle:refresh -f org.apache.sshd.core"
"+++"

"feature:repo-add mvn:net.es.netshell/netshell-features/1.0.0-SNAPSHOT/xml/features"
"feature:install netshell-kernel netshell-python"
"feature:install netshell-controller"

"feature:repo-add mvn:net.es.enos/enos-features/1.0.0-SNAPSHOT/xml/features"
"feature:install enos-esnet"
)

numcommands=${#commands[@]}
for (( i=0; i<${numcommands}; i++ ));
do
  command=${commands[$i]}
  echo "COMMAND:  ${command}"
  if [ "x$command" != "x" ]; then
    if [ "$command" == "+++" ]; then
      echo -n "Waiting for karaf availability..."
      until ${KARAF_EXEC} version; do
        sleep 5
      done
      echo "done"
    else
      echo "Execute: " "$command"
      ${KARAF_EXEC} "$command"
      if [ "$?" -ne 0 ]; then
          echo "ERROR EXIT"
          exit 1
      fi
    fi
  fi
done

