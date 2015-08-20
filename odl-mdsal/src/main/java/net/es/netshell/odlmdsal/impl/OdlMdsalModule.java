package net.es.netshell.odlmdsal.impl;
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

        System.out.println("Hello ODL MD-SAL");

        // TODO:implement
        throw new java.lang.UnsupportedOperationException();
    }

}
