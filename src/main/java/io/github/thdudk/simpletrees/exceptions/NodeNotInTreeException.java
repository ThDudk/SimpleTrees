package io.github.thdudk.simpletrees.exceptions;

public class NodeNotInTreeException extends IllegalArgumentException {
    public NodeNotInTreeException(String message) {
        super(message);
    }
}
