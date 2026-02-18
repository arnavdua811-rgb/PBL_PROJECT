import java.util.*;

public class AStar {

    static class State {
        long node;
        double f;
        State(long node, double f) {
            this.node = node;
            this.f = f;
        }
    }

    public static List<Long> findPath(Graph g, long start, long goal) {

        PriorityQueue<State> pq =
                new PriorityQueue<>(Comparator.comparingDouble(s -> s.f));

        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();

        gScore.put(start, 0.0);
        pq.add(new State(start, 0));

        while (!pq.isEmpty()) {
            long cur = pq.poll().node;
            if (cur == goal) break;

            Node cn = g.nodes.get(cur);
            if (cn == null) continue;

            for (Edge e : cn.edges) {
                double ng = gScore.get(cur) + e.weight;
                if (ng < gScore.getOrDefault(e.to, Double.MAX_VALUE)) {
                    gScore.put(e.to, ng);
                    parent.put(e.to, cur);

                    Node next = g.nodes.get(e.to);
                    Node goalN = g.nodes.get(goal);
                    double h = OSMParser.haversine(
                            next.lat, next.lon,
                            goalN.lat, goalN.lon
                    );

                    pq.add(new State(e.to, ng + h));
                }
            }
        }

        List<Long> path = new ArrayList<>();
        if (!parent.containsKey(goal) && start != goal) return path;

        for (Long at = goal; at != null; at = parent.get(at))
            path.add(at);

        Collections.reverse(path);
        return path;
    }
}
