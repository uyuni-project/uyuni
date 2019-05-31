/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {Table, Column, SearchField} = require("components/table");
const { TopPanel } = require('components/panels/TopPanel');
const Messages = require("components/messages").Messages;
const Network = require("utils/network");

class FormulaCatalog extends React.Component {
    constructor(props) {
        super(props);
        var st = {
            "serverData": [],
            "messages": []
        };
        this.state = st;
    }

    refreshServerData = () => {
        Network.get("/rhn/manager/api/formula-catalog/data").promise.then(data => {
          this.setState({"serverData" : data});
        });
    };

    UNSAFE_componentWillMount() {
        this.refreshServerData();
    }

    sortByText = (aRaw, bRaw, columnKey, sortDirection) => {
        return aRaw.toLowerCase().localeCompare(bRaw.toLowerCase()) * sortDirection;
    };

    rowKey = (rowData) => {
        return rowData;
    };

    searchData = (data, criteria) => {
        return data.filter((row) => row.toLowerCase().includes(criteria.toLowerCase()));
    };

    render() {
        var messages = <Messages items={[{severity: "info", text:
            <p>The formula catalog page enables viewing of currently installed <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank">Salt Formulas</a>. Apply these formulas to individual systems or server groups. Formulas allow automatic installation and configuration of software and may be installed via RPM packages.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }
        return (
            <TopPanel title="Formula Catalog" icon="spacewalk-icon-salt-add" helpUrl="/docs/reference/salt/salt-formula-catalog.html">
            {messages}
            <div>
              <Table
                data={this.state.serverData}
                identifier={this.rowKey}
                initialSortColumnKey="name"
                initialItemsPerPage={userPrefPageSize}
                searchPanel={
                    <SearchField filter={this.searchData}
                      placeholder={t("Filter by formula name")} />
                }>
                <Column
                  columnKey="name"
                  comparator={this.sortByText}
                  header={t("Formula")}
                  cell={ (s) =>
                    <a href={"/rhn/manager/formula-catalog/formula/" + s}>{s}</a> }
                />
              </Table>
            </div>
            </TopPanel>
        );
    }
}

ReactDOM.render(
  <FormulaCatalog flashMessages={flashMessage()}/>,
  document.getElementById('formula-catalog')
);
