package com.example.draw;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.example.model.MyGraph;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphGenerator {

    public Graph<Integer, Integer> generateGraph(DefinedGraph definedGraph, Map<String, Integer> settings){
        return switch (definedGraph) {
            case FULL -> getFullGraph(settings);
            case TREE -> getTreeGraph(settings);
            case CYCLE -> getCycleGraph(settings);
            case PLANAR -> getPlanarGraph(settings);
            case BIPARTITE -> getBipartiteGraph(settings);
        };
    }

    private Graph<Integer, Integer> getFullGraph(Map<String, Integer> settings) {
        Graph<Integer, Integer> graph = new MyGraph<>();
        int numberOfVertices = settings.get("numberOfVertices");
        for (int i = 0; i < numberOfVertices; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < numberOfVertices - 1; i++) {
            for (int j = i + 1; j < numberOfVertices; j++){
                graph.insertEdge(i, j, 1);
            }
        }
        return graph;
    }

    private Graph<Integer, Integer> getCycleGraph(Map<String, Integer> settings) {
        Graph<Integer, Integer> graph = new MyGraph<>();
        int numberOfVertices = settings.get("numberOfVertices");
        for (int i = 0; i < numberOfVertices; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < numberOfVertices; i++) {
            graph.insertEdge(i, (i + 1) % numberOfVertices, 1);
        }
        return graph;
    }

    private Graph<Integer, Integer> getTreeGraph(Map<String, Integer> settings) {
        Graph<Integer, Integer> graph = new MyGraph<>();
        int numberOfChildren = settings.get("numberOfChildren");
        int height = settings.get("height");
        if (height <= 0) {
            return graph;
        }
        int numberOfVertices;
        int numberOfVerticesWithChildren;
        if (numberOfChildren <= 0) {
            numberOfVertices = 1;
            numberOfVerticesWithChildren = 0;
        } else if (numberOfChildren == 1) {
            numberOfVertices = height;
            numberOfVerticesWithChildren = Math.max(0, height - 1);
        } else {
            numberOfVertices = (int) ((1 - Math.pow(numberOfChildren, height)) / (1 - numberOfChildren));
            numberOfVerticesWithChildren = (int) ((1 - Math.pow(numberOfChildren, height - 1)) / (1 - numberOfChildren));
        }
        for (int i = 0; i < numberOfVertices; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < numberOfVerticesWithChildren; i ++) {
            for (int j = 1; j < numberOfChildren + 1; j++) {
                int childIndex = numberOfChildren * i + j;
                graph.insertEdge(i, childIndex, 1);
            }
        }
        return graph;
    }

    private Graph<Integer, Integer> getBipartiteGraph(Map<String, Integer> settings) {
        Graph<Integer, Integer> graph = new MyGraph<>();
        int numberOfVerticesInFirstSet = settings.get("numberOfVerticesInFirstSet");
        int numberOfVerticesInSecondSet = settings.get("numberOfVerticesInSecondSet");
        for (int i = 0; i < numberOfVerticesInFirstSet + numberOfVerticesInSecondSet; i++) {
            graph.insertVertex(i);
        }
        for(int i = 0; i < numberOfVerticesInFirstSet; i ++) {
            for (int j = 0; j < numberOfVerticesInSecondSet; j++) {
                graph.insertEdge(i, numberOfVerticesInFirstSet + j, 1);
            }
        }
        return graph;
    }

    private Graph<Integer, Integer> getPlanarGraph(Map<String, Integer> settings) {
        Graph<Integer, Integer> graph = new MyGraph<>();
        int width = settings.get("width");
        int height = settings.get("height");
        int numberOfVertices = width * height;
        for (int i = 0 ; i < numberOfVertices; i++) {
            graph.insertVertex(i);
        }
        for (int i = 0; i < numberOfVertices; i++) {
            if (i % width != width - 1) {
                graph.insertEdge(i, i + 1, 1);
            }
            if (i < numberOfVertices - width) {
                graph.insertEdge(i, i + width, 1);
            }
        }
        return graph;
    }
}
