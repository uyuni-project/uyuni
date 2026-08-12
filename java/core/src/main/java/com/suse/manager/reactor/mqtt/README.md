# MQTT Event Publisher — Java Architecture

## Overview

This package implements real-time MQTT event publishing from the Uyuni
Java backend. When specific actions occur (user creation, Salt job returns,
CLM builds, etc.), structured JSON messages are published to a Mosquitto
broker, enabling external automation tools like Node-RED to react.

## Component Diagram

There are two paths into the publisher. Salt reactor events arrive through
`MqttEventAction`; events raised directly by application code go through
`MqttEventHelper`, which defers publishing until the transaction commits.

```
SaltReactor                      OrgFactory / UserFactory / ContentManager
     │                                          │
     ▼                                          ▼
MqttEventAction                          MqttEventHelper
  ← maps a reactor message                 ← defers until the DB
    to an MqttEvent                          transaction commits
     │                                          │
     └──────────────────┬───────────────────────┘
                        ▼
              MqttPublisherService
       ← async Paho client, FQDN topic prefix,
         JSON envelope, bounded queue, reconnect
                        │
                        ▼
              Mosquitto Broker (TCP 1883)
```

## Key Classes

### MqttPublisherService

Async MQTT client built on Eclipse Paho `MqttAsyncClient`. A single instance is
held statically and retrieved with `getInstance()`. It is created by
`RhnServletListener` on web application start-up and shut down on context
destroy.

Configuration is read from JVM system properties. In a containerised
deployment these are set in `/etc/tomcat/conf.d/`, for example:

```
JAVA_OPTS="$JAVA_OPTS -Duyuni.mqtt.broker.url=tcp://mosquitto:1883"
```

| Property | Default | Description |
|----------|---------|-------------|
| `uyuni.mqtt.broker.url` | `tcp://mosquitto:1883` | Broker connection URL |
| `uyuni.mqtt.broker.username` | _(none)_ | Broker authentication username |
| `uyuni.mqtt.broker.password` | _(none)_ | Broker authentication password |
| `uyuni.mqtt.qos` | `1` | MQTT QoS level (0, 1, or 2) |
| `uyuni.mqtt.events.enabled` | _(all)_ | Comma-separated event filter |
| `uyuni.mqtt.queue.limit` | `10000` | Bounded publish queue size |

The username and password may also be supplied as the environment variables
`UYUNI_MQTT_BROKER_USERNAME` and `UYUNI_MQTT_BROKER_PASSWORD`.

The client ID is generated per instance (`uyuni-publisher-<random>`) and is not
configurable, so two servers sharing a broker cannot collide.

Key design decisions:

- **Never blocks the caller.** Publishing is handed to a single-thread daemon
  executor, so broker latency cannot stall the reactor.
- **Bounded queue** (default 10000) with a discard-oldest policy that logs a
  warning, so a slow or absent broker cannot exhaust memory.
- **Skip and log** when the broker is unreachable. Events are dropped rather
  than buffered indefinitely; this was a deliberate choice during RFC review.
- **Automatic reconnect** via Paho's `setAutomaticReconnect(true)`.
- **FQDN topic prefix** resolved once at start-up, so several Uyuni instances
  can share a broker.

### MqttEventAction

A `MessageAction` registered in `SaltReactor` for five reactor message types. It
maps an incoming message to the matching `MqttEvent` and publishes it. It holds
no per-event logic of its own, so it does not grow as topics are added.

`canRunConcurrently()` returns `true`, since the action only builds a payload and
hands it to the publisher's executor.

### MqttEventHelper

Publishes events raised directly from application code, and owns the transaction
handling:

- `publish(event)` publishes immediately.
- `publishAfterCommit(event)` registers a Hibernate `runAfterCompletion`
  callback and publishes only if the transaction reaches `COMMITTED`. If no
  transaction is active it publishes immediately.

Use `publishAfterCommit` from factories and managers. Publishing is asynchronous
and cannot be recalled, so an event sent inside an open transaction would
announce a change that a rollback then discards.

### MqttEvent Strategy Classes

Each event type implements the `MqttEvent` interface:

```java
public interface MqttEvent {
    String getTopicSuffix();
    Map<String, Object> getPayload();
}
```

An event therefore owns both where it is published and what it publishes.
Adding a topic means adding a class, not extending a dispatcher.

| Class | Topic Suffix | Triggered By |
|-------|-------------|--------------|
| `MinionRegisteredEvent` | `systems/registered` | `RegisterMinionEventMessage` |
| `JobReturnedEvent` | `jobs/returned` | `JobReturnEventMessage` |
| `StatesAppliedEvent` | `states/applied` | `ApplyStatesEventMessage` |
| `ImageDeployedEvent` | `images/deployed` | `ImageDeployedEventMessage` |
| `BatchStartedEvent` | `batches/started` | `BatchStartedEventMessage` |
| `UserCreatedEvent` | `users/created` | `UserFactory.saveNewUser()` |
| `OrgCreatedEvent` | `orgs/created` | `OrgFactory.saveInternal()` |
| `ClmBuildStartedEvent` | `clm/build_started` | `ContentManager.buildProject()` |
| `ClmBuildCompletedEvent` | `clm/build_completed` | `ContentManager.buildProject()`, synchronous builds only |

`clm/build_completed` is published only when the build runs synchronously. An
asynchronous build has merely scheduled the channel alignment at that point, so
reporting completion there would announce work that has not finished.

## Topic Format

```
uyuni/<server-fqdn>/<category>/<action>
```

Example: `uyuni/uyuni-server.mgr.internal/users/created`

## Message Envelope

The payload returned by `getPayload()` is wrapped before publishing:

```json
{
  "eventId": "c9281ab7-edf4-4c10-8971-54895bdf7337",
  "timestamp": "2026-08-06T18:26:11.482Z",
  "topic": "uyuni/uyuni-server.mgr.internal/clm/build_started",
  "data": { "projectLabel": "clm_project_1", "username": "admin" }
}
```

## Adding a New Event

1. Create a class in `event/` implementing `MqttEvent`, returning the topic
   suffix and payload.
2. For a reactor event, add a branch to `MqttEventAction.toMqttEvent()` and
   register the message type in `SaltReactor.start()`.
3. For an application event, call
   `MqttEventHelper.publishAfterCommit(new YourEvent(...))` where the change
   happens.
4. Add a test case and list the new topic in the RFC.

If a message can arrive with nothing worth publishing, return `null` from a
static `from(...)` factory and nothing is published.

## Testing

- `MqttEventActionTest` — reactor message to topic mapping, payload extraction
- `MqttEventHelperTest` — application events, filtering, credential parsing
- `MqttPublisherServiceTest` — event filter matching, credentials, configuration
  fallbacks for invalid QoS and queue limits
