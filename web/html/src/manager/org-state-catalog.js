'use strict';

var React = require("react");
var TableComponent = require("../components/table");
var PanelComponent = require("../components/panel");
var Messages = require("../components/messages").Messages;
var Network = require("../utils/network");

var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
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
        Network.get("/rhn/manager/state_catalog/data").promise.then(data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },

    compareRows: function(a, b, columnIndex, order) {
        var orderCondition = order ? 1 : -1;
        var aValue = a.props["raw_data"];
        var bValue = b.props["raw_data"];
        var result = aValue.localeCompare(bValue);
        return result * orderCondition;
    },

    render: function() {
        var button = <PanelButton text="Create state" icon="fa-plus" action="/rhn/manager/state_catalog/state"/>;

        var msg = null;

        if(typeof this.props.flashMessages !== "undefined") {
            msg = <Messages items={this.props.flashMessages}/>;
        }
        return (
            <Panel title="States Catalog" icon="spacewalk-icon-virtual-host-manager" button={button}>
                {msg}
                <div>
                    <Table headers={[t("State")]}
                      rows={statesToRows(this.state.serverData)}
                      loadState={this.props.loadState}
                      saveState={this.props.saveState}
                      rowComparator={this.compareRows}
                      sortableColumnIndexes={[0]}
                      rowFilter={(tableRow, searchValue) => tableRow.props["raw_data"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
                      filterPlaceholder={t("Filter by state name:")}
                    />
                </div>
            </Panel>
        );
    }

});

function statesToRows(serverData) {
  return serverData.map((s) => {
    var link = <a href={"/rhn/manager/state_catalog/state/" + s}>{s}</a>
    var columns = [
      <TableCell content={link} />,
    ];
    return <TableRow columns={columns} raw_data={s} />
  });
}

React.render(
  <StateCatalog flashMessages={flashMessage()}/>,
  document.getElementById('state-catalog')
);
