/*
* Generated file
*
* Generated from: yang module name: netshell-odlmdsal-impl yang module local name: netshell-odlmdsal-impl
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Thu Aug 20 10:02:26 PDT 2015
*
* Do not modify this file unless it is present under src/main directory
*/
package net.es.netshell.odlmdsal.impl;
/**
* NetShell ODL MD-SAL integration
*/
public interface OdlMdsalModuleMXBean {
    public javax.management.ObjectName getNotificationService();

    public void setNotificationService(javax.management.ObjectName notificationService);

    public javax.management.ObjectName getRpcRegistry();

    public void setRpcRegistry(javax.management.ObjectName rpcRegistry);

    public javax.management.ObjectName getDataBroker();

    public void setDataBroker(javax.management.ObjectName dataBroker);

}