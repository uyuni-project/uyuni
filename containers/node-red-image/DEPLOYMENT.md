# Deployment Guide — Uyuni MQTT + Node-RED Stack

## Quick Start (Podman)

### Prerequisites
- A running Uyuni server, containerised
- Podman 4.x or newer
- The name of the network the Uyuni containers share, `uyuni` in a standard
  installation. Check with:

```bash
podman inspect uyuni-server --format '{{range $k, $v := .NetworkSettings.Networks}}{{$k}}{{end}}'
```

### 1. Choose credentials

The broker creates two accounts at start-up: `uyuni-publisher`, which may only
write to `uyuni/#` and is used by the Uyuni server, and `uyuni-subscriber`,
which may only read and is used by Node-RED. The split means a compromised
consumer cannot inject forged events.

Put the passwords in a file rather than on the command line, so they stay out of
shell history:

```bash
printf 'MQTT_PUBLISHER_PASSWORD=%s\nMQTT_SUBSCRIBER_PASSWORD=%s\n' \
  'a-long-random-string' 'a-different-long-random-string' > /root/mqtt.env
chmod 600 /root/mqtt.env
```

The user names are fixed, because the ACL grants permissions per user. Only the
passwords are configurable.

### 2. Start the Mosquitto broker

```bash
podman run -d \
  --name mosquitto \
  --network uyuni \
  --restart unless-stopped \
  -p 1883:1883 \
  -v uyuni-mqtt-data:/mosquitto/data \
  --env-file /root/mqtt.env \
  registry.opensuse.org/uyuni/mqtt-broker:latest
```

The broker refuses to start without both passwords rather than falling back to
anonymous access. Confirm that anonymous clients are rejected:

```bash
podman run --rm --network uyuni docker.io/library/eclipse-mosquitto \
  mosquitto_pub -h mosquitto -t 'uyuni/test' -m hi
```

This should fail with `Connection Refused: not authorised`. If it succeeds, the
broker is not enforcing authentication.

### 3. Configure the Uyuni server

The MQTT settings are JVM system properties, not `rhn.conf` entries. Add them
under `/etc/tomcat/conf.d/`, which is on the `etc-tomcat` volume and therefore
survives the container being recreated:

```bash
podman exec uyuni-server sh -c 'cat > /etc/tomcat/conf.d/mqtt_java_opts.conf <<EOF
JAVA_OPTS="\$JAVA_OPTS -Duyuni.mqtt.enabled=true -Duyuni.mqtt.broker.url=tcp://mosquitto:1883 -Duyuni.mqtt.broker.username=uyuni-publisher -Duyuni.mqtt.broker.password=a-long-random-string"
EOF'
podman exec uyuni-server systemctl restart tomcat
```

Use the same publisher password as in `/root/mqtt.env`. Restarting Tomcat
interrupts the web interface for a minute or so.

Confirm the publisher connected:

```bash
podman exec uyuni-server grep -i mqtt /var/log/rhn/rhn_web_ui.log | tail -3
podman logs mosquitto | grep uyuni-publisher
```

Look for `Successfully connected to MQTT broker` on the Uyuni side and
`New client connected ... u'uyuni-publisher'` on the broker side. The `u'...'`
confirms it authenticated rather than connecting anonymously.

### 4. Start Node-RED

```bash
podman run -d \
  --name nodered \
  --network uyuni \
  --restart unless-stopped \
  -p 1880:1880 \
  -v uyuni-nodered-data:/data \
  -e NODERED_CREDENTIAL_SECRET='another-long-random-string' \
  -e NODERED_ADMIN_PASSWORD_HASH='$2b$08$...' \
  -e MQTT_BROKER_HOST=mosquitto \
  -e UYUNI_SERVER_URL=https://uyuni.example.com \
  registry.opensuse.org/uyuni/node-red:latest
```

Mount a volume for `/data`. Without one, every flow an administrator builds is
lost when the container is replaced.

Generate the password hash first, since the container will not start without it:

```bash
podman run --rm -it registry.opensuse.org/uyuni/node-red:latest node-red admin hash-pw
```

To run flows without an editor at all, set `NODERED_DISABLE_EDITOR=true` and omit
the hash.

### 5. Connect a flow

Open `http://<your-server>:1880` and sign in with the editor credentials.

Add a **Uyuni Events** node and create a broker configuration for it: host
`mosquitto`, port `1883`, and the `uyuni-subscriber` credentials. Leave the topic
prefix as `uyuni/+` to receive events from every instance on the broker, or set
it to `uyuni/<your server fqdn>` for one.

Wire it to a debug node and deploy, then trigger something on the server such as
registering a minion. The event should appear in the debug pane.

Ready-made flows ship inside the image and can be brought in from the editor's
**Import → Examples → node-red-contrib-uyuni** menu.

---

## Docker Compose

A `docker-compose.yml` is provided in this directory:

```bash
cp .env.example .env    # then edit the passwords
podman-compose up -d
```

Step 3 still has to be done by hand, since it configures the Uyuni server rather
than these containers.

Note that the broker image is pulled from the registry and is published by the
Open Build Service. Until it has been built there, compose cannot start the
broker.

---

## Kubernetes

The containers can run as separate deployments alongside Uyuni. Credentials
should come from a secret rather than literal values:

