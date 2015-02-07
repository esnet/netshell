netshell
========

Kernel and companion software that support ENOS applications.

NetShell Quickstart
-------------------

1.  Build and install netshell-kernel (and netshell-python if desired).
    This will result in JAR artifacts being copied to the local Maven repository / cache.
    It will also result in an XML features file being copied.

2.  Start up Karaf with ```bin/karaf```.  Execute the following command to make the NetShell
    feature repository available:

        feature:repo-add mvn:net.es/netshell-kernel/1.0-SNAPSHOT/xml/features

3.  Execute the following commands as applicable to start NetShell for the first time:

        feature:install netshell-kernel
        feature:install netshell-python

OpenDaylight Integration
------------------------

1.  Start up the Karaf instance included with OpenDaylight.  Run the command ```feature:repo-list```
    and save its output to a file.  Run this output through this UNIX pipeline to get the list
    of all feature repository definitions in the form of a set of Karaf commands:

        grep mvn:org.opendaylight | awk '{print "feature:repo-add", $3}'
    Shut down the ODL Karaf instance.

    [Comment:  It sure would be nice to have another way to get this information that didn't involve
    actually starting up ODL.]

2.  Copy the ```system/org/opendaylight``` and ```system/org/openexi``` directories from the
    Karaf instance packed with the OpenDaylight distribution to the the one used for NetShell.

3.  Review ```etc/custom.properties``` from ODL and copy / modify ```etc/custom.properties```
    in the target Karaf instance accordingly.  /// Weeellll...not sure about that. ///

4.  Start up the target Karaf instance and execute the commands generated above.

5.  Within the Karaf instance, load the ODL features of interest, for example:

        feature:install odl-dlux-core odl-l2switch-all odl-l2switch-all

        

