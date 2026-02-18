import java.util.*;

class Ambulance {
    String id;
    long nodeId;
    boolean available;

    Ambulance(String id, long nodeId, boolean available) {
        this.id = id;
        this.nodeId = nodeId;
        this.available = available;
    }
}

public class Main {

    static long findNearestRoadNode(Graph g, double lat, double lon) {
        long best = -1;
        double min = Double.MAX_VALUE;

        for (long id : g.roadNodes) {
            Node n = g.nodes.get(id);
            if (n == null) continue;

            double d = OSMParser.haversine(lat, lon, n.lat, n.lon);
            if (d < min) {
                min = d;
                best = id;
            }
        }
        return best;
    }

    static long findNearestHospital(Graph g, long patientNode) {
        Node p = g.nodes.get(patientNode);

        long best = -1;
        double min = Double.MAX_VALUE;

        for (long hId : g.hospitalNames.keySet()) {
            Node h = g.nodes.get(hId);
            if (h == null) continue;

            double d = OSMParser.haversine(p.lat, p.lon, h.lat, h.lon);
            if (d < min) {
                min = d;
                best = hId;
            }
        }
        return best;
    }

    static List<Ambulance> placeAmbulancesNearPatient(Graph g, long patientNode) {
        Node p = g.nodes.get(patientNode);
        List<Ambulance> list = new ArrayList<>();
        int count = 0;

        for (long id : g.roadNodes) {
            Node n = g.nodes.get(id);
            double d = OSMParser.haversine(p.lat, p.lon, n.lat, n.lon);

            if (d < 2000) {
                list.add(new Ambulance(
                        "AMB-" + (count + 1),
                        id,
                        count != 2
                ));
                count++;
            }
            if (count == 3) break;
        }
        return list;
    }

    public static void main(String[] args) throws Exception {

        Graph g = OSMParser.parse(
            "C:/Users/arnav/OneDrive/Desktop/pblprojectambulanceapp/noida.osm"
        );

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter patient latitude: ");
        double plat = sc.nextDouble();
        System.out.print("Enter patient longitude: ");
        double plon = sc.nextDouble();

        long patientNode = findNearestRoadNode(g, plat, plon);
        System.out.println("Patient mapped to road node: " + patientNode);

        List<Ambulance> ambulances =
                placeAmbulancesNearPatient(g, patientNode);

        System.out.println("\nNearby Ambulances:");
        for (Ambulance a : ambulances) {
            Node n = g.nodes.get(a.nodeId);
            double d = OSMParser.haversine(
                    g.nodes.get(patientNode).lat,
                    g.nodes.get(patientNode).lon,
                    n.lat, n.lon
            );
            System.out.println(
                    a.id + " | Distance: " +
                    String.format("%.2f", d) +
                    " meters | Available: " + a.available
            );
        }

        long rawHospital = findNearestHospital(g, patientNode);
        System.out.println(
                "\nNearest hospital: " +
                g.hospitalNames.get(rawHospital)
        );

        Node h = g.nodes.get(rawHospital);
        long hospitalNode = findNearestRoadNode(g, h.lat, h.lon);

        List<Long> path =
                AStar.findPath(g, patientNode, hospitalNode);

        System.out.println("\nShortest Path:");

        double totalDistance = 0;
        StringBuilder mapsUrl =
                new StringBuilder("https://www.google.com/maps/dir/");

        for (int i = 0; i < path.size(); i++) {
            Node n = g.nodes.get(path.get(i));
            System.out.println(n.lat + ", " + n.lon);

            mapsUrl.append(n.lat)
                   .append(",")
                   .append(n.lon)
                   .append("/");

            if (i < path.size() - 1) {
                Node next = g.nodes.get(path.get(i + 1));
                totalDistance += OSMParser.haversine(
                        n.lat, n.lon,
                        next.lat, next.lon
                );
            }
        }

        System.out.printf(
                "%nTotal Distance: %.2f km%n",
                totalDistance / 1000.0
        );

        System.out.println("\nOpen this route in Google Maps:");
        System.out.println(mapsUrl);
    }
}
