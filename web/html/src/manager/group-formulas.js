'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

var Messages = require("../components/messages").Messages
var MessagesUtils = require("../components/messages").Utils;
var Button = require("../components/buttons").Button;
const Network = require("../utils/network");

var toTitle = require("../components/FormulaForm").toTitle;

var GroupFormulas = React.createClass({
    
    requestServerData() {
		Network.get("/rhn/manager/groups/details/formulas/data/" + groupId).promise.then(data => {
			data.selected = data.selected || [];
			data.added = data.added || [];
			data.removed = data.removed || [];
			this.setState({"serverData" : data});
		});
	},

    getInitialState: function() {
        var st = {
            "serverData": {"selected": [], "added": [], "removed": []},
            "messages": []
        };
        this.requestServerData();
        return st;
    },

	updateFormula: function(e) {
		e.preventDefault(); // block redirect from the form
		var serverData = this.state.serverData;
		var formData = {};
		formData.groupId = groupId;
		formData.url = window.location.href;
		formData.selected = serverData.selected.filter(function(e){
			return serverData.removed.indexOf(e) < 0;
			}).concat(serverData.added);
		
        Network.post("/rhn/manager/groups/details/formulas/apply", JSON.stringify(formData), "application/json").promise.then(
		data => {
                this.state.messages = MessagesUtils.info(t("Formulas applied!"))
                this.requestServerData();
		},
        (xhr) => {
           if (xhr.status == 400 || xhr.status == 403) {
               // validation err or permission denied
               var errs = JSON.parse(xhr.responseText);
               this.setState({errors: errs});
           } else {
               this.setState({errors: [t("An internal server error occurred")]});
           }
        });
	},
	
	onListItemClick: function(e) {
		e.preventDefault();
		var formula = (e.target.href == undefined ? e.target.parentElement.id : e.target.id);
		var serverData = this.state.serverData;
		if (serverData.selected.indexOf(formula) >= 0) {
			if (serverData.removed.indexOf(formula) >= 0)
				serverData.removed.splice(serverData.removed.indexOf(formula), 1);
			else
				serverData.removed.push(formula);
		}
		else if (serverData.added.indexOf(formula) >= 0)
			serverData.added.splice(serverData.added.indexOf(formula), 1);
		else
			serverData.added.push(formula);
		this.forceUpdate();
	},
	
	generateList: function() {
		var list = [];
		var formulas = this.state.serverData.formulas;
		for (var i in formulas) {
			list.push(
				<a href="#" onClick={this.onListItemClick} id={formulas[i]} key={formulas[i]} className={"list-group-item " + this.getListItemClass(formulas[i])}>
					<i className={"fa " + this.getListItemIcon(formulas[i])}></i>
					{formulas[i]}
				</a>);
		}
		return list;
	},
	
	getListItemClass(formula) {
		var serverData = this.state.serverData;
		if (serverData.added.indexOf(formula) >= 0)
			return "list-group-item-success";
		else if (serverData.removed.indexOf(formula) >= 0)
			return "list-group-item-danger";
		else if (serverData.selected.indexOf(formula) >= 0)
			return "list-group-item-info";
		else return "";
	},
	
	getListItemIcon(formula) {
		var serverData = this.state.serverData;
		if (serverData.removed.indexOf(formula) >= 0)
			return "fa-times text-danger";
		else if (serverData.added.indexOf(formula) >= 0)
			return "fa-plus text-success";
		else if (serverData.selected.indexOf(formula) >= 0)
			return "fa-check text-success";
		else return "";
	},

    render: function() {
		var messages = this.state.messages || [];
        if(typeof this.props.flashMessages !== "undefined")
			messages = messages.concat(this.props.flashMessages);
        var msg = messages.length > 0 ? <Messages items={messages}/> : null;
        
        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }
		addFormulaNavBar(this.state.serverData.selected);
        
        return (
        	<div>
				{errs}{msg}
				<div className="panel panel-default">
					<div className="panel-heading">
						<h4>Formulas</h4>
					</div>
					<div className="panel-body">
				    	<form id="chooseFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
				        	<div className="form-group">
								<label htmlFor="chooseFormulas" className="col-lg-3 control-label">
									Choose formulas:
								</label>
								<div className="col-lg-6">
									<div id="chooseFormulas" className="list-group">
									  {this.generateList()}
									</div>
								</div>
							</div>
									
				        	<div className="form-group">
				        		<div className="col-lg-offset-3 col-lg-6">
				        			<Button id="save-btn" icon="fa-floppy-o" text="Apply" className="btn btn-success" handler={this.updateFormula} />
				        			<Button id="reset-btn" icon="fa-undo" text="Reset Changes" className="btn btn-default pull-right" handler={this.requestServerData} />
				        		</div>
				        	</div>
				    	</form>
				    </div>
				</div>
		    </div>
        );
    }
});

function addFormulaNavBar(formulaList) {
	$("#formula-nav-bar").remove();
	
	var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
	navBar += "<li class='active'><a href='/rhn/manager/groups/details/formulas?sgid=" + groupId + "'>Formulas</a></li>\n";
	for (var i in formulaList)
		navBar += "<li><a href='/rhn/manager/groups/details/formula/" + i + "?sgid=" + groupId + "'>" + toTitle(formulaList[i]) + "</a></li>\n";
	navBar += "</ul>"
	$(".spacewalk-content-nav").append(navBar);
}

ReactDOM.render(
  <GroupFormulas flashMessages={flashMessage()} />,
  document.getElementById('formulas')
);
