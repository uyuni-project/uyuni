'use strict';

var React = require("react")

var Messages = require("../components/messages").Messages
var Button = require("../components/buttons").Button;
const AsyncButton = require("../components/buttons").AsyncButton;
var Network = require("../utils/network");
const Formats = require("../utils/functions").Formats;
var FormGenerator = require("../utils/form-generator");

var FormulaDetail = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": null,
            "messages": []
        };
        Network.get("/rhn/manager/groups/details/formula/form/" + groupId + "/" + formulaId).promise.then(data => {
          this.setState({"serverData" : data});
        });
        return st;
    },

	updateFormula: function(e) {
		e.preventDefault(); // prevent default form redirect
		
		var formData = {};
		formData.groupId = groupId;
		formData.url = window.location.href;
		formData.formula_name = this.state.serverData.formula_name;
		formData.content = {};
		$("#editFormulaForm input, #editFormulaForm select").each(function(index, element) {
			if (element.id == "") return;
 			else if (element.type == "checkbox")
 				assignValueWithId(formData.content, element.id, element.checked);
 			else
 				assignValueWithId(formData.content, element.id, element.value);
		});
		
        Network.post("/rhn/manager/groups/details/formula/form/save", JSON.stringify(formData), "application/json").promise.then(
		(data) => {
			window.location.href = data.url
		},
        (xhr) => {
           if (xhr.status == 400) {
               // validation err
               var errs = JSON.parse(xhr.responseText);
               this.setState({errors: errs});
           } else {
               this.setState({errors: [t("An internal server error occurred")]});
           }
        });
	},

    render: function() {
		var messages = this.state.messages;
        if(typeof this.props.flashMessages !== "undefined") {
			messages = messages.concat(this.props.flashMessages);
            msg = <Messages items={this.props.flashMessages}/>;
        }
        var msg = messages.length > 0 ? <Messages items={messages}/> : null;

        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }

		if (this.state.serverData == null) {
			return (
		    	<div>
					{errs}{msg}
					<div className="panel panel-default">
						<div className="panel-heading">
							<h4>Formula not found!</h4>
						</div>
						<div className="panel-body">
							Click <a href={"/rhn/manager/groups/details/formulas?sgid=" + groupId}>here</a> to manage the formulas of this server group.
						</div>
					</div>
				</div>
			);
		}			
		else {
		    return (
		    	<div>
		    		{generateFormulaNavBar(this.state.serverData.formulaList, formulaId)}
					{errs}{msg}
					<form id="editFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
						<div className="panel panel-default">
							<div className="panel-heading">
								<h4>{ FormGenerator.toTitle(this.state.serverData.formula_name || "Formula not found") }</h4>
							</div>
							<div className="panel-body">
								{FormGenerator.generateForm(this.state.serverData.values, this.state.serverData.layout, this.state.serverData.formula_name, "group")}
								<div className="row">
									<div className="col-md-2 col-md-offset-3">
										<Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className="btn btn-success" handler={function(e){$('<input type="submit">').hide().appendTo($("#editFormulaForm")).click().remove();}} />
									</div>
								</div>
							</div>
						</div>
					</form>
				</div>
		    );
		}
    }
});

function generateFormulaNavBar(formulaList, activeId) {
	var tabs = [<li role="presentation"><a href={"/rhn/manager/groups/details/formulas?sgid=" + groupId}>Formulas</a></li>];
	for (var i in formulaList)
		tabs.push(<li role="presentation" className={(i == activeId)?"active":""}><a href={ "/rhn/manager/groups/details/formula/" + i + "?sgid=" + groupId}>{FormGenerator.toTitle(formulaList[i])}</a></li>);
	return (
		<ul className="nav nav-tabs">
			{tabs}
		</ul>
	);
}

function assignValueWithId(dir, id, value) {
	var parents = id.split("$");
	
	for (var i in parents.slice(0, -1)) {
		if (dir[parents[i]] == undefined)
			dir[parents[i]] = {};
		dir = dir[parents[i]];
	}
	dir[parents[parents.length-1]] = value;
}

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

React.render(
  <FormulaDetail flashMessages={flashMessage()} />,
  document.getElementById('formula')
);

