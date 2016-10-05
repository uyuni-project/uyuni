"use strict";

const React = require("react");
const Buttons = require("../components/buttons");
const Network = require("../utils/network");
const Messages = require("../components/messages").Messages;

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;

const basicInputTypes = ["text", "email", "url", "date", "time"];
var formulaFormInstance;

//props:
//dataUrl = url to get the server data
//addFormulaNavBar = function(formulaList, activeFormulaId) to add the formula nav bar
//formulaId = the id of the formula to be shown
//getFormulaUrl = function(formulaId) that returns the url to a formula page by id (used for prev/next buttons)
//saveFormula = function(component) that gets called when the save button is pressed
//currentScope = current active scope (system or group)
class FormulaForm extends React.Component {
    constructor(props) {
        super(props);
        formulaFormInstance = this;

        ["init", "saveFormula", "generateForm", "generateFormItem", "generateChildrenFormItems", "checkVisibleCondition", "clearValues",
        "getValueById", "resetValue", "handleChange", "handleGeneratePassword", "handleTogglePasswordVisibility"]
        .forEach(method => this[method] = this[method].bind(this));

        this.state = {
            formulaName: "",
            formulaList: [],
            formulaLayout: {},
            formulaValues: {},
            formulaChanged: false,
            messages: [],
            errors: []
        };

        window.addEventListener("beforeunload", function (e) {
            var confirmationMessage = 'You have unsaved changes. '
                + 'If you leave before saving, your changes will be lost.';

            if (!formulaFormInstance.state.formulaChanged)
                return null;

            get(e, window.event).returnValue = confirmationMessage; //Gecko + IE
            return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
        });

        this.init();
    }

    init() {
        Network.get(this.props.dataUrl).promise.then(data => {
            console.log(JSON.stringify(data));
            if (data == null)
                this.setState({
                    formulaName: "",
                    formulaList: [],
                    formulaLayout: {},
                    formulaValues: {},
                    formulaChanged: false
                });
            else {
                var layout = preprocessLayout(data.layout);
                var values = generateValues(layout, data.group_data, get(data.system_data, {}), this.props.currentScope);
                this.setState({
                    formulaName: data.formula_name,
                    formulaList: data.formula_list,
                    formulaLayout: layout,
                    formulaValues: values,
                    formulaChanged: false
                });
            }
        });
    }

    saveFormula(event) {
        event.preventDefault(); // prevent default form redirect
        console.log(JSON.stringify(this.extractValues()));
        this.props.saveFormula(this);
        this.state.formulaChanged = false;
        window.scrollTo(0, 0);
    }
    
    extractValues(values=this.state.formulaValues, layout=this.state.formulaLayout) {
        var result = {};
        for (var key in values) {
            var value = values[key];
            var element = layout[key];
            if (element.$type == "group" || element.$type == "hidden-group" || element.$type == "edit-group") {
                value = this.extractValues(value, element);
                if (!jQuery.isEmptyObject(value))
                    result[key] = value;
            }
            else if ((element.$scope == this.props.currentScope || element.$scope == "system") && !(value && value.length == 0)) {
                result[key] = value;
            }
        }
        return result;
    }
    
    handleChange(event) {
        var values = this.state.formulaValues;
        if (event.target.type == "checkbox")
            assignValueById(values, event.target.id, event.target.checked);
        else if (event.target.type == "number")
            assignValueById(values, event.target.id, (isNaN(event.target.valueAsNumber) ? "" : event.target.valueAsNumber));
        else
            assignValueById(values, event.target.id, event.target.value);
        this.setState({
            formulaValues: values,
            formulaChanged: true
        });
    }

    clearValues() {
        if (confirm("Are you sure you want to clear all values?")) {
            if (formulaFormInstance.props.currentScope == "system") {
                Network.get(formulaFormInstance.props.dataUrl).promise.then(data => {
                    formulaFormInstance.setState({
                        formulaValues: generateValues(formulaFormInstance.state.formulaLayout, get((data == null ? undefined : data.group_data), {}), {}, formulaFormInstance.props.currentScope),
                        formulaChanged: true
                    });
                });
            }
            else {
                formulaFormInstance.setState({
                    formulaValues: generateValues(formulaFormInstance.state.formulaLayout, {}, {}, formulaFormInstance.props.currentScope),
                    formulaChanged: true
                });
            }
        }
    }

    handleGeneratePassword(event) {
        event.preventDefault();

        var target = $(event.target);
        if (target.attr("class").startsWith("fa"))
            target = target.parent();
        target = document.getElementById(target.parent().siblings("input").attr("id"));
        if (target.disabled)
            return;
        target.value = generatePassword();
        this.handleChange({"target": target});
    }

