package de.wuespace.types;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;


public record House(
        @JsonProperty(required = true) String name,
        @JsonProperty() Set<Room> rooms
) {
    public House {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        if (rooms == null) {
            throw new IllegalArgumentException("Rooms must not be null");
        }
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Rooms must not be empty");
        }
    }

    public int getNumberOfRooms() {
        return rooms.size();
    }

    public int getTotalSize() {
        return rooms.stream().mapToInt(Room::size).sum();
    }
}
