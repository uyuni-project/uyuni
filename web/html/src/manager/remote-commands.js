'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Buttons = require("../components/buttons");
const Network = require("../utils/network");
const AsyncButton = Buttons.AsyncButton;

function object2map(obj) {
  return Object.keys(obj).reduce((acc, id) => {
    acc.set(id, obj[id]);
    return acc;
  }, new Map());
}

class MinionResultView extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      open: false,
    };
    ["onClick"]
    .forEach(method => this[method] = this[method].bind(this));
  }

  onClick() {
    if (this.props.result != "pending" && this.props.result != "timedOut" && this.props.result != "matched" && this.props.result != "error") {
      this.setState({open: !this.state.open})
    }
  }

  render() {
    const id = this.props.id;
    const resultType = this.props.result.type;
    const value = this.props.result.value;
    const props = this.props;
    return (
      <div className="panel panel-default">
        <div id={id} className="panel-heading" onClick={this.onClick} style={props.result ? {cursor: "pointer"} : {cursor: "default"}}>
           <span>{id}</span>
           {(() => {
              if(resultType == "pending") {
                  return (
                    <div className="badge pull-right">
                       {t("pending")}
                    </div>
                  );
              } else if(resultType == "timedOut") {
                  return (
                    <div className="pull-right">
                       <div className="badge">
                           {t("timed out")}
                       </div>
                       <i className="fa fa-right fa-warning fa-1-5x"></i>
                    </div>
                  );
//              } else if(resultType == "waitCanceled") {
//                  return (
//                    <div className="pull-right">
//                       <div className="badge">
//                           {t("canceled wait")}
//                       </div>
//                       <i className="fa fa-right fa-times-circle fa-1-5x"></i>
//                    </div>
//                  );
              } else if(resultType == "matched") {
                  // nothing
              } else if(resultType == "error") {
                  return (
                    <div className="pull-right">
                       <div className="badge">
                          {this.state.open ? t("- hide error -") : t("- show error -")}
                       </div>
                       <i className="fa fa-right fa-warning fa-1-5x"></i>
                    </div>
                  );
              } else if(resultType == "result") {
                  return(
                    <div className="pull-right">
                        <div className="badge">
                          {this.state.open ? t("- hide response -") : t("- show response -")}
                        </div>
                        <i className="fa fa-right fa-check-circle fa-1-5x"></i>
                    </div>
                  );
              }
           })()}
        </div>
        {
          this.state.open && value ?
            <div className="panel-body">
               <pre id={id + '-results'}>{value}</pre>
            </div>
            : undefined
        }
      </div>
    )
  }
}

function isPreviewDone(minionsMap) {
  return Array.from(minionsMap, (e) => e[1]).every((v) => v.type == "matched" || v.type == "timedOut");
}

function isRunDone(minionsMap) {
  return Array.from(minionsMap, (e) => e[1]).every((v) => v.type == "result" || v == "timedOut");
}

function isTimedOutDone(minionsMap) {
  const results = Array.from(minionsMap, (e) => e[1]);
  return results.every((v) => v.type != "pending") && results.some((v) => v.type == "timedOut");
}

class RemoteCommand extends React.Component {

