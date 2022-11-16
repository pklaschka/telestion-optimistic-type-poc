package de.wuespace;

import de.wuespace.types.House;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class JsonHelperTest {
    @Test
    void getObjectMapper() {
        assertNotNull(JsonHelper.getObjectMapper());
    }

    @Test
    void readValue() {
        var json = """
                {
                    "name": "My House",
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
        var house = JsonHelper.readValue(json, House.class);
        assertTrue(house.isPresent(), "House is present when parsing a perfect JSON");
        assertEquals("My House", house.get().name(), "Name is correct");
        assertEquals(2, house.get().rooms().size(), "Number of rooms is correct");

        json = """
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
        house = JsonHelper.readValue(json, House.class);
        assertTrue(house.isPresent(), "House is present when parsing a JSON with unknown properties");
        assertEquals("My House", house.get().name(), "Name is correct");
        assertEquals(2, house.get().rooms().size(), "Number of rooms is correct");

        json = """
                {
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
        house = JsonHelper.readValue(json, House.class);
        assertTrue(house.isEmpty(), "House is empty when parsing a JSON without a name (required)");

        json = """
                {
                    "name": "My House",
                    "rooms": 3
                }
                """;
        house = JsonHelper.readValue(json, House.class);
        assertTrue(house.isEmpty(), "House is empty when parsing a JSON with a wrong type for rooms");
    }

    @Test
    void on() {
        // assert that the callback gets called
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
        AtomicBoolean called = new AtomicBoolean(false);
        JsonHelper.on(json, House.class, h -> {
            assertEquals("My House", h.name());
            assertEquals(2, h.rooms().size());
            called.set(true);
        });
        assertTrue(called.get(), "Callback should be called when parsing a valid JSON");

        // assert that the callback does not get called
        json = """
                {
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
        called.set(false);
        JsonHelper.on(json, House.class, h -> called.set(true));
        assertFalse(called.get(), "Callback should not be called when parsing an invalid JSON");
    }
}