'use strict';

var React = require("react");
const ReactDOM = require("react-dom");
const {Table, Column, SearchField} = require("../components/table");
var PanelComponent = require("../components/panel");
var Messages = require("../components/messages").Messages;
var Network = require("../utils/network");
var Panel = PanelComponent.Panel;


var FormulaCatalog = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": [],
            "messages": []
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
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can see your currently installed <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a>. You can apply these <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a> to server groups and configure them for whole groups or individual systems. This allows you to automatically install and configure software. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }
        return (
			<Panel title="Formula Catalog" icon="spacewalk-icon-salt-add">
			{messages}
			<div>
			  <Table
				data={this.state.serverData}
				identifier={this.rowKey}
				initialSortColumnKey="name"
				searchPanel={
					<SearchField filter={this.searchData}
					  placeholder={t("Filter by formula name")} />
				}>
				<Column
				  columnKey="name"
				  comparator={this.sortByText}
				  header={t("Formula")}
				  cell={ (s) =>
					<a href={"/rhn/manager/formula_catalog/formula/" + s}>{s}</a> }
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
