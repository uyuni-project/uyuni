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
    } else {
        $.get("/rhn/manager/api/states/packages/match?sid=" + serverId + "&target=" + this.state.filter, data => {
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
        states.push(state)
    }
    $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/packages/save",
        data: JSON.stringify({
            sid: serverId,
            packageStates: states
        }),
        contentType: "application/json"
    });
  }

  applyPackageState() {
    $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/apply",
        data: JSON.stringify({
            sid: serverId,
            states: ["packages"]
        }),
        contentType: "application/json"
    });
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
        rows = this.state.packageStates;
    } else if(this.state.view === "search") {
        rows = this.state.search.results;
    } else if(this.state.view === "changes") {
        for(var state of this.state.changed.values()) {
            rows.push(state)
        }
    }
    for(var row of rows) {
      const changed = this.state.changed.get(packageStateKey(row))
      const currentState = changed === undefined? row : changed;
      var versionConstraintSelect = null;
      if(currentState.packageStateId === INSTALLED) {
        //versionConstraintSelect =
        //    <select className="form-control" value={versionConstraints2selectValue(currentState.versionConstraintId)}>
        //      <option value="0">Latest</option>
        //      //<option value="1">Equal</option>
        //    </select>;
      }
      var undoButton = null;
      if(changed !== undefined) {
        undoButton = <button className="btn btn-default" onClick={this.handleUndo(row)}>Undo</button>
      }

      elements.push(
        <tr className={currentState !== row ? "warning" : ""}>
          <td>{currentState.name}</td>
          <td>
            <div className="form-group">
              <select className="form-control" value={packageState2selectValue(currentState.packageStateId)} onChange={this.handleStateChange(row)}>
                <option value="-1">Unmanaged</option>
                <option value="0">Installed</option>
                <option value="1">Removed</option>
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
                    <div>No package states.</div>
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
                arch: packageState.arch,
                epoch: packageState.epoch,
                version: packageState.version,
                release: packageState.release,
                name: packageState.name,
                packageStateId: newPackageStateId,
                versionConstraintId: newPackageStateId == INSTALLED ? LATEST :  packageState.versionConstraintId
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
          Package States
        </h2>
        <div className="row col-md-12">
          <div className="panel panel-default">
            <div className="panel-body">
                <div className="input-group">
                    <input className="form-control" type="text" value={this.state.filter} onChange={this.onSearchChange}/>
                    <span className="input-group-btn">
                        <button className={this.state.view == "search" ? "btn btn-success" : "btn btn-default"} onClick={this.search}>Search</button>
                        <button className={this.state.view == "system" ? "btn btn-success" : "btn btn-default"} onClick={this.setView("system")}>System</button>
                        <button className={this.state.view == "changes" ? "btn btn-success" : "btn btn-default"} onClick={this.setView("changes")}>Changes</button>
                        <button className="btn btn-default" onClick={this.save}>Save</button>
                        <button className="btn btn-default" onClick={this.applyPackageState}>Apply</button>
                    </span>
                </div>
                <table className="table table-striped">
                  <thead>
                    <tr>
                      <th>Package Name</th>
                      <th>State</th>
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

React.render(
  <PackageStates />,
  document.getElementById('package-states')
);