    handleTogglePasswordVisibility(event) {
        event.preventDefault();

        var target = $(event.target);
        if (target.attr("class").startsWith("fa"))
            target = target.parent();
        var pwd_input = document.getElementById(target.parent().siblings("input").attr("id"));
        pwd_input.type = pwd_input.type == "password" ? "text" : "password";
        target.children("i.fa").toggleClass("fa-eye");
        target.children("i.fa").toggleClass("fa-eye-slash");
    }
    
    resetValue(id, reset_value) {
        assignValueById(this.state.formulaValues, id, reset_value);
        this.forceUpdate();
    }

    generateForm() {
        var form = [];
        var layout = this.state.formulaLayout;
        var values = this.state.formulaValues;
        for (var key in layout)
            form.push(this.generateFormItem(layout[key], values[key], ""));
        return form;
    }
    
    getValueById(id) {
        var parents = id.split("$");
        var value = this.state.formulaValues;
        for (var i in parents) {
            if (value[parents[i]] == undefined)
                return null;
            value = value[parents[i]];
        }
        return value;
    }

    checkVisibleCondition(condition) {
        condition = condition.replace(/\s+/g, '');
        if (condition.includes("!=")) {
            condition = condition.split("!=");
            if (String(this.getValueById(condition[0])) != condition[1])
                return true;
        }
        else if (condition.includes("==")) {
            condition = condition.split("==");
            if (String(this.getValueById(condition[0])) == condition[1])
                return true;
        }
        return false;
    }

