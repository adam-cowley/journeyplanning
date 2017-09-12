package co.wecommit.journeyplanning.expanders;

import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import co.wecommit.journeyplanning.evaluators.Evaluators;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Collections;

import static co.wecommit.journeyplanning.Utils.print;


public class ReverseJourneyExpander implements PathExpander<Long> {

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Long> state) {
        Node last = path.endNode();

        ArrayList<Node> nodes = Utils.nodesToArray(path);
        ArrayList<Relationship> rels = Utils.relsToArray(path);

        int nodesize = rels.size();
        int relsize = rels.size();

        // If at the end of the path, we need a next leg or can alight
        if (path.length() == 0) {
            return last.getRelationships(Direction.INCOMING, Relationships.CAN_ALIGHT, Relationships.NEXT_LEG);
        }

        // If three long, check it's not :Platform -> :Platform -> :Platform
        if (path.length() >= 3) {
            Node n1 = nodes.get( 0 );
            Node n2 = nodes.get( 1 );
            Node n3 = nodes.get( 2 );

            if (Evaluators.isMultiPlatformTraversal(n1, n2, n3) ) {
                System.out.println("IGNORE MULTI");
                return Collections.emptyList();
            }
        }

        System.out.println("");
        System.out.println("");
        System.out.println("END "+ path.length());
        print(path);

        if ( last.hasLabel(Labels.Leg) ) {
            Long arrives_at = Utils.getLong(last, "arrives_at");
            Long departs_at = Utils.getLong(last, "departs_at");


            System.out.println("");
            System.out.println("Compare reverse");
            System.out.println(arrives_at +" & "+ departs_at +" vs "+ state.getState());
            System.out.println("");


            return last.getRelationships(Direction.INCOMING, Relationships.CAN_BOARD, Relationships.NEXT_LEG);
        }
        else if ( last.hasLabel(Labels.Station) ) {
            return last.getRelationships(Direction.INCOMING, Relationships.CAN_ALIGHT, Relationships.CAN_TRANSFER_TO);
        }
System.out.println("Sending Empty...");
        return Collections.emptyList();
    }

    @Override
    public PathExpander<Long> reverse() {
        return null;
    }
}
