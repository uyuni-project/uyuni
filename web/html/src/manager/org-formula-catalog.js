'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {Table, Column, SearchField} = require("../components/table");
const PanelComponent = require("../components/panel");
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const Panel = PanelComponent.Panel;

var FormulaCatalog = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": [],
            "messages": []
        };
        return st;
    },

    refreshServerData: function() {
        Network.get("/rhn/manager/api/formula-catalog/data").promise.then(data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },

    sortByText: function(aRaw, bRaw, columnKey, sortDirection) {
        return aRaw.toLowerCase().localeCompare(bRaw.toLowerCase()) * sortDirection;
    },

    rowKey: function(rowData) {
        return rowData;
    },

    searchData: function(data, criteria) {
        return data.filter((row) => row.toLowerCase().includes(criteria.toLowerCase()));
    },

    render: function() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: The formula catalog page enables viewing of currently installed <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank">Salt formulas</a>. Apply these formulas to individual systems or server groups. Formulas allow automatic installation and configuration of software and may be installed via RPM packages. We would be glad to receive your feedback in the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }
        return (
            <Panel title="Formula Catalog" icon="spacewalk-icon-salt-add" helpUrl="/rhn/help/reference/en-US/ref.webui.salt.formula_catalog.jsp">
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
            </Panel>
        );
    }

});

ReactDOM.render(
  <FormulaCatalog flashMessages={flashMessage()}/>,
  document.getElementById('formula-catalog')
);
