package de.wuespace.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Room(@JsonProperty() int size) {
    public Room {
        if (size < 0) {
            throw new IllegalArgumentException("Size must not be negative");
        }
    }
}
