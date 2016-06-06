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
# From fixup-karaf.sh
#
echo -n "Patching org.ops4j.pax.url.mvn.cfg..."
sed -i.bak -E -e 's/^(org\.ops4j\.pax\.url\.mvn\.defaultLocalRepoAsRemote=)(.*)/\1true/' etc/org.ops4j.pax.url.mvn.cfg

echo -n "Patching org.apache.karaf.management.cfg..."
sed -i.bak -E -e 's/(rmiRegistryPort\s*=\s*)(.*)/\11098/' -e 's/(rmiServerPort\s*=\s*)(.*)/\144443/' etc/org.apache.karaf.management.cfg
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

"feature:repo-add mvn:net.es/netshell-features/1.0.0-SNAPSHOT/xml/features"
"feature:install netshell-kernel netshell-python"
"feature:install netshell-controller"

"feature:repo-add mvn:net.es/enos-esnet/1.0-SNAPSHOT/xml/features"
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

