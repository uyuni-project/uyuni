"use strict";

const React = require("react");
var Messages = require("../components/messages").Messages;
var Button = require("../components/buttons").Button;
const Network = require("../utils/network");

const basicInputTypes = ["text", "email", "url", "date", "time"];

//props:
//dataUrl = url to get the server data
//noFormulaText = text to display if no formula is found
//addFormulaNavBar = function(formula_list, activeId) to add the formula nav bar
//formulaId = the id of the formula to be shown
//getFormulaUrl = function(formulaId) that returns the url to a formula page
//updateFormula = function(component) that gets called when the save button is pressed
//currentScope = current active scope (system or group)
class FormulaForm extends React.Component {
    constructor(props) {
        super(props);
        
        ["init", "updateFormula", "generateForm", "serializeValues"]
        .forEach(method => this[method] = this[method].bind(this));
        
        this.state = {
            serverData: null,
            messages: []
        };
        this.init();
    }
    
    init() {
        Network.get(this.props.dataUrl).promise.then(data => {
            console.log(data);
            this.setState({
            serverData: data
            });
        });
    }

    updateFormula(e) {
        e.preventDefault(); // prevent default form redirect
        this.props.updateFormula(this);
        window.scrollTo(0, 0);
    }
    
    serializeValues() {
        var values = {};
        $("#editFormulaForm input, #editFormulaForm select")
        .each(function(index, element) {
                if (element.id == "") return;
                 else if (element.type == "checkbox")
                     assignValueWithId(values, element.id, element.checked);
                 else
                     assignValueWithId(values, element.id, element.value);
            });
        return values;
    }
    
    generateForm() {
        var layout = this.state.serverData.layout;
        if (layout == undefined || layout == null) return;
        layout = preprocessLayout(layout);
        console.log(JSON.stringify(layout));

        var form = [];
        var values = generateValues(layout, this.state.serverData.group_data, this.state.serverData.system_data || {}, this.props.currentScope);
        console.log(JSON.stringify(values));

        for (var key in layout)
            form.push(generateFormItem(layout[key], values[key], "", this.props.currentScope));

        return form;
    }
    
    render() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can configure <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a> to automatically install and configure software. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0)
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;

        var errors = null;
        if (this.state.errors) {
            errors = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }

        if (this.state.serverData == null) {
            this.props.addFormulaNavBar(["Not found"], 0);
            return (
                <div>
                    {errors}{messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Formula not found!</h4>
                        </div>
                        <div className="panel-body">
                            {this.props.noFormulaText}
                        </div>
                    </div>
                </div>
            );
        }    
        else if (this.state.serverData.layout == undefined || this.state.serverData.layout == null) {
            this.props.addFormulaNavBar(this.state.serverData.formula_list || ["Not found"], formulaId);
            return (
                <div>
                    {errors}{messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Error while loading form!</h4>
                        </div>
                        <div className="panel-body">
                            The requested form could not get loaded! The corresponding layout file is either invalid or doesn't exist.
                        </div>
                    </div>
                </div>
            );
        }
        else {
            this.props.addFormulaNavBar(this.state.serverData.formula_list, formulaId);
            return (
                <div>
                    {errors}{messages}
                    <form id="editFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
                        <div className="panel panel-default">
                            <div className="panel-heading">
                                <h4>{toTitle(this.state.serverData.formula_name || "Formula not found")}</h4>
                            </div>
                            <div className="panel-body">
                                {this.generateForm()}
                                <div className="row">
                                    <div className="col-lg-6 col-lg-offset-3">
                                        <Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className="btn btn-success" handler={function(e){$('<input type="submit">').hide().appendTo($("#editFormulaForm")).click().remove();}} />
                                        <a id="next-btn" href={this.props.getFormulaUrl(this.props.formulaId + 1)} disabled={this.state.serverData.formula_list.length - 1 <= this.props.formulaId} className="btn btn-default pull-right">Next <i className="fa fa-arrow-right" /></a>
                                        <a id="prev-btn" href={this.props.getFormulaUrl(this.props.formulaId - 1)} disabled={this.props.formulaId == 0} className="btn btn-default pull-right"><i className="fa fa-arrow-left" /> Prev</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            );
        }
    }
}

