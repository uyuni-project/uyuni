'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Buttons = require("../components/buttons");
const AsyncButton = Buttons.AsyncButton;
const Panel = require("../components/panel").Panel;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const {Table, Column, SearchField, Highlight} = require("../components/table");

function listKeys() {
    return Network.get("/rhn/manager/api/minions/keys").promise;
}

function minionToList(idFingerprintMap, state) {
   return Object.keys(idFingerprintMap).map(key => {
       return {
           "id": key,
           "fingerprint": idFingerprintMap[key],
           "state": state
       };
   });
}

function processData(keys) {
   return ["minions", "minions_rejected", "minions_pre", "minions_denied"].reduce((acc, key) => {
       return acc.concat(minionToList(keys[key], key));
   }, []);
}

function acceptKey(key) {
    return Network.post("/rhn/manager/api/minions/keys/" + key + "/accept").promise;
}

function deleteKey(key) {
    return Network.post("/rhn/manager/api/minions/keys/" + key + "/delete").promise;
}

function rejectKey(key) {
    return Network.post("/rhn/manager/api/minions/keys/" + key + "/reject").promise;
}

function actionsFor(id, state, update, enabled) {
    const acc = () => <AsyncButton disabled={enabled?"":"disabled"} key="accept" title="accept" icon="check" action={() => acceptKey(id).then(update)} />;
    const rej = () => <AsyncButton disabled={enabled?"":"disabled"} key="reject" title="reject" icon="times" action={() => rejectKey(id).then(update)} />;
    const del = () => <AsyncButton disabled={enabled?"":"disabled"} key="delete" title="delete" icon="trash" action={() => deleteKey(id).then(update)} />;
    const mapping = {
        "minions": [del],
        "minions_pre": [acc, rej],
        "minions_rejected": [del],
        "minions_denied": [del]
    };
    return (
      <div className="pull-right btn-group">
         { mapping[state].map(fn => fn()) }
      </div>
    );
}

const stateMapping = {
    "minions": {
        uiName: t("accepted"),
        label: "success"
    },
    "minions_pre": {
        uiName: t("pending"),
        label: "info"
    },
    "minions_rejected": {
        uiName: t("rejected"),
        label: "warning"
    },
    "minions_denied": {
        uiName: t("denied"),
        label: "danger"
    }
}

const stateUiName = (state) => stateMapping[state].uiName;

function labelFor(state) {
    const mapping = stateMapping[state];
    return <span className={"label label-" + mapping.label}>{ mapping.uiName }</span>
}

class Onboarding extends React.Component {

  constructor(props) {
    super();
    ["sortByName", "searchData", "sortByFingerprint", "sortByState", "rowKey", "reloadKeys"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        keys: [],
        isOrgAdmin: false
    };
    this.reloadKeys();
  }

  reloadKeys() {
    return listKeys().then(data => {
        this.setState({
            keys: processData(data["fingerprints"]),
            isOrgAdmin: data["isOrgAdmin"]
        });
    });
  }

  rowKey(rowData) {
    return rowData.id;
  }

  sortByName(a, b) {
    return a.id.toLowerCase().localeCompare(b.id.toLowerCase());
  }

  sortByFingerprint(a, b) {
     return a.localeCompare(b);
  }

  sortByState(a, b) {
    return a.state.localeCompare(b.state);
  }

  searchData(data, criteria) {
      return data.filter((e) => e.id.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()) ||
        e.fingerprint.toLocaleLowerCase().includes(criteria.toLocaleLowerCase()));
  }

  render() {
    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name="Refresh" text action={this.reloadKeys} />
    </div>;
    return (
    <span>
        <h4>{this.state.selectedProductId}</h4>
        <Panel title="Onboarding" icon="fa-desktop" button={ panelButtons }>
            <Table
                data={this.state.keys}
                identifier={this.rowKey}
                pageSize={15}
                searchPanel={
                    <SearchField filter={this.searchData}/>
                }>
              <Column
                columnKey="id"
                width="30%"
                sortFn={this.sortByName}
                header={t('Name')}
                cell={ (row, table) => {
                      if(row.state == "minions") {
                         return <a href={ "/rhn/manager/minions/" + row.id }>
                             <Highlight enabled={table.state.dataModel.isFiltered()}
                               text={row.id}
                               highlight={table.state.dataModel.criteria}/>
                             </a>;
                      } else {
                         return <Highlight enabled={table.state.dataModel.isFiltered()}
                             text={row.id}
                             highlight={table.state.dataModel.criteria}/>;
                      }
                     }}
                />
              <Column
                columnKey="fingerprint"
                width="50%"
                sortFn={this.sortByFingerprint}
                header={t('Fingerprint')}
                cell={ (row, table) =>
                    <Highlight enabled={table.state.dataModel.isFiltered()}
                         text={row.fingerprint}
                         highlight={table.state.dataModel.criteria}/> }
                />
              <Column
                columnKey="state"
                width="10%"
                sortFn={this.sortByState}
                header={t('State')}
                cell={ (row) => labelFor(row.state) }
                />
              <Column
                width="10%"
                header={t('Actions')}
                cell={ (row) => actionsFor(row.id, row.state, this.reloadKeys, this.state.isOrgAdmin)}
                />
            </Table>
        </Panel>
    </span>
    );
  }

}

ReactDOM.render(
  <Onboarding />,
  document.getElementById('onboarding')
);
