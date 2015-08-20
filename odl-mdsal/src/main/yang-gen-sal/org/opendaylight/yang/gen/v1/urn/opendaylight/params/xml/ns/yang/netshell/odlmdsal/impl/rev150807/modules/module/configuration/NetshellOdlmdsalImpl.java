package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.RpcRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.module.Configuration;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>netshell-odlmdsal-impl</b>
 * <br />(Source path: <i>META-INF/yang/netshell-odlmdsal-impl.yang</i>):
 * <pre>
 * case netshell-odlmdsal-impl {
 *     container rpc-registry {
 *         leaf type {
 *             type service-type-ref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)type {
 *                 leaf type {
 *                     type service-type-ref;
 *                 }
 *             }
 *         }
 *     }
 *     container notification-service {
 *         leaf type {
 *             type service-type-ref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)type {
 *                 leaf type {
 *                     type service-type-ref;
 *                 }
 *             }
 *         }
 *     }
 *     container data-broker {
 *         leaf type {
 *             type service-type-ref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)type {
 *                 leaf type {
 *                     type service-type-ref;
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>netshell-odlmdsal-impl/modules/module/configuration/(urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)netshell-odlmdsal-impl</i>
 */
public interface NetshellOdlmdsalImpl
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.NetshellOdlmdsalImpl>,
    Configuration
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl","2015-08-07","netshell-odlmdsal-impl");;

    RpcRegistry getRpcRegistry();
    
    NotificationService getNotificationService();
    
    DataBroker getDataBroker();

}

