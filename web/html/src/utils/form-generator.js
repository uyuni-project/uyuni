"use strict";

const React = require("react");
var Button = require("../components/buttons").Button;

const basicInputTypes = ["text", "email", "url", "date", "time"];

var globalScope = "system";
    
function generateForm(layout, group_data, system_data, formula_name, global_scope) {
	if (layout == undefined || layout == null) return;
	layout = preprocessLayout(layout);
	//console.log(JSON.stringify(layout));
	
	globalScope = global_scope;

	var form = [];
	var values = generateValues(layout, group_data, system_data);
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

function generateValues(layout, group_data, system_data, scope="system") {
	var result = {};
	
	for (var key in layout) {
		if (key.startsWith("$")) continue;
		
		var value = null
		var element = layout[key];
		var element_scope = element.$scope || scope;
		
		if (element.$type == "group" || element.$type == "hidden-group")
			value = generateValues(element, group_data[key] || {}, system_data[key] || {}, element_scope);
		/*else if (element.$type == "edit-group")
			value = generateValues(values[key] || element.$default || {}, element);*/
		else if (element_scope == "system")
			value = (system_data[key] || group_data[key] || element.$default || null);
		else if (element_scope == "group")
			value = (group_data[key] || element.$default || null);
		else if (element_scope == "readonly")
			value = (element.$default || null);
		
		if (value != null)
			result[key] = value
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
	else if (element.$type == "password")
		return wrapGroupWithLabel(element.$name,
			<div className="col-lg-6">
				<div className="input-group">
					<input type={element.$type} name={element.$name} id={id} className="form-control" disabled={isDisabled} defaultValue={value} />
					<span className="input-group-btn">
						<button className="btn btn-default" onClick={toggle_view_password}>
							<i className="fa fa-eye" />
						</button>
					</span>
				</div>
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

function toggle_view_password(event) {
	event.preventDefault();
	
	var target = $(event.target);
	if (target.attr("class").startsWith("fa"))
		target = target.parent();
	var pwd_input = document.getElementById(target.parent().siblings("input").attr("id"));
	if (pwd_input.type == "password")
		pwd_input.type = "text";
	else
		pwd_input.type = "password";
	target.children("i.fa").toggleClass("fa-eye");
	target.children("i.fa").toggleClass("fa-eye-slash");
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
