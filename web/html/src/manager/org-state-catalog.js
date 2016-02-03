'use strict';

var React = require("react")
var TableComponent = require("../components/table.js")
var PanelComponent = require("../components/panel.js")

var Table = TableComponent.Table
var TableCell = TableComponent.TableCell
var TableRow = TableComponent.TableRow
var Panel = PanelComponent.Panel
var PanelButton = PanelComponent.PanelButton

var StateCatalog = React.createClass({

    getInitialState: function() {
        return {"serverData": []};
    },

    refreshServerData: function() {
        $.get("/rhn/manager/state_catalog/data", data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },
//            <ul>
//               { this.state.serverData.map( (e) => {
//                    return <li>{e}</li>
//                })
//               }
//            </ul>
    render: function() {
        var button = <PanelButton text="Create state" icon="fa-plus" action="/rhn/manager/state_catalog/add"/>

        return (
            <Panel title="State catalog" icon="spacewalk-icon-virtual-host-manager" button={button}>
                <div>
                    <div className="spacewalk-list">
                        <Table headers={[t("State")]}
                          rows={statesToRows(this.state.serverData)}
                          loadState={this.props.loadState}
                          saveState={this.props.saveState}
                          sortRow={this.sortRow}
                          sortableColumns={[0]}
                        />
                    </div>
                </div>
            </Panel>
        );
    }

});

function statesToRows(serverData) {
  return serverData.map((s) => {
    var columns = [
      <TableCell content={s} />,
    ];
    return <TableRow columns={columns} raw_data={s} />
  });
}

React.render(
  <StateCatalog />,
  document.getElementById('state-catalog')
);