'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const Buttons = require("../components/buttons")

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;

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
    
    applyRequest: function() {
    	if (this.state.serverData.added.length > 0 || this.state.serverData.removed.length > 0) {
			const response = confirm(t("There are unsaved changes. Do you want to proceed ?"))
			if (response == false) {
				return null;
			}
		}
    
		return Network.post(
		    "/rhn/manager/api/states/apply",
		    JSON.stringify({
		        id: groupId,
		        type: "GROUP",
		        states: []
		    }),
		    "application/json"
		    )
		    .promise.then( data => {
		      console.log("apply action queued:" + data)
		      this.setState({
		          messages: t("Applying the highstated has been scheduled for each minion server in this group")
		      });
		    });
	},

	saveRequest: function() {
		var serverData = this.state.serverData;
		var formData = {};
		formData.groupId = groupId;
		formData.url = window.location.href;
		formData.selected = serverData.selected.filter(function(e){
			return serverData.removed.indexOf(e) < 0;
			}).concat(serverData.added);
		
        Network.post("/rhn/manager/groups/details/formulas/apply", JSON.stringify(formData), "application/json").promise.then(
		data => {
                this.state.messages = [t("Formulas saved!")]
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
		var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can select <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a> for this group, which can then be configured on group and system level. This allows you to automatically install and configure software. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }
        
        var errors = null;
        if (this.state.errors) {
            errors = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }
		addFormulaNavBar(this.state.serverData.selected);
        
        return (
        	<div>
				{errors}{messages}
				<div className="panel panel-default">
					<div className="panel-heading">
						<h4>Formulas</h4>
					</div>
					<div className="panel-body">
				    	<form id="chooseFormulaForm" className="form-horizontal" onSubmit={function (e) {e.preventDefault();}}>
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
				        			<span className="btn-group">
					        			<Button id="save-btn" className="btn btn-default" icon="fa-floppy-o" handler={this.saveRequest} text={t("Save")} />
										<AsyncButton id="apply-btn" defaultType="btn-success" action={this.applyRequest} name={t("Apply Highstate")} />
					        		</span>
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
  <GroupFormulas />,
  document.getElementById('formulas')
);
