package co.wecommit.journeyplanning;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;


public class BidirectionalJourneyExpander implements PathExpander<Double> {

    @Override
    public Iterable<Relationship> expand(Path path, BranchState<Double> state) {
        return path.endNode().getRelationships(Relationships.NEXT_LEG, Direction.OUTGOING);
    }

    @Override
    public PathExpander<Double> reverse() {
        return new PathExpander<Double>() {

            @Override
            public Iterable<Relationship> expand(Path path, BranchState<Double> state) {
                return path.endNode().getRelationships(Relationships.NEXT_LEG, Direction.INCOMING);
            }

            @Override
            public PathExpander<Double> reverse() {
                return null;
            }
        };
    }
}
