# Telestion RFC: Optimistic type inference on JSON / Event Bus messages in the Telestion Application

Date: 2022-11-16

## Status

Proposed

## Context

[//]: # (The issue motivating this decision, and any context that influences or constrains the decision.)

Previously, the Telestion Application used a strict type inference on JSON / Event Bus messages.

This means that any message that was sent to the Event Bus had to be of a specific type and annotated as such using a
`className` field.

A message, therefore, would look like this:

```json
{
  "className": "de.wuespace.telestion.project.daedalus2.mission.MissionState",
  "missionState": "RUNNING"
}
```

A message with the same validity and information but without the `className` field would be rejected by the Event Bus:

```json
{
  "missionState": "RUNNING"
}
```

This means that any system interacting with the Telestion Application had to know the exact type of the message and its
corresponding class name.

This, then, creates a strong coupling between the Telestion Application and the systems that interact with it.

Any refactoring on the application side would have to be reflected by the other systems, as well.

Application developers already have to "ask" for the type they want to receive from the Event Bus, meaning that the
proposed type has to be explicitly stated during parsing:

```java
public class MessageTransformer extends TelestionVerticle<MessageTransformer.Configuration> {
    @Override
    public void onStart() {
        var eb = getVertx().eventBus();

        eb.consumer("address", event ->
                JsonMessage.on(IntegerMessage.class, event, message -> {
                    // Do something with the message
                }));
    }
}
```

The system doesn't offer real type safety, as a malicious actor could send a message with a different type than the one
declared:

```json
{
  "className": "de.wuespace.telestion.project.daedalus2.mission.MissionState",
  "maliciousField": "maliciousValue"
}
```

Thus, the only real benefit of the strict type inference is that the system can reject messages easier and has an
additional layer of protection against mis-using event bus addresses (which, by design, should be used to define what
kind of data gets sent on them, and then a type compatibility check has to be performed to ignore "malicious messages").

## Decision

[//]: # (The change that we're proposing or have agreed to implement.)

We will remove the `className` field from the JSON / Event Bus message format.

We will consider any message that satisfies requirements of the expected type as valid.

To be more precise, for a requested type specification (e.g., a `record`), we will

- reject messages where a `@JsonProperty`-annotated property with `required = true` is missing
- reject messages where a `@JsonProperty`-annotated property with `required = true` has a `null` value
- reject messages where a `@JsonProperty`-annotated property with `required = true` has a value of a different type than
  the one declared in the `record` class
- accept messages where a `@JsonProperty`-annotated property with `required = false` is missing
- accept messages where a `@JsonProperty`-annotated property with `required = false` has a `null` value
- accept messages where a `@JsonProperty`-annotated property with `required = false` has a value of a different type
  than the one declared in the `record` class
- accept messages with additional properties

when parsing a message using `JsonMessage.on()` (or similar methods).

The following reference implementation provides a proof of concept for the proposed change:

```java
public class JsonHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, true);
    }

    public static <T> Optional<T> readValue(String json, Class<T> clazz) {
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
```

Here, `readValue` returns an `Optional` that is empty if the JSON string cannot be parsed into the requested type or the
object in the requested type if it is valid.

We will no longer require event bus messages to implement the `JsonMessage` interface.

## Consequences

### Better developer experience for Telestion Application developers

Integration with any external system will be easier, as with the new systems, the Telestion Application will be able to
accept arbitrary JSON messages without loosing type safety. Instead, the developer will only have to create a `record`
class that describes the expected message format.

Then, they can use the `JsonMessage.on()` method to parse the message and be sure that the message is valid (even if the
other system makes slight changes and introduces additional properties or other compatible changes).

This way, the implementation of the Telestion Application will be more robust and less error-prone.

Refactoring class names and package names will be easier, as the Telestion Application will not be coupled to external
systems.

Developers can use existing knowledge of the Jackson library to parse JSON messages and don't have to learn the
Telestion specific non-standard assumptions.

### Better developer experience for Telestion Client developers

This change means that messages sent from the client are no longer required to have knowledge about the implementation
structure on the server side. Instead, as long as the JSON object structure matches, the message is accepted.

### `JsonMessage` no longer required

Because the `ObjectMapper` no longer needs specific type information, the `JsonMessage` class is no longer required.

This means that any `record` that has JSON-serializable fields (annotated as `@JsonProperty`) can be sent over the event
bus and received from it.

### Breaking changes

Previously, usage of the `required = true` property in the `@JsonProperty` annotation was ignored (and, essentially, all
attributes were implicitly assumed to be required).

Therefore, a common message record would look like this:

```java
public record MyMessage(
        @JsonProperty String foo,
        @JsonProperty String bar,
        @JsonProperty String baz
) implements JsonMessage {
}
```

Because, according to the `@JsonProperty` annotation, properties are not required by default, our mapping would happily
accept an empty JSON object `{}` and map it to a `MyMessage` instance with all fields set to `null`.

> **Note:** This actually fits the specification of the `@JsonProperty` annotation, meaning that the behavior would be
> correct and our current implementation is "wrong", formally speaking.

However, the necessary refactoring to make this work is not too hard:

```java
public record MyMessage(
        @JsonProperty(required = true) String foo,
        @JsonProperty(required = true) String bar,
        @JsonProperty(required = true) String baz
) {
}
```

**There is no way to slowly deprecate the current behavior without significant detrimental effects on the developer
experience. Instead, this ADR proposes a "harsh" breaking change that is easy to implement and understand.**

Also, we would now be able to properly support the `required` property of the `@JsonProperty` annotation.
