"use strict";

const React = require("react");
const Buttons = require("../components/buttons");
const Network = require("../utils/network");
const Messages = require("../components/messages").Messages;

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;

const basicInputTypes = ["text", "email", "url", "date", "time"];

//props:
//dataUrl = url to get the server data
//notFoundText = text to display if no formula is found
//addFormulaNavBar = function(formula_list, activeId) to add the formula nav bar
//formulaId = the id of the formula to be shown
//getFormulaUrl = function(formulaId) that returns the url to a formula page
//saveFormula = function(component) that gets called when the save button is pressed
//currentScope = current active scope (system or group)
class FormulaForm extends React.Component {
    constructor(props) {
        super(props);

        ["init", "saveFormula", "generateForm", "generateFormItem", "generateChildrenFormItems",
        "getValueById", "handleChange", "handleGeneratePassword", "handleTogglePasswordVisibility"]
        .forEach(method => this[method] = this[method].bind(this));

        this.state = {
            formulaFound: false,
            formulaName: "",
            formulaList: [],
            formulaLayout: {},
            formulaValues: {},
            values: {},
            messages: [],
            errors: []
        };
        this.init();
    }

    init() {
        Network.get(this.props.dataUrl).promise.then(data => {
            console.log(data);
            if (data == null)
                this.setState({
                    formulaFound: false,
                    formulaName: "",
                    formulaList: [],
                    formulaLayout: {},
                    formulaValues: {},
                    values: {}
                });
            else {
                var layout = preprocessLayout(data.layout);
                var values = generateValues(layout, data.group_data, data.system_data || {}, this.props.currentScope);
                this.setState({
                    formulaFound: true,
                    formulaName: data.formula_name,
                    formulaList: data.formula_list,
                    formulaLayout: layout,
                    formulaValues: values
                });
            }
        });
    }

    saveFormula(event) {
        event.preventDefault(); // prevent default form redirect
        console.log(JSON.stringify(this.extractValues()));
        this.props.saveFormula(this);
        window.scrollTo(0, 0);
    }
    
    // don't extract values that:
    // - are out of scope
    // - are on default
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
            else if ((element.$scope == this.props.currentScope || element.$scope == "system") && value != element.$default) {
                result[key] = value;
            }
        }
        return result;
    }
    
    handleChange(event) {
        var values = this.state.formulaValues;
        if (event.target.type == "checkbox")
            assignValueWithId(values, event.target.id, event.target.checked);
        else
            assignValueWithId(values, event.target.id, event.target.value);
        this.setState({
            formulaValues: values
        });
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

    generateFormItem(element, value, parents) {
        var id = (parents == "" ? "" : parents + "$") + element.$id;
        var isDisabled = (this.props.currentScope != element.$scope && element.$scope != "system");
        if ("$visibleIf" in element) {
            var condition = element.$visibleIf.replace(/\s+/g, '');
            if (condition.includes("!=") {
                condition = condition.split("!=");
                if (String(this.getValueById(condition[0])) == condition[1])
                    return null;
            else if (condition.includes("==") {
                condition = condition.split("==");
                if (String(this.getValueById(condition[0])) != condition[1])
                    return null;
        }

        if (basicInputTypes.indexOf(element.$type) >= 0) //Element is a basic html input type
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type={element.$type} name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder || ""} title={element.$help} disabled={isDisabled} value={value} />
                </div>
            );
        else if (element.$type == "password")
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <div className="input-group">
                        <input type={element.$type} name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder || ""} title={element.$help} disabled={isDisabled} value={value} />
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
        else if (element.$type == "datetime")
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="datetime-local" name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder || ""} title={element.$help} disabled={isDisabled} value={value} />
                </div>
            );
        else if (element.$type == "number")
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="number" steps="1" name={element.$name} id={id} className="form-control" onChange={this.handleChange} placeholder={element.$placeholder || ""} title={element.$help} disabled={isDisabled} value={value} />
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
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <select className="form-control" name={element.$name} id={id} onChange={this.handleChange} title={element.$help} disabled={isDisabled} value={value}>
                        {generateSelectList(element.$values)}
                    </select>
                </div>
            );
        else if (element.$type == "boolean")// HACK: style should be in css file
            return wrapGroupWithLabel(element.$name,
                <div className="col-lg-6">
                    <input type="checkbox" className="big-checkbox" onChange={this.handleChange} name={element.$name} id={id} title={element.$help} disabled={isDisabled} checked={value} />
                </div>
            );
        else
            return wrapGroupWithLabel(element.$name || "Element not found",
                <div className="col-lg-6" id={id}>
                    { JSON.stringify(value) }
                </div>
            );
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

        if (!this.state.formulaFound || this.state.formulaLayout == undefined || this.state.formulaLayout == null) {
            this.props.addFormulaNavBar(this.state.formulaList || ["Not found"], formulaId);
            return (
                <div>
                    {errors}{messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Error while loading form!</h4>
                        </div>
                        <div className="panel-body">
                            The requested form could not get loaded! The corresponding formula either doesn't exist or has no valid layout file.
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
                                <h4>{toTitle(this.state.formulaName || "Formula not found")}</h4>
                            </div>
                            <div className="panel-body">
                                {this.generateForm()}
                                <div className="row">
                                    <div className="col-lg-6 col-lg-offset-3">
                                        <Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className="btn btn-success" handler={function(e){$('<input type="submit">').hide().appendTo($("#editFormulaForm")).click().remove();}} />
                                        <a id="next-btn" href={this.props.getFormulaUrl(this.props.formulaId + 1)} disabled={this.state.formulaList.length - 1 <= this.props.formulaId} className="btn btn-default pull-right">Next <i className="fa fa-arrow-right" /></a>
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
            child.$default = (child.$default || "");
        }
        else if (child.$type == "boolean")
            child.$default = (child.$default || false);
        else if (child.$type == "select")
            child.$default = (child.$default || child.$values[0]);
        else if (child.$type == "password")
            child.$default = (child.$default || generatePassword());
        else if (child.$type == "group" || child.$type == "hidden-group")
            child = preprocessLayout(child, child.$scope);
        else if (child.$type == "edit-group")
            child.$element = preprocessLayout(child.$element, child.$scope);
        else
            child.$default = (child.$default || "");

        child.$id = child_name;
        if (!child.$name) child.$name = toTitle(child_name);
        if (!child.$help) child.$help = child.$name;
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
            value = generateValues(element, group_data[key] || {}, system_data[key] || {}, element.$scope);
        else if (element.$scope == "system")
            value = (system_data[key] || group_data[key] || element.$default || null);
        else if (element.$scope == "group")
            value = (group_data[key] || element.$default || null);
        else if (element.$scope == "readonly")
            value = (element.$default || null);

        if (value != null)
            result[key] = value
    }
    return result;
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

function generatePassword() {
    var length = Math.floor(Math.random() * 10) + 15,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i)
        retVal += charset.charAt(Math.floor(Math.random() * n));
    return retVal;
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
