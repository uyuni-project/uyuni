'use strict';

const UNMANAGED = {};
const INSTALLED = {value: 0};
const REMOVED = {value: 1};
const PURGED = {value: 2};

const LATEST = {value: 0};
const EQUAL = {value: 1};

function selectValue2PackageState(value) {
    switch(value){
        case -1: return UNMANAGED;
        case 0: return INSTALLED;
        case 1: return REMOVED;
        case 2: return PURGED;
    }
}

function packageState2selectValue(ps) {
    return ps.value !== undefined ? ps.value : -1;
}

function versionConstraints2selectValue(vc) {
    return vc.value;
}

function normalizePackageState(ps) {
    return selectValue2PackageState(packageState2selectValue(ps));
}

function selectValue2VersionConstraints(value) {
    switch(value){
        case 0: return LATEST;
        case 1: return EQUAL;
    }
}

function packageStateKey(packageState) {
    return packageState.name + packageState.version +
           packageState.release + packageState.epoch +
           packageState.arch;
}

class PackageStates extends React.Component {

  constructor() {
    ["init", "tableBody", "handleStateChange", "onSearchChange", "search", "save", "setView"]
    .forEach(method => this[method] = this[method].bind(this));
    this.state = {
        filter: "",
        view: "system",
        packageStates: [],
        search: {
            filter: null,
            results: []
        },
        changed: new Map()
    };
    this.init();
  }

  init() {
    $.get("/rhn/manager/api/states/packages?sid=" + serverId, data => {
      console.log(data);
      this.setState({
        packageStates: data.map(state => {
          state.packageStateId = normalizePackageState(state.packageStateId);
          return state;
        })
      });
    });
  }

  search() {
    if (this.state.filter === this.state.search.filter) {
        this.setState({
            view: "search"
        });
        return Promise.resolve();
    } else {
       return $.get("/rhn/manager/api/states/packages/match?sid=" + serverId + "&target=" + this.state.filter, data => {
          console.log(data);
          this.setState({
            view: "search",
            search:  {
                filter: this.state.filter,
                results: data.map(state => {
                  state.packageStateId = normalizePackageState(state.packageStateId);
                  return state;
                })
            }
          });
        });
    }
  }

