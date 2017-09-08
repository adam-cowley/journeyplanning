package co.wecommit.journeyplanning;

import org.neo4j.graphdb.RelationshipType;

public enum Relationships implements RelationshipType {
    CAN_ALIGHT,
    CAN_BOARD,
    CAN_TRANSFER_TO,
    HAS_LEG,
    HAS_PLATFORM,

    NEXT_LEG,

    OPERATES,

}