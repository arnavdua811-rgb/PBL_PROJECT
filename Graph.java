import java.util.*;

class Node {
    long id;
    double lat, lon;
    List<Edge> edges = new ArrayList<>();

    Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
}

class Edge {
    long from, to;
    double weight;

    Edge(long from, long to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}

class Graph {
    Map<Long, Node> nodes = new HashMap<>();

    // hospital node → hospital name
    Map<Long, String> hospitalNames = new HashMap<>();

    // ONLY road-connected nodes
    Set<Long> roadNodes = new HashSet<>();
}
