'use strict';

var React = require("react");

var Messages = require("../components/messages").Messages;
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
        Network.get("/rhn/manager/systems/details/formula/form/" + serverId + "/" + formulaId).promise.then(data => {
          this.setState({"serverData" : data});
        });
        return st;
    },
/*
    applyHighstate: function() {
    	// TODO: disabled only for testing, but: should you even be able to apply highstate from here?
    	console.warn("Applying highstate is currently disabled for testing purposes!");
    	return;
    	
        const request = Network.post(
            "/rhn/manager/api/states/apply",
            JSON.stringify({
                id: serverId,
                type: "SERVER",
                states: [],
                earliest: Formats.LocalDateTime(new Date())
            }),
            "application/json"
        ).promise.then(data => {
            this.state.messages.push(msg('info', <span>{t("Applying the highstate has been ")}
                    <a href={"/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + data}>{t("scheduled")}</a>
                    {t(".")}</span>))
            this.setState({
                messages: this.state.messages
            });
        });
        return request;
    },*/

	updateFormula: function(e) {
		e.preventDefault(); // prevent default form redirect
		var formData = {};
		formData["serverId"] = serverId;
		formData["url"] = window.location.href;
		formData["formula_name"] = this.state.serverData["formula_name"];
		formData["content"] = FormGenerator.serializeValues($("#editFormulaForm input, #editFormulaForm select"));
		
        Network.post("/rhn/manager/systems/details/formula/form/save", JSON.stringify(formData), "application/json").promise.then(
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
							<h4>No formula found!</h4>
						</div>
						<div className="panel-body">
							No formulas found for this server! Add a formula to a group this server is part of.
						</div>
					</div>
				</div>
			);
		}			
		else {
		    return (
		    	<div>
		    		{ generateFormulaNavBar(this.state.serverData.formula_list, formulaId) }
					{errs}{msg}
					<form id="editFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
						<div className="panel panel-default">
							<div className="panel-heading">
								<h4>{ FormGenerator.toTitle(this.state.serverData.formula_name || "Formula not found") }</h4>
							</div>
							<div className="panel-body">
								{FormGenerator.generateForm(this.state.serverData.layout, this.state.serverData.group_data, this.state.serverData.system_data, this.state.serverData.formula_name, "system")}
								<div className="row">
									<div className="col-lg-6 col-lg-offset-3">
										<Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className="btn btn-success" handler={function(e){$('<input type="submit">').hide().appendTo($("#editFormulaForm")).click().remove();}} />
										<a id="next-btn" href={ "/rhn/manager/systems/details/formula/" + (formulaId + 1) + "?sid=" + serverId} disabled={this.state.serverData.formula_list.length - 1 <= formulaId} className="btn btn-default pull-right">Next <i className="fa fa-arrow-right" /></a>
										<a id="prev-btn" href={ "/rhn/manager/systems/details/formula/" + (formulaId - 1) + "?sid=" + serverId} disabled={formulaId == 0} className="btn btn-default pull-right"><i className="fa fa-arrow-left" /> Prev</a>
									</div>
								</div>
							</div>
						</div>
					</form>
				</div>
		    );
		}
    },
});

function generateFormulaNavBar(formulaList, activeId) {
	var tabs = [];
	for (var i in formulaList)
		tabs.push(<li role="presentation" className={(i == activeId)?"active":""}><a href={ "/rhn/manager/systems/details/formula/" + i + "?sid=" + serverId}>{FormGenerator.toTitle(formulaList[i])}</a></li>);
	return (
		<ul className="nav nav-tabs">
			{tabs}
		</ul>
	);
}

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

React.render(
  <FormulaDetail flashMessages={flashMessage()} />,
  document.getElementById('formula')
);

