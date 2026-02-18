import javax.xml.stream.*;
import java.io.*;
import java.util.*;

public class OSMParser {

    public static Graph parse(String file) throws Exception {
        Graph graph = new Graph();

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader =
                factory.createXMLStreamReader(new FileInputStream(file));

        List<Long> wayNodes = new ArrayList<>();
        boolean isHighway = false;
        boolean isHospital = false;
        String hospitalName = null;

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamReader.START_ELEMENT) {
                String name = reader.getLocalName();

                if (name.equals("node")) {
                    long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                    double lat = Double.parseDouble(reader.getAttributeValue(null, "lat"));
                    double lon = Double.parseDouble(reader.getAttributeValue(null, "lon"));
                    graph.nodes.put(id, new Node(id, lat, lon));
                }

                else if (name.equals("way")) {
                    wayNodes.clear();
                    isHighway = false;
                    isHospital = false;
                    hospitalName = null;
                }

                else if (name.equals("nd")) {
                    wayNodes.add(Long.parseLong(reader.getAttributeValue(null, "ref")));
                }

                else if (name.equals("tag")) {
                    String k = reader.getAttributeValue(null, "k");
                    String v = reader.getAttributeValue(null, "v");

                    if ("highway".equals(k)) isHighway = true;
                    if ("amenity".equals(k) && "hospital".equals(v)) isHospital = true;
                    if ("name".equals(k)) hospitalName = v;
                }
            }

            if (event == XMLStreamReader.END_ELEMENT &&
                reader.getLocalName().equals("way")) {

                if (isHospital && !wayNodes.isEmpty()) {
                    graph.hospitalNames.put(
                            wayNodes.get(0),
                            hospitalName != null ? hospitalName : "Unnamed Hospital"
                    );
                }

                if (isHighway) {
                    for (int i = 0; i < wayNodes.size() - 1; i++) {
                        Node n1 = graph.nodes.get(wayNodes.get(i));
                        Node n2 = graph.nodes.get(wayNodes.get(i + 1));

                        if (n1 != null && n2 != null) {
                            double d = haversine(n1.lat, n1.lon, n2.lat, n2.lon);

                            graph.roadNodes.add(n1.id);
                            graph.roadNodes.add(n2.id);

                            n1.edges.add(new Edge(n1.id, n2.id, d));
                            n2.edges.add(new Edge(n2.id, n1.id, d));
                        }
                    }
                }
            }
        }
        return graph;
    }

    static double haversine(double lat1, double lon1,
                            double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
