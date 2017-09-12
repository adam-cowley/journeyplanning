package co.wecommit.journeyplanning.results;

import org.neo4j.graphdb.Node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static co.wecommit.journeyplanning.Utils.getDate;

public class Journey {
    private final Node origin;

    private final Node destination;

    private final ArrayList<HashMap<String, Object>> legs;

    public Journey(Node origin, Node destination, ArrayList<HashMap<String, Object>> legs) {
        this.origin = origin;
        this.destination = destination;
        this.legs = legs;
    }


    public String toString() {
        String output = "\n\nJourney: "+ origin.getProperty("name") + " to "+ destination.getProperty("name") + "\n--";

        SimpleDateFormat time = new SimpleDateFormat("HH:mm");

        for (HashMap<String, Object> leg : legs) {
            output += "\n";

            switch ( leg.get("type").toString() ) {
                case "leg":
                    Node leg_node = (Node) leg.get("leg");
                    Node station = (Node) leg.get("station");
                    Node platform = (Node) leg.get("platform");

                    Date departs_at = getDate(leg_node, "departs_at");

                    output += "-- "+ leg_node.getId();
                    output += " -- "+ time.format(departs_at);
                    output += " " + station.getProperty("name") + " P. "+ platform.getProperty("number");
                    break;
                case "platform_transfer":
                    Node p = (Node) leg.get("platform");

                    output += "--[TX] -- ";

                    output += p.getProperty("name");
                    break;
            }
        }

        output += "\n";
        output += "\n";

        HashMap<String, Object> last_leg = legs.get(legs.size() - 1);

        Date arrives_at = getDate((Node) last_leg.get("leg"), "arrives_at");

        output += "Arrives at "+ destination.getProperty("name") + " at "+ time.format(arrives_at);

        output += "\n--\n\n";

        return output;
    }
}
