/*
 * ENOS, Copyright (c) 2015, The Regents of the University of California,
 * through Lawrence Berkeley National Laboratory (subject to receipt of any
 * required approvals from the U.S. Dept. of Energy).  All rights reserved.
 *
 * If you have questions about your rights to use or distribute this software,
 * please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
 *
 * NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
 * the U.S. Government has been granted for itself and others acting on its
 * behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
 * to reproduce, prepare derivative works, and perform publicly and display
 * publicly.  Beginning five (5) years after the date permission to assert
 * copyright is obtained from the U.S. Department of Energy, and subject to
 * any subsequent five (5) year renewals, the U.S. Government is granted for
 * itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
 * worldwide license in the Software to reproduce, prepare derivative works,
 * distribute copies to the public, perform publicly and display publicly, and
 * to permit others to do so.
 */

package net.es.netshell.odlmdsal.impl;

import com.google.common.collect.Lists;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class installs a low priority "catch-all" flow on every switch,
 * which forwards all packets that didn't get matched by some other rule
 * to the controller.
 *
 * Somewhat derived from InitialFlowWriter in the ODL's l2switch module.
 */
public class InitialFlowWriter implements OpendaylightInventoryListener {

    // ODL objects
    ExecutorService initialFlowExecutor = Executors.newCachedThreadPool();
    SalFlowService salFlowService;

    OdlMdsalImpl odlMdsalImpl;

    private short flowTableId = 0;
    private int flowPriority = 0;
    private int flowIdleTimeout = 0;
    private int flowHardTimeout = 0;

    // Logging
    static final private Logger logger = LoggerFactory.getLogger(InitialFlowWriter.class);

    public InitialFlowWriter(OdlMdsalImpl o, SalFlowService s) {
        this.odlMdsalImpl = o;
        this.salFlowService = s;
    }

    public SalFlowService getSalFlowService() {
        return salFlowService;
    }

    public void setSalFlowService(SalFlowService salFlowService) {
        this.salFlowService = salFlowService;
    }

    public short getFlowTableId() {
        return flowTableId;
    }

    public void setFlowTableId(short flowTableId) {
        this.flowTableId = flowTableId;
    }

    public int getFlowPriority() {
        return flowPriority;
    }

    public void setFlowPriority(int flowPriority) {
        this.flowPriority = flowPriority;
    }

    public int getFlowIdleTimeout() {
        return flowIdleTimeout;
    }

    public void setFlowIdleTimeout(int flowIdleTimeout) {
        this.flowIdleTimeout = flowIdleTimeout;
    }

    public int getFlowHardTimeout() {
        return flowHardTimeout;
    }

    public void setFlowHardTimeout(int flowHardTimeout) {
        this.flowHardTimeout = flowHardTimeout;
    }

    @Override
    public void onNodeConnectorRemoved(NodeConnectorRemoved n) {
        // nothing
    }

    @Override
    public void onNodeConnectorUpdated(NodeConnectorUpdated n) {
        // nothing
    }

    @Override
    public void onNodeRemoved(NodeRemoved n) {
        // nothing
    }

    @Override
    public void onNodeUpdated(NodeUpdated n) {
        logger.debug("Fire for {}", n.toString());
        initialFlowExecutor.submit(new InitialFlowWriterProcessor(n));
    }

    public class InitialFlowWriterProcessor implements Runnable {

        private NodeUpdated nodeUpdated;
        public InitialFlowWriterProcessor(NodeUpdated n) {
            this.nodeUpdated = n;
        }

        @Override
        public void run() {
            logger.debug("run for {}", nodeUpdated.toString());

            if (nodeUpdated == null) {
                return;
            }

            // Need to get an InstanceIdentifier.
            try {
                addInitialFlows((InstanceIdentifier<Node>) nodeUpdated.getNodeRef().getValue());
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        public void addInitialFlows(InstanceIdentifier<Node> nodeId) throws ExecutionException, InterruptedException {
            logger.info("Add flows for {}", nodeId);

            // Build flow
            Flow f = makeControllerFlow();

            // At this point in the process, the new switch might not have been
            // added to the switch inventory.  We don't have any easy way of getting
            // the Node object corresponding to the switch (so we can do
            // OdlMdsalImpl.addFlow()) but we don't need this either since all
            // we need it for is to make an InstanceIdentifier<Node> and we were
            // handed that.  So we just in-line the parts of addFlow() that make
            // sense for us to do here.
            //
            // There's probably a better way to do this!
            AddFlowInputBuilder builder = new AddFlowInputBuilder(f);
            builder.setNode(new NodeRef(nodeId));

            InstanceIdentifier<Flow> flowInstanceIdentifier =
                    nodeId.builder().
                    augmentation(FlowCapableNode.class).
                    child(Table.class, new TableKey(f.getTableId())).
                    child(Flow.class, f.getKey()).
                    build();
            builder.setFlowRef(new FlowRef(flowInstanceIdentifier));
            builder.setFlowTable(new FlowTableRef(OdlMdsalImpl.getTableInstanceId(nodeId, f.getTableId())));

            builder.setTransactionUri(new Uri(f.getId().getValue()));

            Future<RpcResult<AddFlowOutput>> resultFuture =
                    salFlowService.addFlow(builder.build());
            RpcResult<AddFlowOutput> rpcResult = resultFuture.get();
            if (rpcResult.isSuccessful()) {
                AddFlowOutput result = rpcResult.getResult();
            }
            else {
                logger.error(rpcResult.getErrors().toString());
            }

        }

        /**
         * Create a forwarding flow that sends everything to the controller.
         */
        public Flow makeControllerFlow() {

            // Create a match object.  We match everything.
            MatchBuilder matchBuilder = new MatchBuilder();

            // Create an output action to forward to the output port
            OutputActionBuilder output = new OutputActionBuilder();
            output.setMaxLength(0xffff);
            output.setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()));

            // Put that in an action
            ActionBuilder ab = new ActionBuilder();
            ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
            ab.setOrder(0);
            ab.setKey(new ActionKey(0));

            // Make an action list to hold the action
            List<Action> actionList = Lists.newArrayList();
            actionList.add(ab.build());

            // Create apply actions instruction
            ApplyActionsBuilder aab = new ApplyActionsBuilder();
            aab.setAction(actionList);

            // Now create an instruction to include the instruction
            InstructionBuilder ib = new InstructionBuilder();
            ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
            ib.setOrder(0);
            ib.setKey(new InstructionKey(0));

            // List of instructions that stores individual instructions
            List<Instruction> instructions = Lists.newArrayList();
            instructions.add(ib.build());

            // Now we need an instruction set builder
            InstructionsBuilder isb = new InstructionsBuilder();
            isb.setInstruction(instructions);

            // Finally get to make the flow itself
            FlowBuilder flowBuilder = new FlowBuilder();

            flowBuilder.setBarrier(true);
            flowBuilder.setTableId((short) 0);
            flowBuilder.setPriority(getFlowPriority());
            flowBuilder.setHardTimeout(flowHardTimeout);
            flowBuilder.setIdleTimeout(flowIdleTimeout);
            flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
            flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));

            FlowId flowId = new FlowId("ToController");
            flowBuilder.setId(flowId);
            flowBuilder.setKey(new FlowKey(flowId));
            flowBuilder.setFlowName(flowId.getValue());

            flowBuilder.setMatch(matchBuilder.build());
            flowBuilder.setInstructions(isb.build());

            return flowBuilder.build();

        }


    }
}
