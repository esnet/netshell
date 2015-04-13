netshell
========

Kernel and companion software that support ENOS applications.

NetShell Quickstart
-------------------

These steps install NetShell into a generic Karaf container.  For use with a customized
Karaf container that is an artifact of an OpenDaylight integration build, see the "OpenDaylight
Integration" section of this document.

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

Like NetShell, OpenDaylight (as of the Helium release) is composed of a number of OSGi features and bundles
running inside a Karaf container.  However, OpenDaylight (henceforth referred to as ODL) and NetShell require some
"encouragement" to play together.  For the most part, this is because ODL is distributed in a somewhat
customized Karaf container, and some of those customizations are incompatible with NetShell.  The approach
to integration is basically to start with a ODL Karaf container and tweak it a bit so that NetShell
can run inside it.

There are two starting points.  One uses a stock ODL distribution.  The other uses a custom-built ODL
integration build; this latter approach has the virtue of requiring less deploy-time customization (it also
allows working with an ODL snapshot or with an updated Karaf runtime).

If using a stock ODL distribution:

1.  Download an ODL Karaf distribution from the OpenDaylight Web site.
    As of this writing the current version is Helium-SR2.  Unpack it.

2.  Also download and unpack a stock version of Karaf corresponding to the ODL distribution.  As of this
    writing, Helium-SR2 was packaged in a Karaf 3.0.1 container.

3.  Copy the file ```etc/org.ops4j.pax.url.mvn.cfg``` from the stock Karaf distribution into the ODL
    distribution.  This change restores the default search behavior for finding bundles in Maven
    repositories (in particular it's needed to read the NetShell bundles from the local Maven
    repository / cache).

If using a custom-build ODL integration build:

1.  Download the custom ODL build.  This build will have the stock ```etc/ops4j.pax.url.mvn.cfg``` file
    mentioned above.  It may also have a newer Karaf and / or newer ODL components.  As of this writing,
    bmah@es.net has a build that is a snapshot of what will become Helium-SR3, using Karaf 3.0.2.

The following steps are required regardless of the version of ODL used as a starting point:

1.  Edit ```etc/custom.properties``` and note the definition of ```org.osgi.framework.system.capabilities```.
    If running with Karaf 3.0.2 or later, this definition should be commented out; it was apparently a
    workaround for a bug in an older version of Karaf.

2.  In some circumstances, ODL does not play well with the NetShell security manager (the exact conditions
    are not completely known).  If this is
    believed to be a problem, the security manager can be disabled by creating a ```netshell.json```
    file 
    in the top level of the ODL Karaf installation and setting the value of the
    ```securityManagerDisabled``` parameter to 1.

3.  Start up the ODL Karaf container from the top-level directory of the ODL Karaf installation with ```bin/karaf```.

4.  Within the Karaf instance, load the ODL features of interest.  A typical set of features that provides a
    layer-2 learning bridge plus the DLUX GUI is:

        feature:install odl-dlux-core odl-openflowplugin-all 

5.  Features necessary for NetShell integration can be loaded as follows:

        feature:install odl-adsal-compatibility odl-nsf-managers

6.  To make the embedded SSH server start up correctly, it is necessary to refresh the bindings of one
    of the bundles.

        bundle:refresh -f org.apache.sshd.core

    This is necessary so that the org.apache.sshd.core contains correct bindings for
    the org.apache.mina.service package.  These bindings are necessary for NetShell's embedded SSH
    server; failure to get this right results in a a runtime exception at NetShell startup time.

7.  Follow the instructions in "NetShell Quickstart" above to load and run the base NetShell modules.

8.  To load the NetShell OpenDaylight bundle (at least until a feature is created for it):

        bundle:install mvn:mvn:net.es/netshell-odl/1.0-SNAPSHOT



