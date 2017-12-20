'use strict';

var React = require("react");
const ReactDOM = require("react-dom");
const {Table, Column, SearchField} = require("../components/table");
var PanelComponent = require("../components/panel");
var Messages = require("../components/messages").Messages;
var Network = require("../utils/network");
var Panel = PanelComponent.Panel;
var PanelButton = PanelComponent.PanelButton;


var StateCatalog = React.createClass({

  getInitialState: function() {
    var st = {
      "serverData": []
    };
    return st;
  },

  refreshServerData: function() {
    Network.get("/rhn/manager/api/state-catalog/data").promise.then(data => {
      this.setState({serverData : data});
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
    var button = <PanelButton text="Create State" icon="fa-plus" action="/rhn/manager/state-catalog/state"/>;
    var msg = null;

    if(typeof this.props.flashMessages !== "undefined") {
      msg = <Messages items={this.props.flashMessages}/>;
    }
    return (
      <Panel title="State Catalog" icon="spacewalk-icon-salt-add" button={button}>
        {msg}
        <div>
          <Table
            data={this.state.serverData}
            identifier={this.rowKey}
            initialSortColumnKey="name"
            initialItemsPerPage={userPrefPageSize}
            searchPanel={
                <SearchField filter={this.searchData}
                  placeholder={t("Filter by state name")} />
            }>
            <Column
              columnKey="name"
              comparator={this.sortByText}
              header={t("State")}
              cell={ (s) =>
                <a href={"/rhn/manager/state-catalog/state/" + s}>{s}</a> }
            />
          </Table>
        </div>
      </Panel>
    );
  }
});

ReactDOM.render(
  <StateCatalog flashMessages={flashMessage()}/>,
  document.getElementById('state-catalog')
);
