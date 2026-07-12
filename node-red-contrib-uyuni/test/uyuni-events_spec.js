const should = require("should");
const helper = require("node-red-node-test-helper");
const uyuniEvents = require("../nodes/uyuni-events/uyuni-events.js");
const uyuniConfig = require("../nodes/uyuni-events/uyuni-config.js");
const mqtt = require("mqtt");

// Mock MQTT client
const mockClient = {
  on: function(event, callback) {
    if (event === 'connect') {
      setTimeout(() => callback(), 10);
    }
    return this;
  },
  subscribe: function(topic, options, callback) {
    if (callback) callback();
  },
  end: function(force, options, callback) {
    if (callback) callback();
  }
};

let originalMqttConnect;

describe('uyuni-events Node', function () {
  before(function (done) {
    originalMqttConnect = mqtt.connect;
    mqtt.connect = function() { return mockClient; };
    helper.startServer(done);
  });

  after(function (done) {
    mqtt.connect = originalMqttConnect;
    helper.stopServer(done);
  });

  afterEach(function () {
    helper.unload();
  });

  it('should be loaded', function (done) {
    const flow = [
      { id: "n1", type: "uyuni-events", name: "uyuni-events", server: "c1", eventCategory: "#" },
      { id: "c1", type: "uyuni-config", name: "uyuni-config", broker: "localhost", port: 1883 }
    ];
    helper.load([uyuniEvents, uyuniConfig], flow, function () {
      const n1 = helper.getNode("n1");
      n1.should.have.property('name', 'uyuni-events');
      done();
    });
  });
});