```yaml
containers:
  - name: mosquitto
    image: registry.opensuse.org/uyuni/mqtt-broker:latest
    ports:
      - containerPort: 1883
    env:
      - name: MQTT_PUBLISHER_PASSWORD
        valueFrom:
          secretKeyRef:
            name: mqtt-credentials
            key: publisher-password
      - name: MQTT_SUBSCRIBER_PASSWORD
        valueFrom:
          secretKeyRef:
            name: mqtt-credentials
            key: subscriber-password
    volumeMounts:
      - name: mqtt-data
        mountPath: /mosquitto/data
```

This has not been tested on a cluster and should be treated as a starting point.

---

## Environment Variables Reference

### Mosquitto broker

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MQTT_PUBLISHER_PASSWORD` | Yes | — | Password for the write-only `uyuni-publisher` account |
| `MQTT_SUBSCRIBER_PASSWORD` | Yes | — | Password for the read-only `uyuni-subscriber` account |

### Node-RED

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `NODERED_CREDENTIAL_SECRET` | Yes | — | Encrypts credentials stored in flows |
| `NODERED_ADMIN_PASSWORD_HASH` | Yes, unless the editor is disabled | — | bcrypt hash of the editor password |
| `NODERED_ADMIN_USER` | No | `admin` | Editor username |
| `NODERED_DISABLE_EDITOR` | No | `false` | `true` stops the editor and admin API being served |
| `NODERED_HTTP_NODE_PASSWORD_HASH` | No | — | Protects endpoints created by `http in` nodes |
| `NODERED_HTTP_NODE_USER` | No | `uyuni` | Username for those endpoints |
| `NODERED_LOG_LEVEL` | No | `info` | Runtime log level |
| `MQTT_BROKER_HOST` | No | `mosquitto` | Broker host, offered to flows |
| `MQTT_BROKER_PORT` | No | `1883` | Broker port, offered to flows |
| `UYUNI_SERVER_URL` | No | `https://uyuni-server` | Server URL, offered to flows |

The last three are written into Node-RED's global context, so a function node can
read `global.get('mqttBrokerHost')`. They do not configure the nodes themselves:
broker and API details, including credentials, are entered in the `uyuni-config`
and `uyuni-api-config` nodes, where they are stored encrypted.

### Uyuni server (JVM system properties)

| Property | Default | Description |
|----------|---------|-------------|
| `uyuni.mqtt.enabled` | `false` | Master switch. The publisher is not started unless this is `true` |
| `uyuni.mqtt.broker.url` | `tcp://mosquitto:1883` | Broker connection URL |
| `uyuni.mqtt.broker.username` | _(none)_ | Broker authentication username |
| `uyuni.mqtt.broker.password` | _(none)_ | Broker authentication password |
| `uyuni.mqtt.qos` | `1` | MQTT QoS level (0, 1, or 2) |
| `uyuni.mqtt.events.enabled` | _(all)_ | Comma-separated topic allowlist, for example `systems.registered,jobs.returned` |
| `uyuni.mqtt.queue.limit` | `10000` | Bounded publish queue size |

---

## TLS

TLS between the publisher and the broker is not implemented yet. The broker image
ships a plain listener on 1883 only, and the Uyuni publisher has been tested with
`tcp://` URLs. This is recorded as a future enhancement in the RFC.

Node-RED's `uyuni-config` node does expose a **Use TLS** option, which switches
the connection to `mqtts://`. It is usable against a broker you have configured
for TLS yourself, but the shipped broker image is not one.

---

## Verification

### Watch events arrive

```bash
podman run --rm --network uyuni --env-file /root/mqtt.env \
  docker.io/library/eclipse-mosquitto \
  sh -c 'mosquitto_sub -h mosquitto -u uyuni-subscriber -P "$MQTT_SUBSCRIBER_PASSWORD" -t "uyuni/#" -v'
```

Then trigger something on the server. A Salt job return looks like:

```
uyuni/uyuni-server.mgr.internal/jobs/returned {"eventId":"9a68a713-...","timestamp":"2026-08-06T17:46:45.464Z","topic":"uyuni/uyuni-server.mgr.internal/jobs/returned","data":{"jid":"20260806174645240085","success":true,"minionId":"client1.example.com","fun":"test.ping","retcode":0}}
```

### Check Node-RED is connected

Open the editor and confirm the `uyuni-events` node shows a green **connected**
status.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Node-RED shows "disconnected" | Wrong broker credentials | A broker that rejects the login refuses the connection outright, so authentication problems look like connection problems. Check the username and password first. |
| No events arriving | Publisher not configured or not started | Check `rhn_web_ui.log` for `Successfully connected`. If absent, verify `/etc/tomcat/conf.d/mqtt_java_opts.conf` and restart Tomcat. |
| Publisher connects but nothing is published | Topic prefix mismatch | The server publishes under its own FQDN, shown in the `Initializing MqttPublisherService` log line. Make the subscription match. |
| Events stop after a broker restart | By design | Events published while the broker is unavailable are dropped rather than queued, to avoid unbounded memory use. Publishing resumes automatically. |
| Connection loop in the broker log | Duplicate MQTT client ID | Two Node-RED nodes sharing a client ID will disconnect each other repeatedly. Give each one its own. |
| Flows disappeared after replacing the container | No volume on `/data` | There is no recovery. Check the mount before relying on it. |
