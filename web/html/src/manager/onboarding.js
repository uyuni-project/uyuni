'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Buttons = require("../components/buttons");
const AsyncButton = Buttons.AsyncButton;
const Panel = require("../components/panel").Panel;
const Table = require("../components/table2").Table;
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Comparators = Functions.Comparators;
const Filters = Functions.Filters;
const Renderer = Functions.Renderer;
const STables = require("../components/tableng.js");
const STable = STables.STable;
const SColumn = STables.SColumn;
const SHeader = STables.SHeader;
const SOutput = STables.SOutput;


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
    ["reloadKeys"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        keys: []
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

  render() {
    const minions = this.state.keys;
    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name="Refresh" text action={this.reloadKeys} />
    </div>;

    return (
        <Panel title="Onboarding" icon="fa-desktop" button={ panelButtons }>
            <Table
                data={ minions }
                keyFn={ entry => entry.id + entry.fingerprint }
                options={{
                }}
                columns={[
                  {
                      "header": t("Name"),
                      "entryToCell": (entry) => entry,
                      "renderCell": (entry, filter) => {
                        if(entry.state == "minions") {
                            return <a href={ "/rhn/manager/minions/" + entry.id }>{ Renderer.highlightSubstring(entry.id, filter) }</a>;
                        } else {
                            return Renderer.highlightSubstring(entry.id, filter);
                        }
                      },
                      "sort": Comparators.mapping(Comparators.locale, (entry) => entry.id),
                      "filter": Filters.mapping(Filters.substring, (entry) => entry.id),
                      "ratio": 0.3
                  },{
                      "header": t("Fingerprint"),
                      "entryToCell": (entry) => entry.fingerprint,
                      "renderCell": Renderer.highlightSubstring,
                      "sort": Comparators.locale,
                      "filter": Filters.substring,
                      "ratio": 0.5,
                      "className": "fingerprintCell"
                  },{
                      "header": t("State"),
                      "entryToCell": (entry) => entry.state,
                      "renderCell": (cell) => labelFor(cell),
                      "sort": Comparators.mapping(Comparators.locale, stateUiName),
                      "filter": Filters.mapping(Filters.substring, stateUiName),
                      "ratio": 0.1
                  },{
                      "header": t("Actions"),
                      "entryToCell": (entry) => entry,
                      "renderCell": (cell) => actionsFor(cell.id, cell.state, this.reloadKeys, this.state.isOrgAdmin),
                      "ratio": 0.1
                  }
                ]}
            />
        </Panel>
    );
  }

}








var dummyData = [];

var stooges = ["Moe", "Curly", "Larry"]

var j = 0;
for (var i=0; i < 100; i++) {
  dummyData.push({id: i, name: stooges[j++] + i, address: "Address " + i});
  if (j > 2) {
     j = 0;
  }
}

function Highlight(props) {
  var text = props.text;
  var high = props.highlight;

  if (!props.enabled) {
    return <span>{text}</span>
  }

  var chunks = text.split(new RegExp("^" + high));

  return <span>{chunks
    .map((e, i) => e == "" ? <span key="highlight" style={{backgroundColor: "#f0ad4e", borderRadius: "2px"}}>{high}</span> : <span key={i}>{e}</span>)}</span>;
}








class Onboarding2 extends React.Component {

  constructor(props) {
    super();
    ["sortById", "searchData", "sortByName", "rowKey"].forEach(method => this[method] = this[method].bind(this));
    this.state = {
        keys: [],
        tableData: dummyData
    };
//    this.reloadKeys();
  }

//  reloadKeys() {
//    return listKeys().then(data => {
//        this.setState({
//            keys: processData(data["fingerprints"]),
//            isOrgAdmin: data["isOrgAdmin"]
//        });
//    });
//  }

  rowKey(rowData) {
    return rowData.id;
  }

  sortById(data, direction) {
    return data.sort((a, b) => direction * (a.id - b.id));
  }

  sortByName(data, direction) {
     return data.sort((a, b) => {
         if (a.name > b.name) {
           return direction * 1;
         }
         if (a.name < b.name) {
           return direction * -1;
         }
         return 0
       });
  }

  searchData(data, criteria) {
      if (!criteria || criteria == "") {
        return data;
      }

      return data.filter((e) => e.name.startsWith(criteria));
  }

  render() {
    const minions = this.state.keys;
    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name="Refresh" text action={this.reloadKeys} />
    </div>;

    return (
        <Panel title="Onboarding" icon="fa-desktop" button={ panelButtons }>
            <STable data={this.state.tableData} searchFn={this.searchData} rowKeyFn={this.rowKey}>
              <SColumn columnKey="id">
                <SHeader sortFn={this.sortById}>Id</SHeader>
                <SOutput value={ (row) => row.id}/>
              </SColumn>
              <SColumn columnKey="name">
                <SHeader sortFn={this.sortByName}>Name</SHeader>
                <SOutput value={ (row, table) => <Highlight enabled={table.state.dataModel.filtered} text={row.name}
                    highlight={table.state.dataModel.filteredText}/> } />
              </SColumn>
              <SColumn>
                <SHeader>Address</SHeader>
                <SOutput value={ (row) => row.address } />
              </SColumn>
            </STable>
        </Panel>
    );
  }

}

ReactDOM.render(
  <Onboarding />,
  document.getElementById('onboarding')
);

ReactDOM.render(
  <Onboarding2 />,
  document.getElementById('onboarding2')
);