const should = require("should");
const helper = require("node-red-node-test-helper");
const proxyquire = require("proxyquire").noCallThru();

// Record what the node asks of xmlrpc, and control what the server "returns",
// so the session handling can be exercised without a Uyuni server.
let created = [];
let responder = null;

function fakeClient() {
  return {
    methodCall: function (method, params, cb) {
      responder(method, params, cb);
    }
  };
}

const uyuniApiConfig = proxyquire("../nodes/uyuni-api-config/uyuni-api-config.js", {
  xmlrpc: {
    createSecureClient: function (opts) { created.push({ secure: true, opts: opts }); return fakeClient(); },
    createClient: function (opts) { created.push({ secure: false, opts: opts }); return fakeClient(); }
  }
});

function flow(extra) {
  const cfg = { id: "c1", type: "uyuni-api-config", name: "api", url: "https://uyuni.example.com/rpc/api" };
  Object.assign(cfg, extra || {});
  return [cfg];
}

describe('uyuni-api-config Node', function () {
  before(function (done) { helper.startServer(done); });
  after(function (done) { helper.stopServer(done); });

  beforeEach(function () {
    created = [];
    responder = null;
  });

  afterEach(function () {
    helper.unload();
  });

  it('should parse host, port and path from an https URL', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      responder = function (method, params, cb) { cb(null, "session-1"); };
      helper.getNode("c1").callMethod("system.listActiveSystems", []).then(function () {
        created[0].secure.should.be.true();
        created[0].opts.host.should.equal("uyuni.example.com");
        created[0].opts.port.should.equal(443);
        created[0].opts.path.should.equal("/rpc/api");
        done();
      }).catch(done);
    });
  });

  it('should default to port 80 for a plain http URL', function (done) {
    helper.load([uyuniApiConfig], flow({ url: "http://uyuni.example.com/rpc/api" }), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].secure.should.be.false();
        created[0].opts.port.should.equal(80);
        done();
      }).catch(done);
    });
  });

  it('should honour an explicit port', function (done) {
    helper.load([uyuniApiConfig], flow({ url: "https://uyuni.example.com:8443/rpc/api" }), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].opts.port.should.equal(8443);
        done();
      }).catch(done);
    });
  });

  it('should verify certificates by default', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].opts.rejectUnauthorized.should.be.true();
        done();
      }).catch(done);
    });
  });

  it('should skip verification and warn when self-signed is allowed', function (done) {
    helper.load([uyuniApiConfig], flow({ allowSelfSigned: true }), function () {
      const node = helper.getNode("c1");
      node.warn.called.should.be.true();
      String(node.warn.firstCall.args[0]).should.match(/development only/);
      responder = function (method, params, cb) { cb(null, "ok"); };
      node.callMethod("ping", []).then(function () {
        created[0].opts.rejectUnauthorized.should.be.false();
        done();
      }).catch(done);
    });
  });

  it('should log in first and prepend the session key to the call', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      const seen = [];
      responder = function (method, params, cb) {
        seen.push({ method: method, params: params });
        cb(null, method === "auth.login" ? "SESSION-ABC" : ["result"]);
      };
      helper.getNode("c1").callMethod("system.getDetails", [42]).then(function (result) {
        seen[0].method.should.equal("auth.login");
        seen[1].method.should.equal("system.getDetails");
        seen[1].params.should.eql(["SESSION-ABC", 42]);
        result.should.eql(["result"]);
        done();
      }).catch(done);
    });
  });

  it('should reuse the cached session on a second call', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      let logins = 0;
      responder = function (method, params, cb) {
        if (method === "auth.login") { logins++; return cb(null, "S1"); }
        cb(null, "ok");
      };
      const node = helper.getNode("c1");
      node.callMethod("a", []).then(function () {
        return node.callMethod("b", []);
      }).then(function () {
        logins.should.equal(1);
        done();
      }).catch(done);
    });
  });

  it('should log in again once when the session expires', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      let logins = 0;
      let firstCall = true;
      responder = function (method, params, cb) {
        if (method === "auth.login") { logins++; return cb(null, "S" + logins); }
        if (firstCall) {
          firstCall = false;
          return cb(new Error("Invalid session key"));
        }
        cb(null, "recovered");
      };
      helper.getNode("c1").callMethod("system.getDetails", [1]).then(function (result) {
        result.should.equal("recovered");
        logins.should.equal(2);
        done();
      }).catch(done);
    });
  });

  it('should give up after one retry rather than looping forever', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      let logins = 0;
      responder = function (method, params, cb) {
        if (method === "auth.login") { logins++; return cb(null, "S" + logins); }
        // The server keeps reporting a session problem.
        cb(new Error("Invalid session key"));
      };
      helper.getNode("c1").callMethod("system.getDetails", [1]).then(function () {
        done(new Error("should not have resolved"));
      }).catch(function (err) {
        String(err).should.match(/Invalid session key/);
        // One initial login plus exactly one retry login.
        logins.should.equal(2);
        done();
      });
    });
  });

  it('should not retry a fault that is unrelated to the session', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      let logins = 0;
      responder = function (method, params, cb) {
        if (method === "auth.login") { logins++; return cb(null, "S1"); }
        cb(new Error("invalid parameter: serverId"));
      };
      helper.getNode("c1").callMethod("system.getDetails", [1]).then(function () {
        done(new Error("should not have resolved"));
      }).catch(function (err) {
        String(err).should.match(/invalid parameter/);
        logins.should.equal(1);
        done();
      });
    });
  });

  it('should reject when the login itself fails', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      responder = function (method, params, cb) {
        cb(new Error("Either the password or username is incorrect"));
      };
      helper.getNode("c1").callMethod("system.getDetails", [1]).then(function () {
        done(new Error("should not have resolved"));
      }).catch(function (err) {
        String(err).should.match(/username is incorrect/);
        done();
      });
    });
  });
  it('should default the request timeout to 30 seconds', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].opts.timeout.should.equal(30000);
        done();
      }).catch(done);
    });
  });

  it('should pass a configured timeout to the client', function (done) {
    helper.load([uyuniApiConfig], flow({ timeout: 5000 }), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].opts.timeout.should.equal(5000);
        done();
      }).catch(done);
    });
  });

  it('should ignore a non-numeric or zero timeout and use the default', function (done) {
    helper.load([uyuniApiConfig], flow({ timeout: "not-a-number" }), function () {
      responder = function (method, params, cb) { cb(null, "ok"); };
      helper.getNode("c1").callMethod("ping", []).then(function () {
        created[0].opts.timeout.should.equal(30000);
        done();
      }).catch(done);
    });
  });

  it('should log in only once when several calls start at the same time', function (done) {
    helper.load([uyuniApiConfig], flow(), function () {
      let logins = 0;
      responder = function (method, params, cb) {
        if (method === "auth.login") {
          logins++;
          // Resolve on a later tick so all three calls are in flight while the
          // first login is still outstanding. Without a shared promise each
          // would start its own login.
          setTimeout(function () { cb(null, "session-1"); }, 10);
          return;
        }
        cb(null, "ok");
      };
      const node = helper.getNode("c1");
      Promise.all([
        node.callMethod("system.getDetails", [1]),
        node.callMethod("system.getDetails", [2]),
        node.callMethod("system.getDetails", [3])
      ]).then(function () {
        logins.should.equal(1);
        done();
      }).catch(done);
    });
  });
});
