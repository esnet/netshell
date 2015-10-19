netshell
========

Kernel and companion software that support ENOS applications.

NetShell Quickstart
-------------------

These steps install NetShell into a generic Karaf container.  For use with a customized
Karaf container that is an artifact of an OpenDaylight integration build, see the "OpenDaylight
Integration" section of this document.

1.  Build and install netshell-kernel (and netshell-python if desired, and netshell-odl if applicable).
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

In some cases, slightly different steps are needed depending on the version of OpenDaylight being
used.  (As of this writing, some success has been had with both ODL Helium and ODL Lithium.  
ODL Hydrogen is not supported.)  These differences will be noted where necessary.

1.  Download an ODL Karaf distribution from the OpenDaylight Web site.
    As mentioned above, both ODL Helium and ODL Lithium have been tested, as of this writing.  Unpack it.

2.  From the top-level directory of the ODL Karaf distribution, execute the ```fixup-karaf.sh```
    script found in the top-level directory of this source tree.
    This change restores the default search behavior for finding bundles in Maven
    repositories (in particular it's needed to read the NetShell bundles from the local Maven
    repository / cache).

3.  In some circumstances, ODL does not play well with the NetShell security manager (the exact conditions
    are not completely known).  If this is
    believed to be a problem, the security manager can be disabled by creating a ```netshell.json```
    file 
    in the top level of the ODL Karaf installation and setting the value of the
    ```securityManagerDisabled``` parameter to 1.

4.  Start up the ODL Karaf container from the top-level directory of the ODL Karaf installation with ```bin/karaf```.

5.  Within the Karaf instance, load the ODL features of interest, such as OpenFlow support and the
    DLUX GUI:

        feature:install odl-dlux-core odl-openflowplugin-all
        
    On ODL Lithium, it might also be necessary to load some additional DLUX modules:
    
        feature:install odl-dlux-all
        
    Note that on ODL Helium the default URL for the DLUX interface is:
    
        http://localhost:8181/dlux/index.html
        
    This URL has changed for ODL Lithium:
    
        http://localhost:8181/index.html

5.  Features necessary for NetShell integration can be loaded as follows on ODL Helium:

        feature:install odl-adsal-compatibility odl-nsf-managers
        
    On ODL Lithium, it is necessary to do this instead:
    
        feature:install odl-openflowplugin-adsal-compatibility odl-nsf-managers

6.  To make the embedded SSH server start up correctly, it is necessary to refresh the bindings of one
    of the bundles.

        bundle:refresh -f org.apache.sshd.core

    This is necessary so that the org.apache.sshd.core contains correct bindings for
    the org.apache.mina.service package.  These bindings are necessary for NetShell's embedded SSH
    server; failure to get this right results in a a runtime exception at NetShell startup time.

7.  Follow the instructions in "NetShell Quickstart" above to load and run the base NetShell modules.

8.  To load the NetShell OpenDaylight AD-SAL bundle:

        feature:install netshell-controller
        feature:install netshell-odl
        
    NOTE:  NetShell and ENOS are moving to a model that uses OpenDaylight's MD-SAL APIs, and where
    the netshell-controller module sits atop the netshell-odl-mdsal and netshell-odl-corsa
    modules.  To deploy in a way compatible with that approach, do this instead:
    
        feature:install netshell-odl-mdsal
        feature:install netshell-controller

