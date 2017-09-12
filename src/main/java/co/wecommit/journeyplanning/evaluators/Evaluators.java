package co.wecommit.journeyplanning.evaluators;

import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;

import static co.wecommit.journeyplanning.Utils.hasDegree;


public class Evaluators {

    public static boolean hasGoneTooFar(Node last_node, Node end_platform) {
        if ( !last_node.hasLabel(Labels.Platform) ) {
            return false;
        }

        return Utils.hasDegree(last_node, end_platform, Relationships.CAN_TRANSFER_TO, Direction.BOTH);
    }

    public static boolean isIgnoringNextLegRelationship(ArrayList<Node> nodes) {
        int nodesize = nodes.size();

        if ( nodes.size() < 3 ) {
            return false;
        }

        Node l1 = nodes.get( nodesize -3 );
        Node platform = nodes.get( nodesize -2 );
        Node l2 = nodes.get( nodesize -1 );

        return l1.hasLabel(Labels.Leg) && platform.hasLabel(Labels.Platform) && l2.hasLabel(Labels.Leg) && Utils.hasDegree(l1, l2, Relationships.NEXT_LEG, Direction.BOTH);
    }

    public static boolean isMultiPlatformTraversal(Iterable<Node> path) {
        ArrayList<Node> nodes = Utils.nodesToArray(path);
        int size = nodes.size();

        if ( size < 3 ) {
            return false;
        }

        Node n1 = nodes.get( size - 3 );
        Node n2 = nodes.get( size - 2 );
        Node n3 = nodes.get( size - 1 );

        return isMultiPlatformTraversal(n1, n2, n3);
    }

    public static boolean isMultiPlatformTraversal(Path path) {
        if ( path.length() < 2 ) {
            return false;
        }

        ArrayList<Node> nodes = Utils.nodesToArray(path);
        int size = nodes.size();

        Node n1 = nodes.get( size - 3 );
        Node n2 = nodes.get( size - 2 );
        Node n3 = nodes.get( size - 1 );

        return isMultiPlatformTraversal(n1, n2, n3);
    }

    public static boolean isMultiPlatformTraversal(Node n1, Node n2, Node n3) {
        return n1.hasLabel(Labels.Platform) && n2.hasLabel(Labels.Platform) && n3.hasLabel(Labels.Platform);
    }

    public static boolean hasMissedConnection(ArrayList<Node> nodes, Long max_waiting) {
        int nodesize = nodes.size();

        if ( nodesize < 3 ) {
            return false;
        }

        Node l1 = nodes.get( nodesize -3 );
        Node platform = nodes.get( nodesize -2 );
        Node l2 = nodes.get( nodesize -1 );

        if ( ! (l1.hasLabel(Labels.Leg) && platform.hasLabel(Labels.Platform) && l2.hasLabel(Labels.Leg) ) ) {
            return false;
        }

        Long arrives_at = Utils.getLong(l1, "arrives_at");
        Long departs_at = Utils.getLong(l2, "departs_at");

        return arrives_at >= departs_at || departs_at >= arrives_at + max_waiting ;
    }

    public static boolean hasMissedConnectionAcrossPlatforms(ArrayList<Node> nodes, ArrayList<Relationship> rels, Long max_waiting) {
        int nodesize = nodes.size();

        if ( nodesize < 4 ) {
            return false;
        }

        Node l1 = nodes.get( nodesize -4 );
        Node p1 = nodes.get( nodesize -3 );
        Node p2 = nodes.get( nodesize -2 );
        Node l2 = nodes.get( nodesize -1 );

        Relationship transfer = rels.get( rels.size() -2 );

        if ( ! (l1.hasLabel(Labels.Leg) && p1.hasLabel(Labels.Platform) && p2.hasLabel(Labels.Platform) && l2.hasLabel(Labels.Leg) ) ) {
            return false;
        }

        Long arrives_at = Utils.getLong(l1, "arrives_at");
        Long transfer_time = Utils.getLong(transfer, "minutes");
        Long departs_at = Utils.getLong(l2, "departs_at");

        return arrives_at + transfer_time >= departs_at || departs_at >= arrives_at + transfer_time + max_waiting;
    }
}
