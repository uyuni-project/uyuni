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
        <div id={id} className="panel-heading" onClick={this.onClick}>
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
           <pre id={id + '-results'}>{result}</pre>
        </div>
        : undefined }
      </div>
    )
  }
}


class RemoteCommand extends React.Component {

  constructor() {
    super();
    ["onPreview", "onRun", "commandChanged", "targetChanged", "commandResult"]
    .forEach(method => this[method] = this[method].bind(this));
    this.state = {
      command: "ls -lha",
      target: "*",
      result: {
        minions: new Map()
      },
      previewed: false,
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
    const button = !this.state.previewed ?
        <AsyncButton id="preview" name={t("Preview")} action={this.onPreview} /> :
        <AsyncButton id="run" name={t("Run")} action={this.onRun} />;

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
    return $.get("/rhn/manager/api/minions/match?target=" + target)
        .done(data => {
            console.log(data);
            this.setState({
              previewed: true,
              started: false,
              result: {
                minions: data.reduce((acc, id) => {
                  acc.set(id, null);
                  return acc;
                }, new Map())
              }
            });
        });
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
    return $.post("/rhn/manager/api/minions/cmd", {
        csrf_token: csrfToken,
        cmd: cmd,
        target: target
    })
    .done(data => {
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
    });
  }

  targetChanged(event) {
    this.setState({
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
