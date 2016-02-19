'use strict';

const React = require("react");
const Buttons = require("../components/buttons");
const Panels = require("../components/panels");

const AsyncButton = Buttons.AsyncButton;
const InnerPanel = Panels.InnerPanel;
const PanelRow = Panels.PanelRow;

function stateKey(state) {
    return state.name + "_" + stat.assigned;
}

class ApplyState extends React.Component {

  constructor(props) {
    super();

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
          state.versionConstraintId = normalizePackageVersionConstraint(state.versionConstraintId);
          return state;
        })
      });
    });
  }

  applySaltState() {
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

  save() {

  }

  onSearchChange(event) {
    this.setState({
        filter: event.target.value
    });
  }

  search() {
    if (this.state.filter === this.state.search.filter) {
        this.setState({
            view: "search"
        });
        return Promise.resolve();
    } else {
       return $.get("/rhn/manager/api/states/match?sid=" + serverId + "&target=" + this.state.filter, data => {
          console.log(data);
          this.setState({
            view: "search",
            search:  {
                filter: this.state.filter,
                results: data
            }
          });
        });
    }
  }

  setView(view) {
    return event => {
      this.setState({
          view: view
      });
    }
  }

  addChanged(original, name, selected) {
      const currentState = this.state.changed.get(name);
      if (currentState != undefined &&
          selected ==  currentState.original.selected) {
            this.state.changed.delete(name);
      } else {
            this.state.changed.set(key, {
                original: original,
                value: {
                    name: original.name,
                    assigned: selected
                }
            });
      }
      this.setState({
         changed: this.state.changed
      });
  }

  handleSelectionChange(original) {
      return event => {
         this.addChanged(
            original,
            event.target.value,
            event.target.checked
         );
      }
  }


  tableBody() {
    const elements = [];
    var rows = [];
    if(this.state.view === "system") {
        // TODO
    } else if(this.state.view === "search") {
        rows = this.state.search.results.map(state => {
            const changed = this.state.changed.get(stateKey(state))
            if(changed !== undefined) {
                return changed;
            } else {
                return {
                    original: state,
                };
            }
        });
    } else if(this.state.view === "changes") {
        // TODO
    }

    for(var row of rows) {
      const changed = row.value;
      const currentState = changed === undefined? row.original : changed;

      elements.push(
        <tr className={changed !== undefined ? "warning" : ""}>
          <td>{currentState.name}</td>
          <td>
            <div className="form-group">
                <input type="checkbox" value={currentState.name} onClick={this.handleSelectionChange(row.original)}/>
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
                    <div>{t("No states.")}</div>
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

  setView(view) {
    return event => {
      this.setState({
          view: view
      });
    }
  }

  render() {
    const buttons = [<AsyncButton action={this.save} name={t("Save")} disabled={this.state.changed.size == 0}/>,
                     <AsyncButton action={this.applySaltState} name={t("Apply")} />];

    return (
        <InnerPanel title={t("Apply States")} buttons={buttons}>
            <PanelRow className="input-group">
                <input className="form-control" type="text" value={this.state.filter} onChange={this.onSearchChange}/>
                <span className="input-group-btn">
                    <AsyncButton name={t("Search")} action={this.search} />
                    <button className={this.state.view == "system" ? "btn btn-success" : "btn btn-default"} onClick={this.setView("system")}>{t("System")}</button>
                    <button className={this.state.view == "changes" ? "btn btn-success" : "btn btn-default"} disabled={this.state.changed.size == 0} onClick={this.setView("changes")}>
                        {this.state.changed.size > 0 ? this.state.changed.size : t("No")} {t("Changes")}
                    </button>
                </span>
            </PanelRow>

            <table className="table table-striped">
              <thead>
                <tr>
                  <th>{t("State Name")}</th>
                  <th>{t("Apply")}</th>
                </tr>
              </thead>
              {this.tableBody()}
            </table>

        </InnerPanel>
    );
  }

}

React.render(
  <ApplyState/>,
  document.getElementById('apply-states')
);