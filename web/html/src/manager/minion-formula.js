'use strict';

var React = require("react")

var Messages = require("../components/messages").Messages
var Button = require("../components/buttons").Button;
const AsyncButton = require("../components/buttons").AsyncButton;
var Network = require("../utils/network");
const Formats = require("../utils/functions").Formats;

const basicInputTypes = ["text", "password", "email", "url", "date", "time"];

var FormulaDetail = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": {"formula_name": "Loading..."},
            "messages": []
        };
        Network.get("/rhn/manager/systems/details/formula/form/" + serverId + "/" + formulaId).promise.then(data => {
          this.setState({"serverData" : data});
        });
        return st;
    },

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
    },

	updateFormula: function(e) {
		e.preventDefault(); // prevent default form redirect
		var formData = {};
		formData["serverId"] = serverId;
		formData["url"] = window.location.href;
		formData["formula_name"] = this.state.serverData["formula_name"];
		formData["content"] = {};
		$("#editFormulaForm input, #editFormulaForm select").each(function(index, element) {
			if (element.id == "") return;
 			else if (element.type == "checkbox")
 				assignValueWithId(formData["content"], element.id, element.checked);
 			else
 				assignValueWithId(formData["content"], element.id, element.value);
		});
		
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
		    		{ generateFormulaNavBar(this.state.serverData.formulaList, formulaId || -1) }
					{errs}{msg}
					<form id="editFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
						<div className="panel panel-default">
							<div className="panel-heading">
								<h4>{ toTitle(this.state.serverData.formula_name || "Formula not found") }</h4>
							</div>
							<div className="panel-body">
								{this.generateForm(this.state.serverData.values, this.state.serverData.layout, this.state.serverData.formula_name)}
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
		    /*
		    <div className="col-md-3">
		        <AsyncButton action={this.applyHighstate} name={t("Apply Highstate")} />
		    </div>
		    */
		}
    },
    
    generateForm: function(values, layout, formula_name) {
		if (layout == undefined || layout == null) return;
		layout = preprocessLayout(layout);
		//console.log(JSON.stringify(layout));

		var form = [];
		values = generateValues(values, layout);
		//console.log(JSON.stringify(values));

		if ((Object.keys(layout).length == 1) && (formula_name in layout) && layout[formula_name].$type == "group") {
			values = values[formula_name];
			layout = layout[formula_name];
			for (key in layout)
				if (!key.startsWith("$"))
					form.push(generateFormItem(layout[key], values[key], formula_name));
		}
		else
			for (var key in layout)
				form.push(generateFormItem(layout[key], values[key], ""));

		return form;
    }
});

function generateFormulaNavBar(formulaList, activeId) {
	var tabs = [];
	for (var i in formulaList)
		tabs.push(<li role="presentation" className={(i == activeId)?"active":""}><a href={ "/rhn/manager/systems/details/formula/" + i + "?sid=" + serverId}>{formulaList[i]}</a></li>);
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

function handleChange(event) {
	event.target.parent.value = event.target.value;
}

function generateValues(values, layout) {
	var result = {};
	/*
	if (layout.$type == "edit_group") {
		for (var key in values) {
			
	}*/
	
	for (var key in layout) {
		if (key.startsWith("$")) continue;

		var element = layout[key];
		if (element.$type == "group")
			result[key] = generateValues(values[key] || {}, element);
		/*else if (element.$type == "edit_group")
			result[key] = generateValues(values[key] || element.$default || {}, element);*/
		else
			result[key] = (values[key] || element.$default || "");
	}
	return result;
}

function generateFormItem(element, value, parents) {
	if (parents == "") var id = element.$name;
	else var id = parents + "$" + element.$name;
	
	if (basicInputTypes.indexOf(element.$type) >= 0) //Element is a basic html input type
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type={element.$type} name={element.$name} id={id} className="form-control" defaultValue={value} />
			</div>
		);
	else if (element.$type == "datetime")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="datetime-local" name={element.$name} id={id} className="form-control" defaultValue={value} />
			</div>
		);
	else if (element.$type == "number")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="number" min="0" steps="1" name={element.$name} id={id} className="form-control" defaultValue={value} />
			</div>
		);
	else if (element.$type == "group")
		return (
		<div className="panel panel-default" id={id}>
			<div className="panel-heading">
				<h4>{ toTitle(element.$name) }</h4>
			</div>
			<div className="panel-body">
				{ generateChildrenFormItems(element, value, id) }
			</div>
		</div>
		);
	else if (element.$type == "edit_group")
		return (
		<div className="panel panel-default" id={id}>
			<div className="panel-heading">
				<h4>{ toTitle(element.$name) }</h4>
			</div>
			<div className="panel-body">
				<div id={id + "$elements"}>
					{ generateEditGroup(element, value, id) }
				</div>
				<Button id={id + "$plus-button"} icon="fa-plus" text="Add Element" className="btn-default btn-3d btn-sm" handler={addElementToEditGroup} />
			</div>
		</div>
		);
	else if (element.$type == "select")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<select className="form-control" name={element.$name} id={id} defaultValue={value}>
					{generateSelectList(element.values)}
				</select>
			</div>
		);
	else if (element.$type == "boolean")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="checkbox" name={element.$name} id={id} defaultChecked={value} />
			</div>
		);
	else
		return (
			<div className="form-group">
				{ wrapLabelTitle(element.$name || "null")}
				<div className="col-lg-6" id={id}>
					{ JSON.stringify(value) }
				</div>
			</div>
		);
}

