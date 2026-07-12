const should = require("should");
const helper = require("node-red-node-test-helper");
const uyuniAction = require("../nodes/uyuni-action/uyuni-action.js");
const uyuniQuery = require("../nodes/uyuni-query/uyuni-query.js");
const uyuniApiConfig = require("../nodes/uyuni-api-config/uyuni-api-config.js");

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
    const flow = [
      { id: "n1", type: "uyuni-action", name: "uyuni-action", server: "c1", actionType: "applyHighstate" },
      { id: "c1", type: "uyuni-api-config", name: "uyuni-api-config", url: "https://localhost/rpc/api" }
    ];
    helper.load([uyuniAction, uyuniApiConfig], flow, function () {
      const n1 = helper.getNode("n1");
      n1.should.have.property('name', 'uyuni-action');
      done();
    });
  });

  it('should load query node', function (done) {
    const flow = [
      { id: "n2", type: "uyuni-query", name: "uyuni-query", server: "c1", queryType: "listActiveSystems" },
      { id: "c1", type: "uyuni-api-config", name: "uyuni-api-config", url: "https://localhost/rpc/api" }
    ];
    helper.load([uyuniQuery, uyuniApiConfig], flow, function () {
      const n2 = helper.getNode("n2");
      n2.should.have.property('name', 'uyuni-query');
      done();
    });
  });
});
