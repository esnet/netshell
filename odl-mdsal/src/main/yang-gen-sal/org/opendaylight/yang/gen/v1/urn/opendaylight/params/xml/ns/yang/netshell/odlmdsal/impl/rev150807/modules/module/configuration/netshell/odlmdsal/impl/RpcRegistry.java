package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.ServiceRef;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.Module;


/**
 * <p>This class represents the following YANG schema fragment defined in module <b>netshell-odlmdsal-impl</b>
 * <br />(Source path: <i>META-INF/yang/netshell-odlmdsal-impl.yang</i>):
 * <pre>
 * container rpc-registry {
 *     leaf type {
 *         type service-type-ref;
 *     }
 *     leaf name {
 *         type leafref;
 *     }
 *     uses service-ref {
 *         refine (urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)type {
 *             leaf type {
 *                 type service-type-ref;
 *             }
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>netshell-odlmdsal-impl/modules/module/configuration/(urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl?revision=2015-08-07)netshell-odlmdsal-impl/rpc-registry</i>
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.RpcRegistryBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.RpcRegistryBuilder
 */
public interface RpcRegistry
    extends
    ChildOf<Module>,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netshell.odlmdsal.impl.rev150807.modules.module.configuration.netshell.odlmdsal.impl.RpcRegistry>,
    ServiceRef
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:netshell-odlmdsal:impl","2015-08-07","rpc-registry");;


}

