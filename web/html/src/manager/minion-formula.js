'use strict';

var React = require("react")

var Messages = require("../components/messages").Messages
var Button = require("../components/buttons").Button;
const AsyncButton = require("../components/buttons").AsyncButton;
var Network = require("../utils/network");
const Formats = require("../utils/functions").Formats;

var FormulaDetail = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": [],
            messages: []
        };
        Network.get("/rhn/manager/systems/details/formula/form/" + serverId).promise.then(data => {
          this.setState({"serverData" : data});
        });
        return st;
    },

    applyHighstate: function() {
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
    },

	updateFormula: function(e) {
		e.preventDefault(); // prevent default form redirect
		var formData = {};
		formData["serverId"] = serverId;
		formData["url"] = window.location.href;
		formData["formula_name"] = this.state.serverData["formula_name"];
		formData["content"] = {};
		$("#editFormulaForm input, #editFormulaForm select").each(function(index, element) {
 			if (element.type.toLowerCase() == "checkbox")
 				formData["content"][element.id] = element.checked;
 			else
 				formData["content"][element.id] = element.value;
		});

        Network.post("/rhn/manager/systems/details/formula/form/save", JSON.stringify(formData), "application/json").promise.then(
		(data) => {
			console.log(data)
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
        
		var form;
		if (this.state.serverData == null) {
			form = (
				<div className="form-group">
					<div className="col-lg-offset-3 col-lg-6">
						No formulas found for this server! Add a formula to a group this server is part of.
					</div>
				</div>
			);
		}
		else {
			form = (
				<div>
					{createForm(this.state.serverData["values"], this.state.serverData["layout"])}
					<div className="row">
			    		<div className="col-md-2 col-md-offset-3">
			    			<Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className="btn btn-success" handler={this.updateFormula} />
			    		</div>
		                <div className="col-md-3">
		                    <AsyncButton action={this.applyHighstate} name={t("Apply Highstate")} />
		                </div>
			    	</div>
				</div>
	        );
		}
		
        return (
        	<div>
				{errs}{msg}
				<div className="panel panel-default">
					<div className="panel-heading">
						<h4>{"Edit Formula: " + (this.state.serverData == null ? "" : this.state.serverData["formula_name"])}</h4>
					</div>
					<div className="panel-body">
				    	<form id="editFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
				        	{form}
				    	</form>
				    </div>
				</div>
		    </div>
        );

    }
});
	
function handleChange(event) {
	event.target.parent.value = event.target.value;
}

function createForm(values, layout) {
	var form = [];
	for (var key in layout) {
		if (key in values)
			form.push(createFormGroup(key, layout[key], values[key]));
		else
			form.push(createFormGroup(key, layout[key], layout[key]));
	}
	return form;
}

function createFormGroup(key, data, value) {
	if (typeof data == "string") {
		return (
			<div className="form-group">
				<label htmlFor={key} className="col-lg-3 control-label">
					{key + ":"}
				</label>
				<div className="col-lg-6">
					<input type="text" name={key} id={key} className="form-control" defaultValue={value} />
				</div>
			</div>
		);
	}
	else if (typeof data == "boolean") {
		return (
			<div className="form-group">
				<label htmlFor={key} className="col-lg-3 control-label">
					{key + ":"}
				</label>
				<input type="checkbox" name={key} id={key} defaultChecked={value} />
			</div>
		);
	}
	else if (typeof data == "object") {
		return (
			<div className="form-group">
				<label htmlFor={key} className="col-lg-3 control-label">
					{key + ":"}
				</label>
				<div className="col-lg-6">
					<select className="form-control" name={key} id={key} defaultValue={typeof value == "object" ? "" : value}>
						{createSelectList(data)}
					</select>
				</div>
			</div>
		);
	}
	else {
		return (
			<div className="form-group">
				<label htmlFor={key} className="col-lg-3 control-label">
					{key + ":"}
				</label>
				<div className="col-lg-6" id={key}>
					{data + " -> " + value + " (type: " + (typeof data) + ")"}
				</div>
			</div>
		);
	}
}

function createSelectList(data) {
	var options = []
	for (var key in data) {
		options.push(<option value={data[key]}>{data[key]}</option>);
	}
	return options;
}

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

React.render(
  <FormulaDetail flashMessages={flashMessage()} />,
  document.getElementById('formula')
);

