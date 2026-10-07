# node-red-contrib-uyuni

Node-RED nodes for [Uyuni](https://www.uyuni-project.org/). Receive server events
in real time over MQTT, and act back on the server through its API — without
wiring up HTTP nodes and session handling by hand.

A typical flow: a minion registers → the event arrives on the canvas → a function
node decides what to do → an action node applies a state or schedules a reboot.

## Nodes

| Node              | Type   | What it does                                                             |
|-------------------|--------|--------------------------------------------------------------------------|
| `uyuni-events`    | input  | Subscribes to Uyuni events on the MQTT broker and emits them as messages |
| `uyuni-config`    | config | Broker connection and credentials, shared by every event node            |
| `uyuni-action`    | output | Performs an action on the server: apply highstate, reboot, install packages, list channels |
| `uyuni-query`     | input  | Reads data back: active systems, system details, relevant errata, installed packages |
| `uyuni-api-config`| config | API URL and credentials, shared by the action and query nodes            |

The two config nodes exist so credentials are entered once and reused, rather
than repeated on every node in the flow.

## Installing

From your Node-RED user directory (usually `~/.node-red`):

```bash
npm install node-red-contrib-uyuni
```

Restart Node-RED, and the nodes appear under the **Uyuni** category in the
palette.

## Setting up the connection

**Broker.** Add a `uyuni-config` node with your broker's host and port. The
broker requires authentication, so fill in the username and password too — use
the `uyuni-subscriber` account, which is read-only and cannot publish. Enable
TLS if your broker is configured for it.

**API.** Add a `uyuni-api-config` node with your server's XML-RPC endpoint
(`https://<server>/rpc/api`) and a user that has permission for the actions you
intend to run. The server's certificate is verified by default; if you are
pointing at a development server with a self-signed certificate, tick
**Self-signed** to skip verification for that connection.

Credentials are stored using Node-RED's own credential mechanism, encrypted at
rest and kept out of exported flows.

## Receiving events

Drop a `uyuni-events` node on the canvas, pick a config node, and choose an event
category — or leave it at `#` to receive everything.

The node parses the JSON and splits it across the message, so the event's own
fields are directly on `msg.payload` and the envelope metadata is kept separate:

```js
{
  topic: "uyuni/server.example.com/systems/registered",
  payload: {                       // the event's own data
    minionId: "client1.example.com",
    machineId: "8c1f...",
    saltbootInitrd: false
  },
  uyuni: {                         // envelope metadata
    eventId: "3f2b1c8e-...",
    timestamp: "2026-07-27T09:14:22.481Z",
    category: "systems",
    eventType: "registered"
  }
}
```

So a minion ID is `msg.payload.minionId`, not `msg.payload.data.minionId`.
`category` and `eventType` are derived from the topic, which makes them handy
for routing in a switch node.

Nine event types are published:

| Topic suffix          | Fires when                                     |
|-----------------------|------------------------------------------------|
| `systems/registered`  | A minion registers with the server             |
| `jobs/returned`       | A Salt job returns                             |
| `states/applied`      | Salt states are applied to a system            |
| `images/deployed`     | An image is deployed                           |
| `batches/started`     | A batch execution starts                       |
| `users/created`       | A user is created                              |
| `orgs/created`        | An organization is created                     |
| `clm/build_started`   | A content lifecycle build starts               |
| `clm/build_completed` | A content lifecycle build finishes             |

Topics are `uyuni/<server-fqdn>/<category>/<action>`, so one broker can carry
several Uyuni instances. Subscribe to `uyuni/+/systems/registered` to catch that
event from all of them.

## Acting on the server

The `uyuni-action` node takes a system ID from `msg.payload.systemId`,
`msg.payload.serverId`, `msg.payload.id`, or `msg.systemId` — so an event node
can usually feed it directly.

Session handling is automatic: the node logs in, caches the session, and if a
call fails with a session fault it clears the cache, logs in once more, and
retries. Faults unrelated to the session are reported rather than retried, so a
bad argument surfaces as an error instead of looping.

`uyuni-query` works the same way and returns its result on `msg.payload`.

## Example Flows (`examples/`)

Pre-built importable flows are included under [`examples/`](./examples):

1. **`jira-ticket-creation.json`**: Listens for failed Salt jobs (`jobs/returned` with `success === false`) and automatically POSTs to Jira REST API (`/rest/api/2/issue`) to create an incident bug ticket.
2. **`slack-webhook-alert.json`**: Listens for system registration events and posts rich Slack Block Kit notifications to an incoming Slack Webhook endpoint.
3. **`email-notification-alert.json`**: Listens for `states/applied` events and formats email notifications with applied state module details.
4. **`minion-registration-alert.json`**: Formats registration alert messages with timestamps.
5. **`clm-build-monitor.json`**: Routes Content Lifecycle Management build start and completion events.
6. **`auto-highstate-on-register.json`**: Automatically triggers `applyHighstate` via XML-RPC when a new minion registers.

Import any flow in Node-RED via **Menu → Import → Examples → node-red-contrib-uyuni**.

## Requirements

- Node-RED 3.0 or later
- Node.js 18 or later
- A Uyuni server publishing events, and a reachable MQTT broker

## Development

```bash
npm install
npm test
```

Tests use `node-red-node-test-helper` with Mocha.

## License

GPL-2.0
