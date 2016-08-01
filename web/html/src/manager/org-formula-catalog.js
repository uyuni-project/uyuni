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


var FormulaCatalog = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": []
        };
        return st;
    },

    refreshServerData: function() {
        Network.get("/rhn/manager/formula_catalog/data").promise.then(data => {
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
        var msg = null;

        if(typeof this.props.flashMessages !== "undefined") {
            msg = <Messages items={this.props.flashMessages}/>;
        }
        return (
            <Panel title="Formula Catalog" icon="spacewalk-icon-salt-add">
                {msg}
                <div>
                    <Table headers={[t("Formula")]}
                      rows={formulasToRows(this.state.serverData)}
                      loadState={this.props.loadState}
                      saveState={this.props.saveState}
                      rowComparator={this.compareRows}
                      sortableColumnIndexes={[0]}
                      rowFilter={(tableRow, searchValue) => tableRow.props["raw_data"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
                      filterPlaceholder={t("Filter by formula name:")}
                    />
                </div>
            </Panel>
        );
    }

});

function formulasToRows(serverData) {
  return serverData.map((f) => {
    var link = <a href={"/rhn/manager/formula_catalog/formula/" + f}>{f}</a>
    var columns = [
      <TableCell content={link} />,
    ];
    return <TableRow columns={columns} raw_data={f} />
  });
}

React.render(
  <FormulaCatalog flashMessages={flashMessage()}/>,
  document.getElementById('formula-catalog')
);
