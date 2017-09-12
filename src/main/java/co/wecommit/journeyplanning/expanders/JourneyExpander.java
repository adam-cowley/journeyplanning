package co.wecommit.journeyplanning.expanders;

import co.wecommit.journeyplanning.Labels;
import co.wecommit.journeyplanning.Relationships;
import co.wecommit.journeyplanning.Utils;
import co.wecommit.journeyplanning.evaluators.Evaluators;
import co.wecommit.journeyplanning.expanders.ReverseJourneyExpander;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Collections;

import static co.wecommit.journeyplanning.Utils.print;

public class JourneyExpander implements PathExpander<Long> {

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Long> state) {
        Node last = path.endNode();

        ArrayList<Node> nodes = Utils.nodesToArray(path);
        ArrayList<Relationship> rels = Utils.relsToArray(path);

        int nodesize = rels.size();
        int relsize = rels.size();

        System.out.println("");
        System.out.println("");
        System.out.println("START "+ path.length());
        print(path);


        // We always start at a Leg, so we can alight or go to a next leg.
        if (path.length() == 0) {
            return last.getRelationships(Direction.OUTGOING, Relationships.CAN_ALIGHT, Relationships.NEXT_LEG);
        }

        // If Path length is three, check there is no Platform -> Platform -> Platform
        if ( path.length() >= 3 ) {
            Node n1 = nodes.get( relsize-3 );
            Node n2 = nodes.get( relsize-2 );
            Node n3 = nodes.get( relsize-1 );

            if (Evaluators.isMultiPlatformTraversal(n1, n2, n3) ) {
                return Collections.emptyList();
            }
        }

        // From a leg you should be able to alight or go to the next leg
        if ( last.hasLabel(Labels.Leg) ) {
            return last.getRelationships(Direction.OUTGOING, Relationships.CAN_ALIGHT, Relationships.NEXT_LEG);
        }
        // From a platform you should be able to board a leg or transfer to another platform
        else if ( last.hasLabel(Labels.Platform) ) {
            return last.getRelationships(Direction.OUTGOING, Relationships.CAN_BOARD, Relationships.CAN_TRANSFER_TO);
        }


        return Collections.emptyList();
    }

    @Override
    public PathExpander<Long> reverse() {
        return new ReverseJourneyExpander();
    }

}
