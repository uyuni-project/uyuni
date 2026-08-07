# Uyuni Node-RED Container Image

A containerized [Node-RED](https://nodered.org/) automation environment pre-installed with custom [`node-red-contrib-uyuni`](../../node-red-contrib-uyuni) nodes for low-code event-driven workflow automation.

This container connects to the **Uyuni MQTT Broker** (`mqtt-broker-image`) to listen for real-time server events, and triggers automated administrative actions back on the Uyuni Server over XML-RPC APIs.

## Key Features

- **Pre-installed Nodes**: Ships with `uyuni-events`, `uyuni-config`, `uyuni-action`, `uyuni-query`, and `uyuni-api-config` nodes out of the box.
- **Global Installation**: Package is installed globally (`npm install -g`), preserving pre-baked nodes even when a host volume is mounted to `/data`.
- **Dynamic Configuration**: Generates `settings.js` at runtime based on environment variables.
- **Uyuni Branding**: Includes custom editor page theme and header title.

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `NODERED_CREDENTIAL_SECRET` | **Yes** | *None* | Secret key used to encrypt credentials stored in flows |
| `MQTT_BROKER_HOST` | No | `mosquitto` | Hostname/IP of the Uyuni MQTT Broker |
| `MQTT_BROKER_PORT` | No | `1883` | Port of the MQTT Broker |
| `UYUNI_SERVER_URL` | No | `https://uyuni-server` | Base URL of the Uyuni XML-RPC endpoint |

## Running with Podman / Docker

```bash
podman run -d --name uyuni-nodered \
  --network uyuni \
  -p 1880:1880 \
  -v uyuni-nodered-data:/data \
  -e NODERED_CREDENTIAL_SECRET="your-secure-secret-key" \
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
