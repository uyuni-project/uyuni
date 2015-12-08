'use strict';

const INSTALLED = 0;
const REMOVED = 1;
const PURGED = 2;

const LATEST = 0;
const EQUAL = 1;

class PackageStates extends React.Component {

  constructor() {
    ["init", "tableBody", "handleStateChange", "onSearchChange", "search"]
    .forEach(method => this[method] = this[method].bind(this));
    this.state = {
        filter: "",
        packageStates: []
    };
    this.init();
  }

  init() {
    $.get("/rhn/manager/api/states/packages?sid=" + serverId, data => {

      console.log(data);
      this.setState({
        packageStates: data.map(state => {
          return {
            original: state,
            current: state
          }
        })
      });
    });
  }

  search() {
    $.get("/rhn/manager/api/states/packages/match?sid=" + serverId + "&target=" + this.state.filter, data => {
      console.log(data);
      this.setState({
        packageStates: data.map(state => {
          return {
            original: state,
            current: state
          }
        })
      });
    });
  }

  tableBody(rows) {
    const elements = [];
    for(var row of rows) {
      const currentState = row.current;
      var versionConstraintSelect = null;
      if(currentState.packageStateId === INSTALLED) {
        versionConstraintSelect =
            <select className="form-control" value={currentState.versionConstraintId}>
              <option value="0">Latest</option>
              //<option value="1">Equal</option>
            </select>;
      }
      var undoButton = null;
      if(currentState !== row.original) {
        undoButton = <button className="btn" onClick={this.handleUndo(row)}>Undo</button>
      }

      elements.push(
        <tr className={currentState !== row.original ? "warning" : ""}>
          <td>{currentState.name}</td>
          <td>
            <div className="form-group">
              <select className="form-control" value={currentState.packageStateId} onChange={this.handleStateChange(row)}>
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
        {elements}
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
         const newStates = this.state.packageStates.map(state => {
            if(state === packageState){
                if (state.current !== state.original) {
                    return {
                        current: state.original,
                        original: state.original
                    };
                } else {
                    return state;
                }
            } else {
                return state;
            }
         });
         this.setState({
            packageStates: newStates
         });
      }
  }

  handleStateChange(packageState) {
      return event => {
         const newPackageStateId = parseInt(event.target.value);
         const newStates = this.state.packageStates.map(state => {
            if(state === packageState){
                if (newPackageStateId === packageState.original.packageStateId) {
                    return {
                      original: packageState.original,
                      current: packageState.original
                    };
                } else {
                   return {
                      original: packageState.original,
                      current: {
                         arch: state.current.arch,
                         epoch: state.current.epoch,
                         version: state.current.version,
                         release: state.current.release,
                         name: state.current.name,
                         packageStateId: newPackageStateId,
                         versionConstraintId: state.current.versionConstraintId
                       }
                   };
                }
            } else {
                return state;
            }
         });
         this.setState({
            packageStates: newStates
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
            <input type="text" value={this.state.filter} onChange={this.onSearchChange}/>
            <button className="btn" onClick={this.search}>Search</button>
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Package Name</th>
                  <th>State</th>
                </tr>
              </thead>
              {this.tableBody(this.state.packageStates)}
            </table>
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
