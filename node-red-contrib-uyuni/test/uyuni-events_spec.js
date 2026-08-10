const should = require("should");
const helper = require("node-red-node-test-helper");
const proxyquire = require("proxyquire").noCallThru();
const uyuniConfig = require("../nodes/uyuni-events/uyuni-config.js");

// A stand-in MQTT client. It records what the node asked for and lets a test
// drive the callbacks the node registered, so the parsing and lifecycle logic
// runs without a broker.
let connections = [];

function fakeClient() {
  const handlers = {};
  return {
    handlers: handlers,
    subscriptions: [],
    ended: false,
    on: function (event, cb) { handlers[event] = cb; return this; },
    subscribe: function (topic, opts, cb) {
      this.subscriptions.push({ topic: topic, opts: opts });
      if (cb) { cb(null); }
    },
    end: function (force, opts, cb) { this.ended = true; if (cb) { cb(); } },
    emit: function (event) {
      const args = Array.prototype.slice.call(arguments, 1);
      if (handlers[event]) { handlers[event].apply(null, args); }
    }
  };
}

const uyuniEvents = proxyquire("../nodes/uyuni-events/uyuni-events.js", {
  mqtt: {
    connect: function (url, opts) {
      const c = fakeClient();
      connections.push({ url: url, opts: opts, client: c });
      return c;
    }
  }
});

function flow(serverProps, nodeProps) {
  const server = { id: "c1", type: "uyuni-config", name: "broker", broker: "mosquitto", port: 1883 };
  Object.assign(server, serverProps || {});
  const evt = { id: "n1", type: "uyuni-events", name: "events", server: "c1", wires: [[]] };
  Object.assign(evt, nodeProps || {});
  return [evt, server];
}

function lastClient() {
  return connections[connections.length - 1].client;
}

describe('uyuni-events Node', function () {
  before(function (done) { helper.startServer(done); });
  after(function (done) { helper.stopServer(done); });

  beforeEach(function () { connections = []; });
  afterEach(function () { helper.unload(); });

  it('should be loaded', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      helper.getNode("n1").should.have.property('name', 'events');
      done();
    });
  });

  it('should connect over mqtt:// using the configured host and port', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow({ broker: "broker.example.com", port: 1884 }), function () {
      connections[0].url.should.equal("mqtt://broker.example.com:1884");
      connections[0].opts.clean.should.be.true();
      done();
    });
  });

  it('should connect over mqtts:// when TLS is enabled', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow({ usetls: true }), function () {
      connections[0].url.should.startWith("mqtts://");
      done();
    });
  });

  it('should pass credentials to the broker when a username is set', function (done) {
    const creds = { c1: { username: "uyuni-subscriber", password: "secret" } };
    helper.load([uyuniEvents, uyuniConfig], flow(), creds, function () {
      connections[0].opts.username.should.equal("uyuni-subscriber");
      connections[0].opts.password.should.equal("secret");
      done();
    });
  });

  it('should subscribe to every category by default', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      lastClient().emit("connect");
      lastClient().subscriptions[0].topic.should.equal("uyuni/+/#");
      lastClient().subscriptions[0].opts.qos.should.equal(1);
      done();
    });
  });

  it('should narrow the subscription to a chosen category', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow({}, { eventCategory: "systems/registered" }), function () {
      lastClient().emit("connect");
      lastClient().subscriptions[0].topic.should.equal("uyuni/+/systems/registered");
      done();
    });
  });

  it('should subscribe under a single server when a topic prefix is set', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow({ topicPrefix: "uyuni/server.example.com" }), function () {
      lastClient().emit("connect");
      lastClient().subscriptions[0].topic.should.equal("uyuni/server.example.com/#");
      done();
    });
  });

  it('should unwrap the envelope onto payload, topic and uyuni metadata', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.send = function (msg) {
        msg.topic.should.equal("uyuni/server.example.com/systems/registered");
        msg.payload.should.eql({ minionId: "client1.example.com", machineId: "m-42" });
        msg.uyuni.eventId.should.equal("evt-1");
        msg.uyuni.timestamp.should.equal("2026-08-05T09:00:00.000Z");
        msg.uyuni.category.should.equal("systems");
        msg.uyuni.eventType.should.equal("registered");
        done();
      };
      lastClient().emit("connect");
      lastClient().emit("message",
        "uyuni/server.example.com/systems/registered",
        Buffer.from(JSON.stringify({
          eventId: "evt-1",
          timestamp: "2026-08-05T09:00:00.000Z",
          topic: "uyuni/server.example.com/systems/registered",
          data: { minionId: "client1.example.com", machineId: "m-42" }
        })));
    });
  });

  it('should derive a multi-segment event type from the topic', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.send = function (msg) {
        msg.uyuni.category.should.equal("clm");
        msg.uyuni.eventType.should.equal("build_started");
        done();
      };
      lastClient().emit("connect");
      lastClient().emit("message", "uyuni/srv/clm/build_started",
        Buffer.from(JSON.stringify({ topic: "uyuni/srv/clm/build_started", data: { projectLabel: "p1" } })));
    });
  });

  it('should treat a bare payload without an envelope as the data itself', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.send = function (msg) {
        msg.payload.should.eql({ hello: "world" });
        msg.topic.should.equal("uyuni/srv/misc/thing");
        done();
      };
      lastClient().emit("connect");
      lastClient().emit("message", "uyuni/srv/misc/thing", Buffer.from(JSON.stringify({ hello: "world" })));
    });
  });

  it('should pass malformed JSON through as raw text rather than dropping it', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.warn = function () { /* expected */ };
      n1.send = function (msg) {
        msg.payload.should.equal("not json at all");
        msg.topic.should.equal("uyuni/srv/broken");
        done();
      };
      lastClient().emit("connect");
      lastClient().emit("message", "uyuni/srv/broken", Buffer.from("not json at all"));
    });
  });

  it('should report a subscribe failure as a node error', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/Subscribe failed/);
        done();
      };
      const client = lastClient();
      client.subscribe = function (topic, opts, cb) { cb(new Error("not authorised")); };
      client.emit("connect");
    });
  });

  it('should report a broker error as a node error', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/MQTT error/);
        done();
      };
      lastClient().emit("error", new Error("connection refused"));
    });
  });

  it('should disconnect cleanly when the flow is redeployed', function (done) {
    helper.load([uyuniEvents, uyuniConfig], flow(), function () {
      const client = lastClient();
      helper.getNode("n1").close().then(function () {
        client.ended.should.be.true();
        done();
      }).catch(done);
    });
  });
});
