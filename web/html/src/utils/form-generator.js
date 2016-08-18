"use strict";

const React = require("react");

const basicInputTypes = ["text", "password", "email", "url", "date", "time"];

var globalScope = "system";
    
function generateForm(values, layout, formula_name, global_scope) {
	if (layout == undefined || layout == null) return;
	layout = preprocessLayout(layout);
	//console.log(JSON.stringify(layout));
	
	globalScope = global_scope;

	var form = [];
	values = generateValues(values, layout);
	//console.log(JSON.stringify(values));

	if ((Object.keys(layout).length == 1) && (formula_name in layout) && (layout[formula_name].$type == "group" || layout[formula_name].$type == "hidden-group")) {
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

function generateValues(values, layout) {
	var result = {};
	/*
	if (layout.$type == "edit-group") {
		for (var key in values) {
			
	}*/
	
	for (var key in layout) {
		if (key.startsWith("$")) continue;

		var element = layout[key];
		if (element.$type == "group" || element.$type == "hidden-group")
			result[key] = generateValues(values[key] || {}, element);
		/*else if (element.$type == "edit-group")
			result[key] = generateValues(values[key] || element.$default || {}, element);*/
		else
			result[key] = (values[key] || element.$default || "");
	}
	return result;
}

function generateFormItem(element, value, parents) {
	if (parents == "") var id = element.$name;
	else var id = parents + "$" + element.$name;
	var isDisabled = (globalScope == element.$scope || element.$scope == "system") ? "" : " disabled";
	
	if (basicInputTypes.indexOf(element.$type) >= 0) //Element is a basic html input type
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type={element.$type} name={element.$name} id={id} className="form-control" disabled={isDisabled} defaultValue={value} />
			</div>
		);
	else if (element.$type == "datetime")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="datetime-local" name={element.$name} id={id} className="form-control" disabled={isDisabled} defaultValue={value} />
			</div>
		);
	else if (element.$type == "number")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="number" min="0" steps="1" name={element.$name} id={id} className="form-control" disabled={isDisabled} defaultValue={value} />
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
	else if (element.$type == "hidden-group")
		return generateChildrenFormItems(element, value, id);
	else if (element.$type == "edit-group")
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
				<select className="form-control" disabled={isDisabled} name={element.$name} id={id} defaultValue={value}>
					{generateSelectList(element.$values)}
				</select>
			</div>
		);
	else if (element.$type == "boolean")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<input type="checkbox" name={element.$name} id={id} disabled={isDisabled} defaultChecked={value} />
			</div>
		);
	else
		return (
			<div className="form-group">
				{ wrapLabelTitle(element.$name || "Element not found")}
				<div className="col-lg-6" id={id}>
					{ JSON.stringify(value) }
				</div>
			</div>
		);
}

function preprocessLayout(layout, scope="system") {
	for (var child_name in layout) {
		if (child_name.startsWith("$")) continue;
			
		var child = layout[child_name];
		if (!("$scope" in child))
			child.$scope = scope;
		
		if (!("$type" in child)) {
			child.$type = "text"
			child.$default = (child.$default || "");
		}
		else if (child.$type == "boolean")
			child.$default = (child.$default || false);
		else if (child.$type == "select")
			child.$default = (child.$default || child.$values[0]);
		else if (child.$type == "group" || child.$type == "hidden-group")
			child = preprocessLayout(child, child.$scope);
		else if (child.$type == "edit-group")
			child.$element = preprocessLayout(child.$element, child.$scope);
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

function generateSelectList(data) {
	var options = []
	for (var key in data)
		options.push(<option value={data[key]}>{data[key]}</option>);
	return options;
}

function generateEditGroup(element, data, id) {
	var groups = [];
	for (var key in data) {
		var child = jQuery.extend(true, {"$name": key, "$type": "group"}, element.$element);
		groups.push(generateFormItem(child, data[key], id));
	}
	return groups;
}

// Add New Element to edit group (currently unused as edit-group is on hold)
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
	return str.replace(new RegExp("_|-", 'g'), " ").replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

module.exports = {
    generateForm: generateForm,
    toTitle: toTitle
}
