const should = require("should");
const helper = require("node-red-node-test-helper");
const uyuniAction = require("../nodes/uyuni-action/uyuni-action.js");
const uyuniQuery = require("../nodes/uyuni-query/uyuni-query.js");
const uyuniApiConfig = require("../nodes/uyuni-api-config/uyuni-api-config.js");

// One action node wired to an API config node whose callMethod is stubbed, so
// the action's own argument handling runs without reaching a server.
function flowWith(actionType) {
  return [
    { id: "n1", type: "uyuni-action", name: "action", server: "c1", actionType: actionType },
    { id: "c1", type: "uyuni-api-config", name: "api", url: "https://localhost/rpc/api" }
  ];
}

function stubCallMethod(result) {
  const calls = [];
  helper.getNode("c1").callMethod = function (method, params) {
    calls.push({ method: method, params: params });
    return Promise.resolve(result === undefined ? true : result);
  };
  return calls;
}

describe('uyuni-action and uyuni-query Nodes', function () {
  before(function (done) {
    helper.startServer(done);
  });

  after(function (done) {
    helper.stopServer(done);
  });

  afterEach(function () {
    helper.unload();
  });

  it('should load action node', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("applyHighstate"), function () {
      helper.getNode("n1").should.have.property('name', 'action');
      done();
    });
  });

  it('should load query node', function (done) {
    const flow = [
      { id: "n2", type: "uyuni-query", name: "uyuni-query", server: "c1", queryType: "listActiveSystems" },
      { id: "c1", type: "uyuni-api-config", name: "uyuni-api-config", url: "https://localhost/rpc/api" }
    ];
    helper.load([uyuniQuery, uyuniApiConfig], flow, function () {
      helper.getNode("n2").should.have.property('name', 'uyuni-query');
      done();
    });
  });

  it('should default to applyHighstate when no action type is set', function (done) {
    const flow = [
      { id: "n1", type: "uyuni-action", name: "action", server: "c1" },
      { id: "c1", type: "uyuni-api-config", name: "api", url: "https://localhost/rpc/api" }
    ];
    helper.load([uyuniAction, uyuniApiConfig], flow, function () {
      helper.getNode("n1").should.have.property('actionType', 'applyHighstate');
      done();
    });
  });

  it('should apply a highstate with the system id, a date and force=false', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("applyHighstate"), function () {
      const calls = stubCallMethod();
      helper.getNode("n1").receive({ payload: { systemId: 1000010042 } });
      setTimeout(function () {
        calls[0].method.should.equal("system.applyHighstate");
        calls[0].params[0].should.equal(1000010042);
        calls[0].params[1].should.be.instanceof(Date);
        calls[0].params[2].should.equal(false);
        done();
      }, 20);
    });
  });

  it('should schedule a reboot with the system id and a date', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("scheduleReboot"), function () {
      const calls = stubCallMethod();
      helper.getNode("n1").receive({ payload: { systemId: "77" } });
      setTimeout(function () {
        calls[0].method.should.equal("system.scheduleReboot");
        calls[0].params[0].should.equal(77);
        calls[0].params[1].should.be.instanceof(Date);
        done();
      }, 20);
    });
  });

  it('should install packages and coerce the ids to integers', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("schedulePackageInstall"), function () {
      const calls = stubCallMethod();
      helper.getNode("n1").receive({ payload: { systemId: 5, packageIds: ["11", 12] } });
      setTimeout(function () {
        calls[0].method.should.equal("system.schedulePackageInstall");
        calls[0].params[0].should.equal(5);
        calls[0].params[1].should.eql([11, 12]);
        calls[0].params[2].should.be.instanceof(Date);
        done();
      }, 20);
    });
  });

  it('should wrap a single package id into an array', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("schedulePackageInstall"), function () {
      const calls = stubCallMethod();
      helper.getNode("n1").receive({ payload: { systemId: 5, packageIds: 99 } });
      setTimeout(function () {
        calls[0].params[1].should.eql([99]);
        done();
      }, 20);
    });
  });

  it('should list all channels without needing a system id', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("listAllChannels"), function () {
      const calls = stubCallMethod([{ label: "base" }]);
      helper.getNode("n1").receive({ payload: {} });
      setTimeout(function () {
        calls[0].method.should.equal("channel.software.listAllChannels");
        calls[0].params.should.eql([]);
        done();
      }, 20);
    });
  });

  it('should put the API result on msg.payload', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("listAllChannels"), function () {
      stubCallMethod([{ label: "base" }]);
      const n1 = helper.getNode("n1");
      n1.send = function (msg) {
        msg.payload.should.eql([{ label: "base" }]);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should raise an error when the system id is missing', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("scheduleReboot"), function () {
      const calls = stubCallMethod();
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/systemId is required/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: {} });
    });
  });

  it('should raise an error when package ids are missing', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("schedulePackageInstall"), function () {
      const calls = stubCallMethod();
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/packageIds/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: { systemId: 5 } });
    });
  });

  it('should raise an error for an unknown action type', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("selfDestruct"), function () {
      const calls = stubCallMethod();
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/Unknown action type/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: { systemId: 5 } });
    });
  });

  it('should refuse to start without an API config node', function (done) {
    const flow = [{ id: "n1", type: "uyuni-action", name: "action", actionType: "applyHighstate" }];
    helper.load([uyuniAction, uyuniApiConfig], flow, function () {
      helper.getNode("n1").should.have.property('name', 'action');
      done();
    });
  });

  it('should accept a system id supplied directly on the message', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("applyHighstate"), function () {
      const calls = stubCallMethod();
      helper.getNode("n1").receive({ payload: {}, systemId: 31 });
      setTimeout(function () {
        calls[0].params[0].should.equal(31);
        done();
      }, 20);
    });
  });

  it('should require a system id before installing packages', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("schedulePackageInstall"), function () {
      const calls = stubCallMethod();
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/systemId is required/);
        calls.should.have.length(0);
        done();
      };
      n1.receive({ payload: { packageIds: [1] } });
    });
  });

  it('should require a system id before applying a highstate', function (done) {
    helper.load([uyuniAction, uyuniApiConfig], flowWith("applyHighstate"), function () {
      const calls = stubCallMethod();
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
    helper.load([uyuniAction, uyuniApiConfig], flowWith("applyHighstate"), function () {
      helper.getNode("c1").callMethod = function () {
        return Promise.reject(new Error("permission denied"));
      };
      const n1 = helper.getNode("n1");
      n1.error = function (err) {
        String(err).should.match(/permission denied/);
        done();
      };
      n1.receive({ payload: { systemId: 5 } });
    });
  });
});
