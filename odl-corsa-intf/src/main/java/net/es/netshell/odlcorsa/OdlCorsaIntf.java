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
 *
 */

package net.es.netshell.odlcorsa;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * Created by bmah on 12/22/15.
 */
public interface OdlCorsaIntf {

    public void deleteFlow(FlowRef flowRef) throws InterruptedException, ExecutionException;

    public FlowRef createTransitVlanMacCircuit(Node odlNode, int priority, BigInteger c,
                                               MacAddress m1, NodeConnectorId ncid1, int vlan1,
                                               MacAddress m2, NodeConnectorId ncid2, int vlan2,
                                               short vp2, short q2, long mt2)
            throws InterruptedException, ExecutionException;

    public FlowRef sendVlanMacToController(Node odlNode, int priority, BigInteger c,
                                           MacAddress m1, NodeConnectorId ncid1, int vlan1)
            throws InterruptedException, ExecutionException;

    public MeterRef createGreenMeter(Node odlNode, long meter)
            throws InterruptedException, ExecutionException;

    public MeterRef createGreenYellowMeter(Node odlNode, long meter, long cr, long cbs)
            throws InterruptedException, ExecutionException;

    public MeterRef createGreenRedMeter(Node odlNode, long meter, long er, long ebs)
            throws InterruptedException, ExecutionException;

    public MeterRef createGreenYellowRedMeter(Node odlNode, long meter, long cr, long cbs, long er, long ebs)
            throws InterruptedException, ExecutionException;

    public boolean deleteMeter(MeterRef meterRef) throws InterruptedException, ExecutionException;

}