    generateFormItem(element, value, parents) {
        var id = (parents == "" ? "" : parents + "$") + element.$id;
        var isDisabled = (this.props.currentScope != element.$scope && element.$scope != "system");
        if ("$visibleIf" in element && !this.checkVisibleCondition(element.$visibleIf))
                return null;

        if (basicInputTypes.indexOf(element.$type) >= 0) //Element is a basic html input type
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type={element.$type} name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
                </div>
            );
        else if (element.$type == "password")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <div className="input-group">
                        <input type="password" name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
                        <span className="input-group-btn">
                            <button className="btn btn-default" title="Generate new password" onClick={this.handleGeneratePassword}>
                                <i className="fa fa-key no-margin" />
                            </button>
                            <button className="btn btn-default" title="Show/hide password" onClick={this.handleTogglePasswordVisibility}>
                                <i className="fa fa-eye no-margin" />
                            </button>
                        </span>
                    </div>
                </div>
            );
        else if (element.$type == "color")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <div className="input-group small-color-picker">
                        <input type="color" name={element.$name} id={id} className="form-control" onChange={this.handleChange} title={element.$help} disabled={isDisabled} value={value} />
                        <span className="input-group-btn">
                            <button className="btn btn-default" title="Reset" onClick={function (event) {event.preventDefault(); formulaFormInstance.resetValue(id, element.$default)}}>
                                <i className="fa fa-undo no-margin" />
                            </button>
                        </span>
                    </div>
                </div>
            );
        else if (element.$type == "datetime")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="datetime-local" name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
                </div>
            );
        else if (element.$type == "number")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="number" steps="1" max={get(element.$max, "")} min={get(element.$min, "")} name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
                </div>
            );
        else if (element.$type == "group") {
            return (
            <div className="panel panel-default" id={id}>
                <div className="panel-heading">
                    <h4>{element.$name}</h4>
                </div>
                <div className="panel-body">
                    {this.generateChildrenFormItems(element, value, id)}
                </div>
            </div>
            );
        }
        else if (element.$type == "hidden-group")
            return this.generateChildrenFormItems(element, value, id);
        else if (element.$type == "edit-group")
            return (
            <div className="panel panel-default" id={id}>
                <div className="panel-heading">
                    <h4>{element.$name}</h4>
                </div>
                <div className="panel-body">
                    <div id={id + "$elements"}>
                        {this.generateEditGroup(element, value, id)}
                    </div>
                    <Button id={id + "$plus-button"} icon="fa-plus" text="Add Element" className="btn-default btn-3d btn-sm" />
                </div>
            </div>
            );
        else if (element.$type == "select")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <select className="form-control" name={element.$name} id={id} onChange={this.handleChange} title={element.$help} disabled={isDisabled} value={value}>
                        {generateSelectList(element.$values)}
                    </select>
                </div>
            );
        else if (element.$type == "boolean")
            return wrapFormGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="checkbox" className="big-checkbox" onChange={this.handleChange} name={element.$name} id={id} title={element.$help} disabled={isDisabled} checked={value} />
                </div>
            );
        else {
            console.error("Unknown $type: " + element.$type);
            return wrapFormGroupWithLabel(get(element.$name, "Element not found"),
                <div className="col-lg-6" id={id}>
                    { JSON.stringify(value) }
                </div>
            );
        }
    }

    generateChildrenFormItems(element, value, id) {
        var child_items = [];
        for (var child_name in element) {
            if (child_name.startsWith("$")) continue;
            child_items.push(this.generateFormItem(element[child_name], value[child_name], id));
        }
        return child_items;
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
        if (this.state.errors.length > 0) {
            errors = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }

        if (this.state.formulaLayout == undefined || this.state.formulaLayout == null || jQuery.isEmptyObject(this.state.formulaLayout)) {
            this.props.addFormulaNavBar(get(this.state.formulaList, ["Not found"]), formulaId);
            return (
                <div>
                    {errors}{messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Error while loading form!</h4>
                        </div>
                        <div className="panel-body">
                            The requested form could not get loaded! The corresponding formula either doesn not exist or has no valid layout file.
                        </div>
                    </div>
                </div>
            );
        }
        else {
            this.props.addFormulaNavBar(this.state.formulaList, formulaId);
            return (
                <div>
                    {errors}{messages}
                    <form id="editFormulaForm" className="form-horizontal" onSubmit={this.saveFormula}>
                        <div className="panel panel-default">
                            <div className="panel-heading">
                                <h4>{toTitle(get(this.state.formulaName, "Formula not found"))}</h4>
                            </div>
                            <div className="panel-body">
                                {this.generateForm()}
                                <div className="row">
                                    <div className="col-lg-6 col-lg-offset-3">
                                        <div className="btn-group">
                                            <Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className={"btn btn-success"} handler={function(e){$('<input type="submit">').hide().appendTo($("#editFormulaForm")).click().remove();}} />
                                            <Button id="reset-btn" icon="fa-eraser" text="Clear values" className="btn btn-default" handler={this.clearValues} />
                                        </div>
                                        <div className="btn-group pull-right">
                                            <a id="prev-btn" href={this.props.getFormulaUrl(this.props.formulaId - 1)} disabled={this.props.formulaId == 0} className="btn btn-default"><i className="fa fa-arrow-left" /> Prev</a>
                                            <a id="next-btn" href={this.props.getFormulaUrl(this.props.formulaId + 1)} disabled={this.state.formulaList.length - 1 <= this.props.formulaId} className="btn btn-default">Next <i className="fa fa-arrow-right" /></a>
                                        </div>
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

function generateSelectList(data) {
    var options = []
    for (var key in data)
        options.push(<option value={data[key]}>{data[key]}</option>);
    return options;
}

function preprocessLayout(layout, scope="system") {
    for (var child_name in layout) {
        if (child_name.startsWith("$")) continue;

        var child = layout[child_name];
        if (!("$scope" in child))
            child.$scope = scope;

        if (!("$type" in child)) {
            child.$type = "text"
            child.$default = get(child.$default, "");
        }
        else if (child.$type == "boolean")
            child.$default = get(child.$default, false);
        else if (child.$type == "select")
            child.$default = get(child.$default, child.$values[0]);
        else if (child.$type == "password")
            child.$default = get(child.$default, generatePassword());
        else if (child.$type == "group" || child.$type == "hidden-group")
            child = preprocessLayout(child, child.$scope);
        else if (child.$type == "edit-group")
            child.$element = preprocessLayout(child.$element, child.$scope);
        else
            child.$default = get(child.$default, "");

        child.$id = child_name;
        if (child.$name == undefined) child.$name = toTitle(child_name);
        if (child.$help == undefined) child.$help = child.$name;
        if (child.$placeholder == undefined) child.$placeholder = "";
    }
    return layout;
}

function generateValues(layout, group_data, system_data) {
    var result = {};

    for (var key in layout) {
        if (key.startsWith("$")) continue;

        var value = null
        var element = layout[key];

        if (element.$type == "group" || element.$type == "hidden-group")
            value = generateValues(element, get(group_data[key], {}), get(system_data[key], {}), element.$scope);
        else if (element.$scope == "system")
            value = get(system_data[key], get(group_data[key], element.$default));
        else if (element.$scope == "group")
            value = get(group_data[key], element.$default);
        else if (element.$scope == "readonly")
            value = element.$default;

        if (value != null)
            result[key] = value
    }
    return result;
}

function get(value, def) {
    if (value == undefined)
        return def;
    return value;
}

function wrapFormGroupWithLabel(element_name, innerHTML) {
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

// Replace all "_" and "-" with spaces and capitalize the first letter of each word
function toTitle(str) {
    return str.replace(new RegExp("_|-", 'g'), " ").replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

function generatePassword() {
    var length = Math.floor(Math.random() * 10) + 15,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i)
        retVal += charset.charAt(Math.floor(Math.random() * n));
    return retVal;
}

function assignValueById(dir, id, value) {
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
