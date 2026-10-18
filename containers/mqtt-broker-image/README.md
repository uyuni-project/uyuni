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
| `MQTT_PLAIN_LISTENER`       | no       | `false` closes the plain port 1883. Needs TLS to be set up |
| `MQTT_REQUIRE_CLIENT_CERT`  | no       | `true` makes TLS clients present a certificate signed by `ca.crt` |
| `MQTT_TLS_CERT_DIR`         | no       | Where to look for certificates, `/mosquitto/certs` by default |

Persistence is enabled and `/mosquitto/data` is declared as a volume, so
retained messages survive re-creating the container. Logs go to stdout for
collection by the container runtime.

## TLS

TLS is switched on by mounting a certificate and its key. With these files in
the certificate directory, the broker opens a TLS listener on port 8883:

| File         | Purpose                                                        |
|--------------|----------------------------------------------------------------|
| `server.crt` | The broker certificate, issued for the host name clients use   |
| `server.key` | Its private key, readable by user 1883                         |
| `ca.crt`     | Only with `MQTT_REQUIRE_CLIENT_CERT=true`: the CA that signs client certificates |

```bash
podman run -d --name uyuni-mqtt-broker \
  -p 8883:8883 \
  -v uyuni-mqtt-data:/mosquitto/data \
  -v /etc/uyuni-mqtt/certs:/mosquitto/certs:ro \
  -e MQTT_PUBLISHER_PASSWORD=<publisher password> \
  -e MQTT_SUBSCRIBER_PASSWORD=<subscriber password> \
  -e MQTT_PLAIN_LISTENER=false \
  registry.opensuse.org/uyuni/mqtt-broker:latest
```

Usernames and passwords are still required over TLS. Client certificates are an
extra check, not a replacement for them.

The plain listener on 1883 stays open unless `MQTT_PLAIN_LISTENER=false`, so
existing set-ups keep working after an upgrade. Once every client uses TLS,
close it. The broker refuses to start if the settings would leave it with no
listener, or with only one of `server.crt` and `server.key`. It prints the
listeners it opened at start-up.

## Pointing Uyuni at it

Set these on the Uyuni server so the publisher can authenticate:

```
uyuni.mqtt.enabled=true
uyuni.mqtt.broker.url=tcp://<broker host>:1883
uyuni.mqtt.broker.username=uyuni-publisher
uyuni.mqtt.broker.password=<publisher password>
```

The username and password may also be supplied as the environment variables
`UYUNI_MQTT_BROKER_USERNAME` and `UYUNI_MQTT_BROKER_PASSWORD`.

Over TLS, use `ssl://<broker host>:8883` and point
`uyuni.mqtt.tls.ca.file` at the CA that signed `server.crt`. The host name must
match the certificate.

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
