'use strict';

const React = require("react");
const Buttons = require("../components/buttons");

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
    this.setState({open: !this.state.open});
  }

  render() {
    const id = this.props.id;
    const result = this.props.result;
    const props = this.props;
    const style = {
      margin: "0px"
    };
    return (
      <div className="panel panel-default" style={style}>
        <div className="panel-heading" onClick={this.onClick}>
           <span>{id}</span>
           {(() => {
              if(props.started){
                if(result == null) {
                    return(
                      <div className="badge pull-right">
                         {t("pending")}
                      </div>
                    );
                } else {
                    return(
                      <div className="badge pull-right">
                         {t("done")}
                      </div>
                    );
                }
              }
           })()}
        </div>
        {this.state.open && result != null ?
        <div className="panel-body">
           <pre>{result}</pre>
        </div>
        : undefined }
      </div>
    )
  }
}


class RemoteCommand extends React.Component {

  constructor() {
    ["onPreview", "onRun", "commandChanged", "targetChanged", "commandResult"]
    .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      command: "ls -lha",
      target: "*",
      result: {
        minions: new Map()
      },
      action: "none",
      errors: []
    };
  }

  render() {
    var errs = null;
    if (this.state.errors) {
        this.state.errors.map( msg => {
            errs = <div className="alert alert-danger">{msg}</div>
        })
    }
    // TODO reuse Button class from package-states
    var button = null;
    switch (this.state.action) {
        case "none":
//            button = <button className="btn btn-success" onClick={this.onPreview}>{t("Preview")}</button>
            button = <AsyncButton name={t("Preview")} action={this.search} />;
            break;
        case "matching":
            button = <AsyncButton name={t("Preview")} action={this.search} />;

//          button = <button className="btn btn-default" disabled="true">
//                <i className="fa fa-circle-o-notch fa-spin"></i>{t("Preview")}
//            </button>
            break;
//        case "matched":
//        case "runned":
//            button = <button className="btn btn-success" onClick={this.onRun}>{t("Run")}</button>
//            break;
        case "running":
            button = <AsyncButton name={t("Run")} action={this.onRun} />;
//            button = <button disabled="true" className="btn btn-default">
//                <i className="fa fa-circle-o-notch fa-spin"></i>{t("Running")}
//            </button>
            break;
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
          <div className="panel panel-default">Search
            <div className="panel-body">
              <div className="row">
                <div className="col-lg-12">
                  <div className="input-group">
                      <input className="form-control" type="text" defaultValue={this.state.command} onChange={this.commandChanged} />
                      <span className="input-group-addon">@</span>
                      <input className="form-control" type="text" defaultValue={this.state.target} onChange={this.targetChanged} />
                      <div className="input-group-btn">{button}</div>
                  </div>
                </div>
              </div>
            </div>
            <div className="panel-body">{
                this.state.result != null ?
                  this.commandResult(this.state.result) :
                  <div></div>
            }</div>
          </div>
      </div>
    );
  }

  onPreview() {
    const cmd = this.state.command;
    const target = this.state.target;
    console.log(cmd);
    this.setState({action: "matching"});
    return $.get("/rhn/manager/api/minions/match?target=" + target, data => {
      console.log(data);
      this.setState({
        started: false,
        result: {
          minions: data.reduce((acc, id) => {
            acc.set(id, null);
            return acc;
          }, new Map())
        }
      });
    });
//    .always(() => {
//       this.setState({action: "matched"});
//    });
  }

  onRun() {
    const cmd = this.state.command;
    const target = this.state.target;
    console.log(cmd);
    const m = new Map();
    for(var key of this.state.result.minions.keys()) {
      m.set(key, null);
    }
    this.setState({
      result: {
        minions: m
      },
      started: true
    });
    this.setState({action: "running"})
    $.post("/rhn/manager/api/minions/cmd", {
        csrf_token: csrfToken,
        cmd: cmd,
        target: target
      },
      data => {
          console.log(data);
          this.setState({
            result: {
              minions: object2map(data)
            }
          });
    })
    .fail((jqXHR, textStatus, errorThrown) => {
        try {
            this.setState({errors: $.parseJSON(jqXHR.responseText)})
        } catch (err) {
        }
    })
//    .always(() => {
//        this.setState({action: "runned"});
//    });
  }

  targetChanged(event) {
    this.setState({
      action: "none",
      target: event.target.value,
      previewed: false
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
          <MinionResultView id={id} result={value} started={this.state.started} />
       );
     }
    return (
      <div>
        {elements}
      </div>
    );
  }
}

React.render(
  <RemoteCommand />,
  document.getElementById('remote-commands')
);
