# Uyuni Node-RED Container Image

A containerized [Node-RED](https://nodered.org/) automation environment pre-installed with custom [`node-red-contrib-uyuni`](../../node-red-contrib-uyuni) nodes for low-code event-driven workflow automation.

This container connects to the **Uyuni MQTT Broker** (`mqtt-broker-image`) to listen for real-time server events, and triggers automated administrative actions back on the Uyuni Server over XML-RPC APIs.

## Key Features

- **Pre-installed Nodes**: Ships with `uyuni-events`, `uyuni-config`, `uyuni-action`, `uyuni-query`, and `uyuni-api-config` nodes out of the box.
- **Global Installation**: Package is installed globally (`npm install -g`), preserving pre-baked nodes even when a host volume is mounted to `/data`.
- **Configuration from the environment**: `settings.js` reads its values from environment variables at startup, so a secret is never substituted into the file.
- **Editor secured by default**: the container refuses to start with an unauthenticated editor.
- **Uyuni Branding**: Includes custom editor page theme and header title.

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `NODERED_CREDENTIAL_SECRET` | **Yes** | *None* | Secret key used to encrypt credentials stored in flows |
| `NODERED_ADMIN_PASSWORD_HASH` | **Yes**, unless the editor is disabled | *None* | bcrypt hash of the editor password |
| `NODERED_ADMIN_USER` | No | `admin` | Editor username |
| `NODERED_DISABLE_EDITOR` | No | `false` | `true` stops the editor and admin API being served at all |
| `NODERED_HTTP_NODE_PASSWORD_HASH` | No | *None* | bcrypt hash protecting endpoints created by `http in` nodes |
| `NODERED_HTTP_NODE_USER` | No | `uyuni` | Username for those endpoints |
| `NODERED_LOG_LEVEL` | No | `info` | Runtime log level |
| `MQTT_BROKER_HOST` | No | `mosquitto` | Hostname/IP of the Uyuni MQTT Broker |
| `MQTT_BROKER_PORT` | No | `1883` | Port of the MQTT Broker |
| `UYUNI_SERVER_URL` | No | `https://uyuni-server` | Base URL of the Uyuni XML-RPC endpoint |

## Securing the editor

The admin API can deploy flows, and a flow can execute arbitrary code, so an
unauthenticated editor is remote code execution against the host. The container
therefore refuses to start unless the editor is either protected by a password
or switched off. See the
[Node-RED security guide](https://nodered.org/docs/user-guide/runtime/securing-node-red).

Generate a password hash:

```bash
podman run --rm -it registry.opensuse.org/uyuni/node-red:latest node-red admin hash-pw
```

Pass the result as `NODERED_ADMIN_PASSWORD_HASH`. On a deployment that only runs
flows already present in `flows.json`, set `NODERED_DISABLE_EDITOR=true` instead:
the runtime keeps working and nothing is served on the admin route.

Node-RED serves the editor over plain HTTP. Put it behind a TLS-terminating
reverse proxy, or bind it to the container network only, rather than exposing
port 1880 to an untrusted network.

## Running with Podman / Docker

```bash
podman run -d --name uyuni-nodered \
  --network uyuni \
  -p 1880:1880 \
  -v uyuni-nodered-data:/data \
  -e NODERED_CREDENTIAL_SECRET="your-secure-secret-key" \
  -e NODERED_ADMIN_PASSWORD_HASH='$2b$08$...' \
  -e MQTT_BROKER_HOST="mosquitto" \
  -e MQTT_BROKER_PORT=1883 \
  -e UYUNI_SERVER_URL="https://uyuni.example.com" \
  registry.opensuse.org/uyuni/node-red:latest
```

Joining the same network as the broker is what lets `MQTT_BROKER_HOST=mosquitto`
resolve by container name.

Open your browser at `http://localhost:1880` to access the Node-RED flow editor canvas.

## Volume Storage (`/data`)

The `/data` directory holds user flow files (`flows.json`), credentials (`flows_cred.json`), and settings. Mount a host directory or named volume to `/data` for persistence.

## Building Locally

To build from repository root:

```bash
podman build -f containers/node-red-image/Dockerfile -t uyuni/node-red:latest .
```
