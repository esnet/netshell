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
