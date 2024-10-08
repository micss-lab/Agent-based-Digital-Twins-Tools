package milo.opcua.server;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.FolderTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class CustomNamespace extends ManagedNamespace {
    private static final Logger logger = LoggerFactory.getLogger(CustomNamespace.class);
    public static final String URI = "urn:my:custom:namespace";
    static UaVariableNode destinationPoint;
    static UaVariableNode positionX;
    static UaVariableNode positionY;
    private final SubscriptionModel subscriptionModel;
    public CustomNamespace(final OpcUaServer server, final String uri) throws InterruptedException {
        super(server, uri);
        this.subscriptionModel = new SubscriptionModel(server, this);
        registerItems(getNodeContext());

    }

    private void registerItems(final UaNodeContext context) throws InterruptedException {
        System.out.println("Registering items");

        // create a folder
        final UaFolderNode folder = new UaFolderNode(
                context,
                newNodeId(1),
                newQualifiedName("FirstFolder"),
                LocalizedText.english("MainFolder"));
        context.getNodeManager().addNode(folder);

        // add the folder to the objects folder
        final Optional<UaNode> objectsFolder = context.getServer()
                .getAddressSpaceManager()
                .getManagedNode(Identifiers.ObjectsFolder);
        objectsFolder.ifPresent(node -> {
            ((FolderTypeNode) node).addComponent(folder);
        });

        // add several variables
        {
            destinationPoint = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                    .setNodeId(newNodeId("0-unique-identifier"))
                    .setAccessLevel(AccessLevel.READ_WRITE)
                    .setUserAccessLevel(AccessLevel.READ_WRITE)
                    .setBrowseName(newQualifiedName("DestinationPoint"))
                    .setDisplayName(LocalizedText.english("Destination Point Variable"))
                    .setDataType(Identifiers.String)
                    .setDescription(LocalizedText.english("Target Destination Point"))
                    .build();

            Vector3D vector = new Vector3D(7000, 9000, 0);

            positionX = createUaVariableNode(newNodeId("6-unique-identifier"), AccessLevel.READ_WRITE, AccessLevel.READ_WRITE, Identifiers.Integer, "X Position", "Updating the X Position", "Get the X Position");
            positionY = createUaVariableNode(newNodeId("7-unique-identifier"), AccessLevel.READ_WRITE, AccessLevel.READ_WRITE, Identifiers.Integer, "Y Position", "Updating the Y Position", "Get the Y Position");

            // Set the variable node values
            destinationPoint.setValue(new DataValue(new Variant(vector.toString())));

            // add all the variables to the main folder
            folder.addOrganizes(destinationPoint);
            context.getNodeManager().addNode(destinationPoint);
            folder.addOrganizes(positionY);
            context.getNodeManager().addNode(positionY);
            folder.addOrganizes(positionX);
            context.getNodeManager().addNode(positionX);
        }
    }

    protected void onStartup() throws InterruptedException {
        registerItems(getNodeContext());
        System.out.println("URI = " + URI);
    }

    @Override
    public void onDataItemsCreated(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(final List<DataItem> dataItems) {
        this.subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
        this.subscriptionModel.onMonitoringModeChanged(monitoredItems);

    }

    public UaVariableNode createUaVariableNode (NodeId nodeId, ImmutableSet<AccessLevel> accessLevel, ImmutableSet<AccessLevel> userAccessLevel, NodeId dataType, String qualifiedName, String displayName, String description)
    {
        UaVariableNode uaVariableNode = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                .setNodeId(nodeId)
                .setAccessLevel(accessLevel)
                .setUserAccessLevel(userAccessLevel)
                .setDataType(dataType)
                .setBrowseName(newQualifiedName(qualifiedName))
                .setDisplayName(LocalizedText.english(displayName))
                .setDescription(LocalizedText.english(description))
                .build();
        return  uaVariableNode;
    }
}
