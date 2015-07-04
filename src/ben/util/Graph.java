package ben.util;

import java.util.*;

import static ben.util.Guards.assume;

/**
 * Created by benh on 6/21/15.
 */
public class Graph<N> {

    private Set<N> nodes = new HashSet<>();
    private Set<Edge<N>> edges = new HashSet<>(); // bidirectional

    public boolean containsNode(final N node){
        return nodes.contains(node);
    }

    public boolean containsEdge(final N source, final N target){
        Edge<N> edge = new Edge<>(source, target);
        return edges.contains(edge);
    }

    public void addNode(final N node){
        nodes.add(node);
    }

    public void addEdge(final N source, final N target){
        edges.add(new Edge<>(source, target));
    }

    public Set<N> getNodes(){
        return Collections.unmodifiableSet(nodes);
    }

    public Set<Edge<N>> getEdges(){
        return Collections.unmodifiableSet(edges);
    }

    public Set<Edge<N>> getAdjacencies(final N node){
        final Set<Edge<N>> result = new HashSet<>();
        for(final Edge<N> edge: edges){
            if (edge.incidentOn(node)){
                result.add(edge);
            }
        }
        return result;
    }

    public static class Edge<N> { // bidirectional
        private final Set<N> nodes = new HashSet<>();
        public Edge(final N n1, final N n2){
            nodes.add(n1);
            nodes.add(n2);
        }
        public N first(){  return nodes.iterator().next();  }
        public N second(){
            if (nodes.size() == 1) return first();
            Iterator<N> i = nodes.iterator();
            i.next();
            return i.next();
        }
        public N other(final N node){
            Set<N> copy = new HashSet<N>(nodes);
            copy.remove(node);
            assume(copy.size() == 1);
            return copy.iterator().next();
        }
        public boolean incidentOn(final N node){
            return nodes.contains(node);
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?> edge = (Edge<?>) o;
            return !(nodes != null ? !nodes.equals(edge.nodes) : edge.nodes != null);
        }
        @Override public int hashCode() {
            return nodes != null ? nodes.hashCode() : 0;
        }
        @Override public String toString(){
            return nodes.toString();
        }
    }
}
