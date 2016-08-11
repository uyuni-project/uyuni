'use strict';

const React = require("react");
const Buttons = require("../components/buttons");
const Panels = require("../components/panel");
const Fields = require("../components/fields");
const PopUp = require("../components/popup").PopUp;
const Messages = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const Network = require("../utils/network");

const AsyncButton = Buttons.AsyncButton;
const InnerPanel = Panels.InnerPanel;
const PanelRow = Panels.PanelRow;
const TextField = Fields.TextField;

function stateKey(state) {
    return state.name;
}

class SaltStatePopup extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
      const popUpContent = this.props.saltState ?
            <textarea className="form-control" rows="20" name="content"
                defaultValue={this.props.saltState.content} readOnly="true"/>
                : null;
      const title = this.props.saltState ?
            t("Salt State: {0}", this.props.saltState.name) : null;
      return (<PopUp
                title={title}
                className="modal-lg"
                id="saltStatePopUp"
                content={popUpContent}
                onClosePopUp={this.props.onClosePopUp}
              />)
  }

}

class CustomStates extends React.Component {

  constructor(props) {
    super(props);

    ["init", "tableBody", "handleSelectionChange", "onSearchChange", "search", "save",
        "applySaltState", "setView", "addChanged", "showPopUp", "onClosePopUp"]
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
        filter: "",
        view: "system",
        saltStates: [],
        search: {
            filter: null,
            results: []
        },
        changed: new Map(),
        messages: null
    };
    this.init();
  }

  init() {
    Network.get(this.props.matchUrl()).promise.then(data => {
      console.log(data);
      this.setState({
        saltStates: data
      });
    });
  }

  applySaltState() {
    if (this.state.changed.size > 0) {
        const response = confirm(t("There are unsaved changes. Do you want to proceed ?"))
        if (response == false) {
            return null;
        }
    }

    const request = this.props.applyRequest(this);

    return request;
  }

  save() {
    const states = [];
    for(var state of this.state.changed.values()) {
        states.push(state.value)
    }
    const request = this.props.saveRequest(states).promise.then(
    (data, textStatus, jqXHR) => {
      console.log("success: " + data);

      const newSearchResults = this.state.search.results.map( state => {
          const changed = this.state.changed.get(stateKey(state))
          if(changed !== undefined) {
              return changed.value;
          } else {
              return state;
          }
      });

      this.setState({
        changed: new Map(), // clear changed
        view: "system", // switch view to system
        saltStates: data, // set data for system tab
        search: {
            filter: this.state.search.filter,
            results: newSearchResults
        },
        messages: MessagesUtils.info(t('State assignments have been saved.'))
      });
     },
     (jqXHR, textStatus, errorThrown) => {
      console.log("fail: " + textStatus);

      this.setState({
        messages: MessagesUtils.error(t('An error occurred on save.'))
      });
     });
    return request;
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
       return Network.get(this.props.matchUrl(this.state.filter)).promise.then(data => {
          console.log(data);
          this.setState({
            view: "search",
            search:  {
                filter: this.state.filter,
                results: data
            },
            messages: null
          });
        })
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
          selected == currentState.original.assigned) {
            this.state.changed.delete(name);
      } else {
            this.state.changed.set(name, {
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
        rows = this.state.saltStates.filter(state => state.assigned).map(state => {
            const changed = this.state.changed.get(stateKey(state))
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
        for(var state of this.state.changed.values()) {
            rows.push(state)
        }
    }

    for(var row of rows) {
      const changed = row.value;
      const currentState = changed === undefined? row.original : changed;

      elements.push(
        <tr id={currentState.name + '-row'} key={currentState.name} className={changed !== undefined ? "warning" : ""}>
          <td><a href="#" data-toggle="modal" data-target="#saltStatePopUp" onClick={() => {this.showPopUp(currentState.name);}}>{currentState.name}</a></td>
          <td>
            <div className="form-group">
                <input id={currentState.name + "-cbox"} type="checkbox" checked={currentState.assigned} value={currentState.name} onChange={this.handleSelectionChange(row.original)}/>
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
                    <div>{t("No states assigned. Use search to find and assign states.")}</div>
                </td>
            </tr>
        }
      </tbody>
    );
  }

  setView(view) {
    return event => {
      this.setState({
          view: view
      });
    }
  }

  showPopUp(stateName) {
    Network.get("/rhn/manager/api/state-catalog/state/" + stateName + "/content").promise.then(data => {
        this.setState({
            showSaltState: {name: stateName, content: data}
        })
    });
  }

  onClosePopUp() {
    this.setState({
        showSaltState: null
    })
  }

  clearMessages() {
      this.setState({
          messages: null
      })
  }

  render() {
    const buttons = [<AsyncButton id="save-btn" action={this.save} name={t("Save")} disabled={this.state.changed.size == 0}/>,
                     <AsyncButton id="apply-btn" action={this.applySaltState} name={t("Apply")} />];

    const messages = this.state.messages ?
            <Messages items={this.state.messages}/>
            : null;
    return (
        <span>
        {messages}
        <InnerPanel title={t("Custom States")} icon="spacewalk-icon-salt-add" buttons={buttons}>

            <PanelRow className="input-group">
                <TextField id="search-field" value={this.state.filter} placeholder={t("Search in state catalog")} onChange={this.onSearchChange} onPressEnter={this.search}/>
                <span className="input-group-btn">
                    <AsyncButton id="search-states" name={t("Search")} action={this.search} />
                    <button id="system-btn" className={this.state.view == "system" ? "btn btn-success" : "btn btn-default"} onClick={this.setView("system")}>{t("System")}</button>
                    <button id="changes-btn" className={this.state.view == "changes" ? "btn btn-success" : "btn btn-default"} disabled={this.state.changed.size == 0} onClick={this.setView("changes")}>
                        {this.state.changed.size > 0 ? this.state.changed.size : t("No")} {t("Changes")}
                    </button>
                </span>
            </PanelRow>

            <table className="table table-striped">
              <thead>
                <tr>
                  <th>{t("State Name")}</th>
                  <th>{t("Assign")}</th>
                </tr>
              </thead>
              {this.tableBody()}
            </table>

            <SaltStatePopup saltState={this.state.showSaltState} onClosePopUp={this.onClosePopUp}/>

        </InnerPanel>
        </span>
    );
  }

}

module.exports = {
    CustomStates : CustomStates
}