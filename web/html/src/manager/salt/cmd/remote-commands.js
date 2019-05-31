/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {Button} = require("components/buttons");
const { TopPanel } = require('components/panels/TopPanel')
const Network = require("utils/network");

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
           <span>{ props.label ? props.label : id }</span>
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
                       <i className="fa fa-right fa-warning text-warning fa-1-5x"></i>
                    </div>
                  );
              } else if(resultType == "matched") {
                  // nothing
              } else if(resultType == "error") {
                  return (
                    <div className="pull-right">
                       <div className="badge">
                          {this.state.open ? t("- hide error -") : t("- show error -")}
                       </div>
                       <i className="fa fa-right fa-warning text-danger fa-1-5x"></i>
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

function isPreviewDone(minionsMap, waitForSSH) {
  return Array.from(minionsMap, (e) => e[1])
        .every((v) => v.type == "matched" || v.type == "timedOut" || v.type == "error") &&
        !waitForSSH
}

function isRunDone(minionsMap) {
  return Array.from(minionsMap, (e) => e[1])
        .every((v) => v.type == "result" || v == "timedOut" || v.type == "error");
}

function isTimedOutDone(minionsMap, waitForSSH, timedOutSSH) {
  if (!minionsMap && minionsMap.size == 0) {
    return timedOutSSH;
  }
  const results = Array.from(minionsMap, (e) => e[1]);
  const noMinionsPending = results.every((v) => v.type != "pending")
  const anyTimedOutMinion = results.some((v) => v.type == "timedOut");
  if (waitForSSH) {
    return timedOutSSH && noMinionsPending;
  }
  return noMinionsPending && anyTimedOutMinion;
}

class RemoteCommand extends React.Component {

  constructor() {
    super();
    ["onPreview", "onRun", "onStop", "commandChanged", "targetChanged", "commandResult", "onBeforeUnload"]
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
      command: "ls -lha",
      target: "*",
      result: {
        minions: new Map()
      },
      previewed: $.Deferred(),
      ran: $.Deferred(),
      executing: $.Deferred().resolve(),
      errors: [],
      warnings: []
    };
  }

  render() {
    var msgs = [];
    const style = {
        paddingBottom: "0px"
    }
    if (this.state.errors) {
        this.state.errors.map( msg => {
            msgs.push(<div className="alert alert-danger">{msg}</div>);
        });
    }
    if (this.state.warnings) {
        this.state.warnings.map( msg => {
            msgs.push(<div className="alert alert-warning">{msg}</div>);
        });
    }

    var button;
    if (this.state.executing.state() == "pending") {
        button = <Button id="stop" className="btn-default"
                     text={t("Stop waiting")} handler={this.onStop}
                     icon="fa-circle-o-notch fa-spin"
                     title={t("Stop waiting for all the minions to respond")}/>;
    } else if (this.state.ran.state() == "resolved" && this.state.previewed.state() != "resolved") {
        button = <Button id="preview" className="btn-default"
                     text={t("Find targets")} handler={this.onPreview}
                     icon="fa-search"
                     title={t("Check which minions match the target expression")}/>;
    } else if (this.state.previewed.state() == "resolved") {
        button = [
                <Button id="preview" className="btn-default"
                    handler={this.onPreview}
                    icon="fa-search"
                    title={t("Check which minions match the target expression")}/>,
                <Button id="run" className="btn-default"
                    text={t("Run command")} handler={this.onRun}
                    icon="fa-play"
                    title={t("Run the command on the target minions")}/>
        ];
    }

    return (
      <div>
          {msgs}
          <TopPanel title={t("Remote Commands")} icon="fa-desktop" helpUrl="/docs/reference/salt/salt-remote-commands.html" />
          <div className="panel panel-default">
            <div className="panel-body">
              <div className="row">
                <div className="col-lg-12">
                  <div className="input-group">
                      <input id="command" className="form-control" type="text" defaultValue={this.state.command} onChange={this.commandChanged}
                            disabled={this.state.executing.state() == "pending" ? "disabled" : ""}/>
                      <span className="input-group-addon">@</span>
                      <input id="target" className="form-control" type="text" defaultValue={this.state.target} onChange={this.targetChanged}
                            disabled={this.state.executing.state() == "pending" ? "disabled" : ""}/>
                      <div className="input-group-btn">
                        { button }
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
                if (!this.state.result.minions.size && !this.state.result.waitForSSH) {
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
        warnings: [],
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
          warnings: [],
          ran: deferred,
      });
      return deferred;
  }

  onStop() {
      this.state.websocket.send(JSON.stringify({
                                cancel: true
                              }));
  }

  onBeforeUnload(event) {
      this.setState({
          pageUnloading: true
      });
  }

  componentDidMount() {
    var port = window.location.port;
    var url = "wss://" +
      window.location.hostname +
      (port ? ":" + port : "") +
       "/rhn/websocket/minion/remote-commands";
    var ws = new WebSocket(url);
    ws.onopen = () => {
      this.setState({
          previewed: $.Deferred(),
          ran: $.Deferred().resolve(),
          executing: $.Deferred().resolve(),
      });
    };
    ws.onclose = (e) => {
      var errs = this.state.errors ? this.state.errors : [];
      if (!this.state.pageUnloading && !this.state.websocketErr) {
          errs.push(t("Websocket connection closed. Refresh the page to try again."));
      }
      if (this.state.ran) {
          this.state.ran.resolve();
      }
      if (this.state.previewed) {
          this.state.previewed.resolve();
      }
      if (this.state.executing) {
          this.state.executing.resolve();
      }
      this.setState({
          errors: errs,
          previewed: $.Deferred(),
          ran: $.Deferred(),
      });
    };
    ws.onerror = (e) => {
      console.log("Websocket error: " + e);
      this.setState({
         errors: [t("Error connecting to server. Refresh the page to try again.")],
         websocketErr: true
      });
    };
    ws.onmessage = (e) => {
      var event = JSON.parse(e.data);
      switch(event.type) {
        case "asyncJobStart":
            this.setState({
                executing: $.Deferred(),
                errors: [],
                warnings: [],
                result: {
                    minions: event.minions.reduce((map, minionId) => map.set(minionId, {type: "pending", value: null}), new Map()),
                    waitForSSH: event.waitForSSHMinions
                }
            });
            break;
        case "match":
            var minionsMap = this.state.result.minions;
            minionsMap.set(event.minion, {type: "matched", value: null});
            if (isPreviewDone(minionsMap, this.state.result.waitForSSH)) {
                this.state.previewed.resolve();
                this.state.executing.resolve();
            }
            this.setState({
                result: {
                    minions: minionsMap,
                    waitForSSH: this.state.result.waitForSSH
                }
            });
            break;
        case "matchSSH":
            var minionsMap = event.minions.reduce(
                (map, minionId) => map.set(minionId, {type: "matched", value: null}),
                 this.state.result.minions);
            if (isPreviewDone(minionsMap, false)) {
                this.state.previewed.resolve();
                this.state.executing.resolve();
            }
            this.setState({
                result: {
                    minions: minionsMap,
                    waitForSSH: false
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
            var waitForSSH = this.state.result.waitForSSH;
            var timedOutSSH = this.state.result.timedOutSSH;
            var timedOutDone;
            if (event.minion) {
                minionsMap.set(event.minion, {type: "timedOut", value: null});
                timedOutDone = isTimedOutDone(minionsMap,
                    waitForSSH,
                    this.state.result.timedOutSSH);
            } else if (event.timedOutSSH) {
                timedOutDone = isTimedOutDone(minionsMap, waitForSSH, true);
                waitForSSH = false;
                timedOutSSH = true;
            } else {
                timedOutDone = true;
            }
            var previewed = this.state.previewed;
            var ran = this.state.ran;
            if (timedOutDone) {
                if (previewed.state() == "pending") {
                    previewed.resolve();
                    ran = $.Deferred();
                } else if (ran.state() == "pending") {
                    previewed.resolve();
                    ran.resolve();
                }
            }
            this.setState({
                warnings: timedOutDone ? [t("Not all minions responded on time.")] : [],
                previewed: previewed,
                ran: ran,
                executing: timedOutDone ? this.state.executing.resolve() : this.state.executing,
                result: {
                    minions: minionsMap,
                    waitForSSH: waitForSSH,
                    timedOutSSH: timedOutSSH
                }
            });
            break;
        case "error":
            var globalErr = [];
            if (event.minion) {
                var minionsMap = this.state.result.minions;
                minionsMap.set(event.minion, {type: "error", value: event.message});
                this.setState({
                    result: {
                        minions: minionsMap
                    }
                });
            } else if (event.code == "INVALID_SESSION") {
                window.location.href = "/rhn/Login2.do";
                return;
            } else if (event.code == "ERR_TARGET_NO_MATCH") {
                globalErr = [t("No minions matched the target expression.")];
            } else if (!event.minion) {
                globalErr = [t("Server returned an error: {0}", event.message)];
            }

            const noPending = Array.from(minionsMap, (e) => e[1])
                    .every((v) => v.type != "pending") &&
                    !this.state.result.waitForSSH;
            if (noPending) {
                var previewed = this.state.previewed;
                var ran = this.state.ran;
                if (previewed && previewed.state() == "pending") {
                    previewed = $.Deferred();
                    ran = ran.resolve();
                } else if (ran && ran.state() == "pending") {
                    previewed = previewed.resolve();
                    ran = ran.resolve();
                }
                if (this.state.executing) {
                    this.state.executing.resolve();
                }
                this.setState({
                    previewed: previewed,
                    ran: ran,
                    errors: globalErr
                });
            }
            break;
      }
    };

    window.addEventListener("beforeunload", this.onBeforeUnload)

    this.setState({
        websocket: ws
    });

  }

  componentWillUnmount() {
    window.removeEventListener("beforeunload", this.onBeforeUnload)
  }


  targetChanged(event) {
    this.setState({
      target: event.target.value,
      previewed: $.Deferred(),
      ran: $.Deferred().resolve()
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
      const value = kv[1];
      elements.push(
        <MinionResultView key={id} id={id} result={value}/>
      );
    }
    if (result.waitForSSH) {
        elements.push(
            <MinionResultView key="waitForSSH" id="waitForSSH" label={t("Matching ssh-push minions...")} result={ {type: "pending", value: null} }/>
        );
    } else if (result.timedOutSSH) {
        elements.push(
            <MinionResultView key="waitForSSH" id="waitForSSH" label={t("Matching ssh-push minions...")} result={ {type: "timedOut", value: null} }/>
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
