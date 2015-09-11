package net.es.netshell.odlcorsa.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

/**
* NetShell ODL Corsa/SDX3 Integration
*/
public class OdlCorsaModule extends net.es.netshell.odlcorsa.impl.AbstractOdlCorsaModule {
    public OdlCorsaModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OdlCorsaModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, net.es.netshell.odlcorsa.impl.OdlCorsaModule oldModule, java.lang.AutoCloseable oldInstance) {
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

        OdlCorsaImpl odlCorsaImpl = new OdlCorsaImpl(dataBrokerService, notificationService, rpcRegistryDependency);
        return odlCorsaImpl;

    }

}
