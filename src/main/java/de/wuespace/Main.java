package de.wuespace;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.wuespace.types.House;

public class Main {
    public static void main(String[] args) {
        var om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);

        var json = """
                {
                    "name": "My House",
                    "test": 3,
                    "rooms": [
                        {
                            "size": 10
                        },
                        {
                            "size": 20
                        }
                    ]
                }
                """;
        try {
            var house = om.readValue(json, House.class);
            System.out.println(house);
            System.out.printf("Number of rooms: %d%n", house.getNumberOfRooms());
            System.out.printf("Total size: %d%n", house.getTotalSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}