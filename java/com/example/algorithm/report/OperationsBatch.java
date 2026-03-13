package com.example.algorithm.report;

import com.example.algorithm.operations.Operation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class OperationsBatch {
    @Getter
    private List<Operation> operations = new ArrayList<>();

    public void add(Operation operation) {
        operations.add(operation);
    }

    public void remove(Operation operation) {
        operations.remove(operation);
    }
}