  constructor() {
    super();
    ["onPreview", "onRun", "onStop", "commandChanged", "targetChanged", "commandResult"]
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
      command: "ls -lha",
      target: "*",
      result: {
        minions: new Map()
      },
      previewed: $.Deferred(),
      ran: $.Deferred().resolve(),
      executing: $.Deferred().resolve(),
      errors: []
    };
  }

  render() {
    var errs = null;
    const style = {
        paddingBottom: "0px"
    }
    if (this.state.errors) {
        this.state.errors.map( msg => {
            errs = <div className="alert alert-danger">{msg}</div>
        })
    }

    return (
      <div>
          {errs}
          <div id="remote-root" className="spacewalk-toolbar-h1">
            <h1>
              <i className="fa fa-desktop"></i>
              {t("Remote Commands")}
            </h1>
          </div>
          <div className="panel panel-default">
            <div className="panel-body">
              <div className="row">
                <div className="col-lg-12">
                  <div className="input-group">
                      <input id="command" className="form-control" type="text" defaultValue={this.state.command} onChange={this.commandChanged} />
                      <span className="input-group-addon">@</span>
                      <input id="target" className="form-control" type="text" defaultValue={this.state.target} onChange={this.targetChanged} />
                      <div className="input-group-btn">
                        <AsyncButton id="preview" disabled={this.state.ran.state() == "resolved" ? "" : "disabled"}
                                name={t("Preview")} action={this.onPreview} icon="eye" title={t("Check which minions match the target expression")}/>
                        <AsyncButton id="run" disabled={this.state.previewed.state() == "resolved" ? "" : "disabled"}
                                name={t("Run")} action={this.onRun} icon="play" title={t("Run the command on the target minions")}/>
                        <AsyncButton id="stop" disabled={this.state.executing.state() == "pending" ? "" : "disabled"}
                                name={t("Stop")} action={this.onStop} icon="stop" title={t("Stop waiting for all the minions to respond")}/>
                      </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="panel panel-default">
            <div className="panel-heading">
              <h4>
                <span>{t("Target systems")}</span>
                <span>{this.state.result.minions.size ? " (" + this.state.result.minions.size + ")" : undefined}</span>
              </h4>
            </div>
            <div className="panel-body" style={this.state.result.minions.size ? style : undefined}>
              {(() => {
                if (!this.state.result.minions.size) {
                   return(<span>{this.state.previewed.state() != "pending" ? t("No target systems previewed") : t("No target systems have been found")}</span>)
                } else {
                  return(this.commandResult(this.state.result))
                }
              })()}
            </div>
          </div>
      </div>
    );
  }

  onPreview() {
    const deferred = $.Deferred();
    this.state.websocket.send(JSON.stringify({
                              preview: true,
                              target: this.state.target
                            }));
    this.setState({
        errors: [],
        previewed: deferred,
        result: {
          minions: new Map()
        },
    });
    return deferred;
  }

  onRun() {
      const deferred = $.Deferred();
      this.state.websocket.send(JSON.stringify({
                                preview: false,
                                target: this.state.target,
                                command: this.state.command
                              }));
      this.setState({
          errors: [],
          ran: deferred,
      });
      return deferred;
  }

  onStop() {
      this.state.websocket.send(JSON.stringify({
                                cancel: true
                              }));
  }

  componentDidMount() {
    var ws = new WebSocket("wss://" + window.location.hostname + "/rhn/websocket/minion/remote-commands", "protocolOne");
    ws.onopen = () => {
      console.log("Websocket opened");
    };
    ws.onerror = (e) => {
        this.setState({
            errors: [t("Error connecting to server.")]
        });
       console.log(e);
    };
    ws.onclose = (e) => {
       console.log("Websocket closed");
    };
    ws.onmessage = (e) => {
      console.log("Got websocket message: " + e.data);
      var event = JSON.parse(e.data);
      switch(event.type) {
        case "asyncJobStart":
            this.setState({
                executing: $.Deferred(),
                result: {
                    errors: [],
                    minions: event.minions.reduce((map, minionId) => map.set(minionId, {type: "pending", value: null}), new Map())
                }
            });
            break;
        case "match":
            var minionsMap = this.state.result.minions;
            minionsMap.set(event.minion, {type: "matched", value: null});
            if (isPreviewDone(minionsMap)) {
                this.state.previewed.resolve();
                this.state.executing.resolve();
            }
            this.setState({
                result: {
                    minions: minionsMap
                }
            });
            break;
        case "runResult":
            var minionsMap = this.state.result.minions;
            minionsMap.set(event.minion, {type: "result", value: event.out});
            if (isRunDone(minionsMap)) {
                this.state.ran.resolve();
                this.state.executing.resolve();
            }
            this.setState({
                result: {
                    minions: minionsMap
                }
            });
            break;
        case "timedOut":
            var minionsMap = this.state.result.minions;
            minionsMap.set(event.minion, {type: "timedOut", value: null});
            const timedOutDone = isTimedOutDone(minionsMap);
            this.setState({
                errors: timedOutDone ? [t("Not all minions responded.")] : [],
                previewed: this.state.previewed.state() == "pending" ? this.state.previewed.resolve() : this.state.previewed,
                ran: this.state.ran.state() == "pending" ? this.state.ran.resolve() : this.state.ran,
                executing: timedOutDone ? this.state.executing.resolve() : this.state.executing,
                result: {
                    minions: minionsMap
                }
            });
            break;
//        case "waitCanceled":
//            var minionsMap = this.state.result.minions;
//            minionsMap.set(event.minion, {type: "waitCanceled", value: null});
//            const waitCanceled = isWaitCanceled(minionsMap);
//            if (waitCanceled) {
//                this.state.ran.resolve();
//                this.state.previewed.resolve();
//                this.state.executing.resolve();
//            }
//            this.setState() {
//                errors: waitCanceled ? [t("Canceled waiting for action to complete.")] : [],
//                result: {
//                    minions: minionsMap
//                }
//            }
        case "error":
            if (event.minion) {
                var minionsMap = this.state.result.minions;
                minionsMap.set(event.minion, {type: "error", value: event.message});
                this.setState({
                    result: {
                        minions: minionsMap
                    }
                });
            } else {
                this.setState({
                    result: {
                        errors: [t("Could not complete call: {0}", event.message)],
                    }
                });
            }
            break;
      }
    };
    this.setState({
        websocket: ws,
    });
  }

  targetChanged(event) {
    this.setState({
      target: event.target.value,
      previewed: $.Deferred()
    });
  }

  commandChanged(event) {
    this.setState({
      command: event.target.value
    });
  }

  commandResult(result) {
    const elements = [];
    for(var kv of result.minions) {
      const id = kv[0];
      const value = kv[1]
      elements.push(
        <MinionResultView key={id} id={id} result={value}/>
      );
    }
    return <div>{elements}</div>;
  }
}

function errorMessageByStatus(status) {
  if (status == 401) {
    return [t("Session expired, please reload the page to run command on systems.")];
  }
  else if (status == 403) {
    return [t("Authorization error, please reload the page or try to logout/login again.")];
  }
  else if (status >= 500) {
    return [t("Server error, please check log files.")];
  }
  else {
    return [];
  }
}

ReactDOM.render(
  <RemoteCommand />,
  document.getElementById('remote-commands')
);
