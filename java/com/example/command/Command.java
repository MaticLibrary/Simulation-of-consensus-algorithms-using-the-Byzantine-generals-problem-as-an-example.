package com.example.command;

public interface Command {
    void execute();
    void undo();
    void redo();
    String getName();
}
