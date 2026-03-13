package com.example.draw;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.example.model.MyGraph;
import com.example.model.MyVertex;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OpinionGenerator {

    public void generateAttackers(MyGraph<Integer, Integer> graph, double percentage) {
        int numberOfVertices = (int) (graph.numVertices() * percentage);
        List<Vertex<Integer>> attackers = pickAttackers(graph.vertices(), numberOfVertices);
        graph.vertices().forEach(vertex -> ((MyVertex<Integer>) vertex).setIsSupporting(attackers.contains(vertex)));
    }

    private List<Vertex<Integer>> pickAttackers(Collection<Vertex<Integer>> vertices, int number) {
        List<Vertex<Integer>> verticesCopy = new ArrayList<>(vertices);
        Collections.shuffle(verticesCopy);
        return verticesCopy.subList(0, number);
    }
}
