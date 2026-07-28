# Uyuni MQTT broker image

An [Eclipse Mosquitto](https://mosquitto.org/) broker, configured for distributing
Uyuni server events to external consumers such as Node-RED, Grafana or custom
scripts.

The Uyuni server publishes events to this broker; consumers subscribe. Neither
side needs to know about the other, so new consumers can be added without
touching the server.

## Access control

The broker does **not** accept anonymous clients. Two accounts are created at
container start-up, each restricted to one direction of traffic:

| User               | Permission              | Used by                          |
|--------------------|-------------------------|----------------------------------|
| `uyuni-publisher`  | write to `uyuni/#`      | The Uyuni server                 |
| `uyuni-subscriber` | read from `uyuni/#`     | Node-RED and other consumers     |

The split matters: a compromised consumer cannot inject forged events into the
stream, and the server cannot read back what other instances publish.

The user names are fixed because `acl.conf` grants permissions per user. Only
the passwords are configurable.

## Running it

Both passwords are required. The broker exits with an error if either is
missing, rather than falling back to anonymous access.

```bash
podman run -d --name uyuni-mqtt-broker \
  -p 1883:1883 \
  -v uyuni-mqtt-data:/mosquitto/data \
  -e MQTT_PUBLISHER_PASSWORD=<publisher password> \
  -e MQTT_SUBSCRIBER_PASSWORD=<subscriber password> \
  registry.opensuse.org/uyuni/mqtt-broker:latest
```

| Variable                    | Required | Purpose                                |
|-----------------------------|----------|----------------------------------------|
| `MQTT_PUBLISHER_PASSWORD`   | yes      | Password for `uyuni-publisher`         |
| `MQTT_SUBSCRIBER_PASSWORD`  | yes      | Password for `uyuni-subscriber`        |

Persistence is enabled and `/mosquitto/data` is declared as a volume, so
retained messages survive re-creating the container. Logs go to stdout for
collection by the container runtime.

## Pointing Uyuni at it

Set these on the Uyuni server so the publisher can authenticate:

```
uyuni.mqtt.broker.url=tcp://<broker host>:1883
uyuni.mqtt.broker.username=uyuni-publisher
uyuni.mqtt.broker.password=<publisher password>
```

The username and password may also be supplied as the environment variables
`UYUNI_MQTT_BROKER_USERNAME` and `UYUNI_MQTT_BROKER_PASSWORD`.

## Topics

Events are published as:

```
uyuni/<server-fqdn>/<category>/<action>
```

Including the server FQDN means several Uyuni instances can share one broker.
Subscribe to a single instance with `uyuni/server.example.com/#`, or to the same
event across every instance with `uyuni/+/systems/registered`.

## Checking it works

Subscribe as the read-only consumer and watch events arrive:

```bash
mosquitto_sub -h <broker host> -p 1883 \
  -u uyuni-subscriber -P <subscriber password> \
  -t 'uyuni/#' -v
```

Then trigger something on the server — registering a minion or applying a state
will do — and the JSON should appear.

To confirm the access rules are actually in force, these should all be refused:

```bash
# No credentials at all
mosquitto_pub -h <broker host> -t 'uyuni/test' -m hi

# A consumer trying to publish a forged event
mosquitto_pub -h <broker host> -u uyuni-subscriber -P <subscriber password> \
  -t 'uyuni/test' -m hi
```

The first is rejected at connection time with `Connection Refused: not
authorised`. The second connects but the message is silently discarded by the
ACL, so it never reaches subscribers.

## Building

The image installs mosquitto with `zypper` from the repositories supplied by the
Open Build Service, and makes no network requests of its own at build time. It
therefore builds in OBS but not with a bare `podman build`, since the base
image's own repository does not carry mosquitto — the same is true of the other
Uyuni container images.