function preprocessLayout(layout) {
	for (var child_name in layout) {
		if (child_name.startsWith("$")) continue;
			
		var child = layout[child_name];
		if (!("$type" in child)) {
			child.$type = "text"
			child.$default = (child.$default || "");
		}
		else if (child.$type == "boolean")
			child.$default = (child.$default || false);
		else if (child.$type == "select")
			child.$default = (child.$default || child.values[0]);
		else if (child.$type == "group")
			child = preprocessLayout(child);
		else if (child.$type == "edit_group")
			child.$element = preprocessLayout(child.$element);
		else
			child.$default = (child.$default || "");
		
		child.$name = (child.$name || child_name);
	}
	return layout;
}

function generateChildrenFormItems(element, value, id) {
	var child_items = [];
	for (var child_name in element) {
		if (child_name.startsWith("$")) continue;
		child_items.push(generateFormItem(element[child_name], value[child_name], id));
	}
	return child_items;
}

function generateEditGroup(element, data, id) {
	var groups = [];
	for (var key in data) {
		var child = jQuery.extend(true, {"$name": key, "$type": "group"}, element.$element);
		groups.push(generateFormItem(child, data[key], id));
	}
	return groups;
}

function generateSelectList(data) {
	var options = []
	for (var key in data)
		options.push(<option value={data[key]}>{data[key]}</option>);
	return options;
}

// Add New Element to edit group (currently unused as edit_group is on hold)
// TODO: should probably modify saved values and cause rerender
function addElementToEditGroup(event) {
	return;
	var target = event.target;
	if (!target.id.endsWith("$plus-button")) target = target.parentElement;
	//$("#" + target.parentElement.parentElement.id + "$elements").append(generateFormItem(,,target.parentElement.parentElement.id);
	document.getElementById(target.parentElement.parentElement.id + "$elements").innerHTML +=
		 '<div class="panel panel-default">'
		+'	<div class="panel-heading">'
		+'		<h4>Title</h4>'
		+'	</div>'
		+'	<div class="panel-body">'
		+'		Some body text'
		+'	</div>'
		+'</div>';
}

function wrapGroupWithLabel(element_name, innerHTML) {
	return (
		<div className="form-group">
			{ wrapLabelTitle(element_name) }
			{ innerHTML }
		</div>
	);
}

function wrapLabelTitle(text) {
	return wrapLabel(toTitle(text), text);
}

function wrapLabel(text, label_for) {
	return (
		<label htmlFor={ label_for } className="col-lg-3 control-label">
			{ text + ":" }
		</label>
	);
}

function toTitle(str) {
	return str.replace(new RegExp("_", 'g'), " ").replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

function msg(severityIn, textIn) {
    return {severity: severityIn, text: textIn};
}

React.render(
  <FormulaDetail flashMessages={flashMessage()} />,
  document.getElementById('formula')
);

