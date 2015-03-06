/*
 * ENOS, Copyright (c) $today.date, The Regents of the University of California, through Lawrence Berkeley National Laboratory (subject to receipt of any required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software, please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such, the U.S. Government has been granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, and perform publicly and display publicly.  Beginning five (5) years after the date permission to assert copyright is obtained from the U.S. Department of Energy, and subject to any subsequent five (5) year renewals, the U.S. Government is granted for itself and others acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software to reproduce, prepare derivative works, distribute copies to the public, perform publicly and display publicly, and to permit others to do so.
 */

package net.es.netshell.odl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;

import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Activator class for ODL support
 * Even though this class is an Activator, and OSGi bundles have Activator classes
 * as well, the activator for a ODL component is somewhat different from the
 * activator for an OSGi bundle.  In particular, the specific implementation
 * of an ODL component needs to implement different functions other than the
 * start() and stop() methods that are usually provided for OSGi.  Those
 * methods are provided actually by the base class.
 */
public class Activator extends ComponentActivatorAbstractBase {

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void init() {
        logger.info("init() called");
    }

    @Override
    public void destroy() {
        logger.info("destroy() called");
    }

    @Override
    public Object[] getImplementations() {
        logger.info("Getting implementations");

        Object[] result = {
                Controller.class,
                PacketHandler.class
        };
        return result;
    }

    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        logger.info("Configuring instance");

        if (imp.equals(PacketHandler.class)) {

            // XXX what does this do?
            Dictionary<String, Object> props = new Hashtable<>();
            props.put("salListenerName", "netshell");

            // Export interfaces
            c.setInterface(new String[]{IListenDataPacket.class.getName()}, props);

            // Register services that we depend on
            // DataPacketService is needed for encoding / decoding and sending data packets
            c.add(createContainerServiceDependency(containerName).
                    setService(IDataPacketService.class).
                    setCallbacks("setDataPacketService", "unsetDataPacketService").
                    setRequired(true));

            // FlowProgrammerService is needed for inserting flows
            c.add(createContainerServiceDependency(containerName).
                    setService(IFlowProgrammerService.class).
                    setCallbacks("setFlowProgrammerService", "unsetFlowProgrammerService").
                    setRequired(true));

            // SwitchManager service is used for enumerating switches and ports
            c.add(createContainerServiceDependency(containerName).
                    setService(ISwitchManager.class).
                    setCallbacks("setSwitchManager", "unsetSwitchManager").
                    setRequired(true));

        }
        else if (imp.equals(Controller.class)) {

            // Register services that we depend on
            // DataPacketService is needed for encoding / decoding and sending data packets
            c.add(createContainerServiceDependency(containerName).
                    setService(IDataPacketService.class).
                    setCallbacks("setDataPacketService", "unsetDataPacketService").
                    setRequired(true));

            // FlowProgrammerService is needed for inserting flows
            c.add(createContainerServiceDependency(containerName).
                    setService(IFlowProgrammerService.class).
                    setCallbacks("setFlowProgrammerService", "unsetFlowProgrammerService").
                    setRequired(true));

            // SwitchManager service is used for enumerating switches and ports
            c.add(createContainerServiceDependency(containerName).
                    setService(ISwitchManager.class).
                    setCallbacks("setSwitchManager", "unsetSwitchManager").
                    setRequired(true));

        }
    }
}
