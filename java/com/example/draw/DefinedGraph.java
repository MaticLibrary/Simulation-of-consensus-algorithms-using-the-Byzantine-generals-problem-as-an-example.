package com.example.draw;

public enum DefinedGraph {
    FULL,
    CYCLE,
    TREE,
    BIPARTITE,
    PLANAR;

    @Override
    public String toString() {
        return switch (this) {
            case FULL -> "Graf pełny";
            case CYCLE -> "Graf cyklu";
            case TREE -> "Drzewo";
            case BIPARTITE -> "Graf dwudzielny";
            case PLANAR -> "Graf planarny";
        };
    }
}