function generatePassword() {
    var length = Math.floor(Math.random() * 10) + 15,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i)
        retVal += charset.charAt(Math.floor(Math.random() * n));
    return retVal;
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
            if (element.$type == "password") value = (system_data[key] || group_data[key] || element.$default || generatePassword());
            else value = (system_data[key] || group_data[key] || element.$default || null);
        else if (element_scope == "group")
            if (element.$type == "password") value = (group_data[key] || element.$default || generatePassword());
            else value = (group_data[key] || element.$default || null);
        else if (element_scope == "readonly")
            value = (element.$default || null);
    
        if (value != null)
            result[key] = value
    }
    return result;
}

function generateFormItem(element, value, parents, currentScope) {
    if (parents == "") var id = element.$id;
    else var id = parents + "$" + element.$id;
    var isDisabled = ((currentScope == element.$scope || element.$scope == "system") ? "" : " disabled");
    
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
                        <button className="btn btn-default" title="Generate new password" onClick={handleGeneratePassword}>
                            <i className="fa fa-key" />
                        </button>
                        <button className="btn btn-default" title="Show/hide password" onClick={toggle_view_password}>
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
    else if (element.$type == "group") {
        return (
        <div className="panel panel-default" id={id}>
            <div className="panel-heading">
                <h4>{ toTitle(element.$name) }</h4>
            </div>
            <div className="panel-body">
                { generateChildrenFormItems(element, value, id, currentScope) }
            </div>
        </div>
        );
    }
    else if (element.$type == "hidden-group")
        return generateChildrenFormItems(element, value, id, currentScope);
    else if (element.$type == "edit-group")
        return (
        <div className="panel panel-default" id={id}>
            <div className="panel-heading">
                <h4>{ toTitle(element.$name) }</h4>
            </div>
            <div className="panel-body">
                <div id={id + "$elements"}>
                    { generateEditGroup(element, value, id, currentScope) }
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
                <input type="checkbox" style={{width: 25 + "px", height: 25 + "px"}} name={element.$name} id={id} disabled={isDisabled} defaultChecked={value} />
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

function handleGeneratePassword(event) {
    event.preventDefault();
    
    var target = $(event.target);
    if (target.attr("class").startsWith("fa"))
        target = target.parent();
    target = document.getElementById(target.parent().siblings("input").attr("id"));
    if (target.disabled)
        return
    target.value = generatePassword();
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
        
        child.$name = (child.$name || toTitle(child_name));
        child.$id = child_name;
    }
    return layout;
}

function generateChildrenFormItems(element, value, id, currentScope) {
    var child_items = [];
    for (var child_name in element) {
        if (child_name.startsWith("$")) continue;
        child_items.push(generateFormItem(element[child_name], value[child_name], id, currentScope));
    }
    return child_items;
}

function generateSelectList(data) {
    var options = []
    for (var key in data)
        options.push(<option value={data[key]}>{data[key]}</option>);
    return options;
}

// WIP
function generateEditGroup(element, data, id) {
    var groups = [];
    for (var key in data) {
        var child = jQuery.extend(true, {"$name": key, "$type": "group"}, element.$element);
        groups.push(generateFormItem(child, data[key], id));
    }
    return groups;
}

// WIP
function addElementToEditGroup(event) {
    return;
    var target = event.target;
    if (!target.id.endsWith("$plus-button")) target = target.parentElement;
    //$("#" + target.parentElement.parentElement.id + "$elements").append(generateFormItem(,,target.parentElement.parentElement.id);
    document.getElementById(target.parentElement.parentElement.id + "$elements").innerHTML +=
         '<div class="panel panel-default">'
        +'    <div class="panel-heading">'
        +'        <h4>Title</h4>'
        +'    </div>'
        +'    <div class="panel-body">'
        +'        Some body text'
        +'    </div>'
        +'</div>';
}

function wrapGroupWithLabel(element_name, innerHTML) {
    return (
        <div className="form-group">
            { wrapLabel(element_name) }
            { innerHTML }
        </div>
    );
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

function assignValueWithId(dir, id, value) {
    var parents = id.split("$");
    
    for (var i in parents.slice(0, -1)) {
        if (dir[parents[i]] == undefined)
            dir[parents[i]] = {};
        dir = dir[parents[i]];
    }
    dir[parents[parents.length-1]] = value;
}

module.exports = {
    FormulaForm: FormulaForm,
    toTitle: toTitle
}
