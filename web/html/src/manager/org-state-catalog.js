'use strict';

var React = require("react");
var t = require("../components/translation")
var TableComponent = require("../components/table");
var PanelComponent = require("../components/panel");
var Messages = require("../components/messages").Messages;

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
//        if (this.props.flashMessages) {
//            st["flashMessages"] = this.props.flashMessages;
//        }
        return st;
    },

    refreshServerData: function() {
        $.get("/rhn/manager/state_catalog/data", data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },

//    componentDidMount: function() {
//        var views = 1;
//        if(this.state.flashMessagesViews) {
//            views = this.state.flashMessagesViews + 1;
//        }
//        this.setState({ "flashMessagesViews": views });
//    },
//
//    shouldComponentUpdate: function() {
//        return typeof this.state.flashMessagesViews === "undefined" ||
//            this.state.flashMessagesViews > 1;
//    },

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
//        if(typeof this.state.flashMessages !== "undefined" && this.state.flashMessages.length > 0) {
//            msg = <Messages items={this.props.flashMessages}/>
//        }

        if(typeof this.props.flashMessages !== "undefined") {
//            var items = {severity: "error", text: "alalalal"};
            msg = <Messages items={this.props.flashMessages}/>;
        }
        return (
            <Panel title="States Catalog" icon="spacewalk-icon-virtual-host-manager" button={button}>
                {msg}
                <div>
                    <div className="spacewalk-list">
                        <Table headers={[t("State")]}
                          rows={statesToRows(this.state.serverData)}
                          loadState={this.props.loadState}
                          saveState={this.props.saveState}
                          rowComparator={this.compareRows}
                          sortableColumns={[0]}
                          dataFilter={(tableRow, searchValue) => tableRow.props["raw_data"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1}
                          searchPlaceholder={t("Filter by state name:")}
                        />
                    </div>
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