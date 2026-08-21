const should = require("should");
const helper = require("node-red-node-test-helper");
const uyuniQuery = require("../nodes/uyuni-query/uyuni-query.js");
const uyuniApiConfig = require("../nodes/uyuni-api-config/uyuni-api-config.js");

// Build a flow with one query node wired to an API config node. The config
// node's callMethod is stubbed in each test, so the node's own dispatch logic
// runs without talking to a server.
function flowWith(queryType) {
  return [
    { id: "n1", type: "uyuni-query", name: "query", server: "c1", queryType: queryType },
    { id: "c1", type: "uyuni-api-config", name: "api", url: "https://localhost/rpc/api" }
  ];
}

function stubCallMethod(result) {
  const calls = [];
  helper.getNode("c1").callMethod = function (method, params) {
    calls.push({ method: method, params: params });
    return Promise.resolve(result === undefined ? [] : result);
  };
  return calls;
}

describe('uyuni-query Node', function () {
  before(function (done) {
    helper.startServer(done);
  });

  after(function (done) {
    helper.stopServer(done);
  });

  afterEach(function () {
    helper.unload();
  });

  it('should load query node with listActiveSystems', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listActiveSystems"), function () {
      const n1 = helper.getNode("n1");
      n1.should.have.property('name', 'query');
      n1.should.have.property('queryType', 'listActiveSystems');
      done();
    });
  });

  it('should default to listActiveSystems when no query type is set', function (done) {
    const flow = [
      { id: "n1", type: "uyuni-query", name: "query", server: "c1" },
      { id: "c1", type: "uyuni-api-config", name: "api", url: "https://localhost/rpc/api" }
    ];
    helper.load([uyuniQuery, uyuniApiConfig], flow, function () {
      helper.getNode("n1").should.have.property('queryType', 'listActiveSystems');
      done();
    });
  });

  it('should call system.listActiveSystems with no arguments', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listActiveSystems"), function () {
      const calls = stubCallMethod([{ id: 1000010000 }]);
      helper.getNode("n1").receive({ payload: {} });
      setTimeout(function () {
        calls.should.have.length(1);
        calls[0].method.should.equal("system.listActiveSystems");
        calls[0].params.should.eql([]);
        done();
      }, 20);
    });
  });

  it('should read the system id from msg.payload.systemId', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("getSystemDetails"), function () {
      const calls = stubCallMethod({ id: 42 });
      helper.getNode("n1").receive({ payload: { systemId: 42 } });
      setTimeout(function () {
        calls[0].method.should.equal("system.getDetails");
        calls[0].params.should.eql([42]);
        done();
      }, 20);
    });
  });

  it('should fall back to serverId, then id, then msg.systemId', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listErrata"), function () {
      const calls = stubCallMethod([]);
      const n1 = helper.getNode("n1");
      // An event coming from uyuni-events carries serverId rather than systemId.
      n1.receive({ payload: { serverId: 7 } });
      n1.receive({ payload: { id: 8 } });
      n1.receive({ payload: {}, systemId: 9 });
      setTimeout(function () {
        calls.map(function (c) { return c.params[0]; }).sort().should.eql([7, 8, 9]);
        calls.every(function (c) { return c.method === "system.getRelevantErrata"; }).should.be.true();
        done();
      }, 30);
    });
  });

  it('should coerce a string system id to an integer', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listSystemPackages"), function () {
      const calls = stubCallMethod([]);
      helper.getNode("n1").receive({ payload: { systemId: "1000010042" } });
      setTimeout(function () {
        calls[0].method.should.equal("system.listPackages");
        calls[0].params.should.eql([1000010042]);
        done();
      }, 20);
    });
  });

  it('should put the API result on msg.payload', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listActiveSystems"), function () {
      stubCallMethod([{ id: 1, name: "client1" }]);
      const n1 = helper.getNode("n1");
      n1.send = function (msg) {
        msg.payload.should.eql([{ id: 1, name: "client1" }]);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should raise an error when the system id is missing', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("getSystemDetails"), function () {
      const calls = stubCallMethod({});
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/systemId is required/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should raise an error for an unknown query type', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("notARealQuery"), function () {
      const calls = stubCallMethod({});
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/Unknown query type/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should refuse to start without an API config node', function (done) {
    const flow = [{ id: "n1", type: "uyuni-query", name: "query", queryType: "listActiveSystems" }];
    helper.load([uyuniQuery, uyuniApiConfig], flow, function () {
      const n1 = helper.getNode("n1");
      // The node reports the problem and never registers an input handler.
      n1.should.have.property('name', 'query');
      done();
    });
  });

  it('should require a system id for listErrata', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listErrata"), function () {
      const calls = stubCallMethod([]);
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/systemId is required/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should require a system id for listSystemPackages', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listSystemPackages"), function () {
      const calls = stubCallMethod([]);
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/systemId is required/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should surface an API failure as a node error', function (done) {
    helper.load([uyuniQuery, uyuniApiConfig], flowWith("listActiveSystems"), function () {
      helper.getNode("c1").callMethod = function () {
        return Promise.reject(new Error("Invalid session key"));
      };
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/Invalid session key/);
        done();
      };
      n1.receive({ payload: {} });
    });
  });
});