  save() {
    const states = [];
    for(var state of this.state.changed.values()) {
        states.push(state.value)
    }
    const request = $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/packages/save",
        data: JSON.stringify({
            sid: serverId,
            packageStates: states
        }),
        contentType: "application/json"
    }).then((data, textStatus, jqXHR) => {
      console.log("success: " + data);
      const newPackageStates = data.map(state => {
          state.packageStateId = normalizePackageState(state.packageStateId);
          return state;
      });

      const newSearchResults = this.state.search.results.map( state => {
        const changed = this.state.changed.get(packageStateKey(state))
        if(changed !== undefined) {
            return changed.value;
        } else{
            return state;
        }
      });

      this.setState({
        changed: new Map(),
        view: "system",
        search: {
            filter: this.state.search.filter,
            results: newSearchResults
        },
        packageStates: newPackageStates
      });
    }, (jqXHR, textStatus, errorThrown) => {
      console.log("fail: " + textStatus);
    });
    return Promise.resolve(request);
  }

  applyPackageState() {
    const request = $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/apply",
        data: JSON.stringify({
            sid: serverId,
            states: ["packages"]
        }),
        contentType: "application/json"
    });
    return Promise.resolve(request);
  }

  setView(view) {
    return event => {
      this.setState({
          view: view
      });
    }
  }

  tableBody() {
    const elements = [];
    var rows = [];
    if(this.state.view === "system") {
        rows = this.state.packageStates.map(state => {
            const changed = this.state.changed.get(packageStateKey(state))
            if(changed !== undefined) {
                return changed;
            } else {
                return {
                    original: state,
                };
            }
        });
    } else if(this.state.view === "search") {
        rows = this.state.search.results.map(state => {
            const changed = this.state.changed.get(packageStateKey(state))
            if(changed !== undefined) {
                return changed;
            } else {
                return {
                    original: state,
                };
            }
        });
    } else if(this.state.view === "changes") {
        for(var state of this.state.changed.values()) {
            rows.push(state)
        }
    }
    for(var row of rows) {
      const changed = row.value;
      const currentState = changed === undefined? row.original : changed;

      var versionConstraintSelect = null;
      if(currentState.packageStateId === INSTALLED) {
        //versionConstraintSelect =
        //    <select className="form-control" value={versionConstraints2selectValue(currentState.versionConstraintId)}>
        //      <option value="0">{t("Latest")}</option>
        //      //<option value="1">{t("Equal")}</option>
        //    </select>;
      }
      var undoButton = null;
      if(changed !== undefined) {
        undoButton = <button className="btn btn-default" onClick={this.handleUndo(row.original)}>{t("Undo")}</button>
      }

      elements.push(
        <tr className={changed !== undefined ? "warning" : ""}>
          <td>{t(currentState.name)}</td>
          <td>
            <div className="form-group">
              <select className="form-control" value={packageState2selectValue(currentState.packageStateId)} onChange={this.handleStateChange(row.original)}>
                <option value="-1">{t("Unmanaged")}</option>
                <option value="0">{t("Installed")}</option>
                <option value="1">{t("Removed")}</option>
              </select>
              { versionConstraintSelect }
              { undoButton }
            </div>
          </td>
        </tr>
      );
     }
    return (
      <tbody className="table-content">
        {elements.length > 0 ? elements :
            <tr>
                <td colSpan="2">
                    <div>{t("No package states.")}</div>
                </td>
            </tr>
        }
      </tbody>
    );
  }

  onSearchChange(event) {
    this.setState({
        filter: event.target.value
    });
  }

  handleUndo(packageState) {
      return event => {
         this.state.changed.delete(packageStateKey(packageState));
         this.setState({
            changed: this.state.changed
         });
      }
  }

  handleStateChange(packageState) {
      return event => {
         const newPackageStateId = selectValue2PackageState(parseInt(event.target.value));
         if(packageState.packageStateId == newPackageStateId) {
            this.state.changed.delete(packageStateKey(packageState))
         } else {
            this.state.changed.set(packageStateKey(packageState), {
                original: packageState,
                value: {
                    arch: packageState.arch,
                    epoch: packageState.epoch,
                    version: packageState.version,
                    release: packageState.release,
                    name: packageState.name,
                    packageStateId: newPackageStateId,
                    versionConstraintId: newPackageStateId == INSTALLED ? LATEST :  packageState.versionConstraintId
                }
            });
         }
         this.setState({
            changed: this.state.changed
         });
      }
  }

  render() {
    return (
      <div>
        <h2>
          <i className="fa spacewalk-icon-package-add"></i>
          {t("Package States")}
          <span className="btn-group pull-right">
              <Button action={this.save} name={t("Save")} disabled={this.state.changed.size == 0}/>
              <Button action={this.applyPackageState} name={t("Apply")} />
          </span>
        </h2>
        <div className="row col-md-12">
          <div className="panel panel-default">
            <div className="panel-body">
                <div className="row">
                    <span className="col-md-4 pull-right">
                        <span className="input-group">
                            <input className="form-control" type="text" value={this.state.filter} onChange={this.onSearchChange}/>
                            <span className="input-group-btn">
                                <Button action={this.applyPackageState} name={t("Search")} action={this.search} />
                                <button className={this.state.view == "system" ? "btn btn-success" : "btn btn-default"} onClick={this.setView("system")}>{t("System")}</button>
                                <button className={this.state.view == "changes" ? "btn btn-success" : "btn btn-default"} disabled={this.state.changed.size == 0} onClick={this.setView("changes")}>
                                    {this.state.changed.size > 0 ? this.state.changed.size : t("No")} {t("Changes")}
                                </button>
                            </span>
                        </span>
                    </span>
                </div>
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>{t("Package Name")}</th>
                      <th>{t("State")}</th>
                    </tr>
                  </thead>
                  {this.tableBody()}
                </table>
              </div>
          </div>
        </div>
      </div>
    );
  }
}

const waiting = "waiting";
const success = "success";
const initial = "initial";
const failure = "failure";

class Button extends React.Component {


  constructor(props) {
    ["trigger"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        value: initial
    };
  }

  trigger() {
    this.setState({
        value: waiting
    });
    const future = this.props.action();
    future.then(
      () => {
        this.setState({
            value: success
        });
      },
      () => {
        this.setState({
            value: failure
        });
      }
    );
  }

  render() {
    const style = this.state.value == failure ? "btn btn-danger" : "btn btn-default";
    return (
        <button className={style} disabled={this.state.value == waiting || this.props.disabled} onClick={this.trigger}>
           {this.state.value == waiting ? <i className="fa fa-circle-o-notch fa-spin"></i> : undefined}
           {t(this.props.name)}
        </button>
    );
  }

}

React.render(
  <PackageStates />,
  document.getElementById('package-states')
);
