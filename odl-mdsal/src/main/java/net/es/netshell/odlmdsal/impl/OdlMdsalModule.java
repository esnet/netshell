/*
 * ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
 * of the University of California, through Lawrence Berkeley National
 * Laboratory (subject to receipt of any required approvals from the
 * U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this
 * software, please contact Berkeley Lab's Innovation & Partnerships
 * Office at IPO@lbl.gov.
 *
 * NOTICE.  This Software was developed under funding from the
 * U.S. Department of Energy and the U.S. Government consequently retains
 * certain rights. As such, the U.S. Government has been granted for
 * itself and others acting on its behalf a paid-up, nonexclusive,
 * irrevocable, worldwide license in the Software to reproduce,
 * distribute copies to the public, prepare derivative works, and perform
 * publicly and display publicly, and to permit other to do so.
 */

package net.es.netshell.odlmdsal.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

/**
* NetShell ODL MD-SAL integration
*/
public class OdlMdsalModule extends net.es.netshell.odlmdsal.impl.AbstractOdlMdsalModule {
    public OdlMdsalModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OdlMdsalModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, net.es.netshell.odlmdsal.impl.OdlMdsalModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        // Data broker service to interact with datastore
        DataBroker dataBrokerService = getDataBrokerDependency();

        // RPC Registry service to register RPCs
        RpcProviderRegistry rpcRegistryDependency = getRpcRegistryDependency();

        //retrieves the notification service for publishing notifications
        NotificationProviderService notificationService = getNotificationServiceDependency();

        OdlMdsalImpl odlMdsalImpl = new OdlMdsalImpl(dataBrokerService, notificationService, rpcRegistryDependency);
        return odlMdsalImpl;

    }

}
