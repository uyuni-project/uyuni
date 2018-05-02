const React = require("react");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Buttons = require("../components/buttons");
const Messages = require("../components/messages").Messages;
const generateFormulaComponent = require("./formulas/FormulaComponentGenerator").generateFormulaComponent;

const Button = Buttons.Button;
const capitalize = Functions.Utils.capitalize;

//props:
//dataUrl = url to get the server data
//saveUrl = url to save the data, data is sent as post request
//addFormulaNavBar = function(formulaList, activeFormulaId) to add the formula nav bar
//formulaId = the id of the formula to be shown
//getFormulaUrl = function(formulaId) that returns the url to a formula page by id (used for prev/next buttons)
//scope = current active scope (system or group)
class FormulaForm extends React.Component {
    constructor(props) {
        super(props);

        ["saveFormula", "handleChange", "clearValues"].forEach(method => this[method] = this[method].bind(this));

        const previewMessage = <p><strong>{t('This is a feature preview')}</strong>: On this page you can configure <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank" rel="noopener noreferrer">Salt formulas</a> to automatically install and configure software. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank" rel="noopener noreferrer">forum</a>.</p>;

        this.state = {
            formulaName: "",
            formulaList: [],
            formulaLayout: {},
            formulaValues: {},
            formulaChanged: false,
            messages: [previewMessage],
            errors: []
        };

        window.addEventListener("beforeunload", function (e) {
            if (!this.state.formulaChanged)
                return null;

            let confirmationMessage = 'You have unsaved changes. '
                + 'If you leave before saving, your changes will be lost.';

            get(e, window.event).returnValue = confirmationMessage; //Gecko + IE
            return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
        }.bind(this));

        this.init();
    }

    init() {
        Network.get(this.props.dataUrl).promise.then(data => {
            if (data === null)
                this.setState({
                    formulaName: "",
                    formulaList: [],
                    formulaLayout: {},
                    formulaValues: {},
                    formulaChanged: false
                });
            else {
                const layout = preprocessLayout(data.layout);
                const values = generateValues(layout, get(data.group_data, {}), get(data.system_data, {}), this.props.scope);
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
        event.preventDefault();
        this.setState({ formulaChanged: false });
        let scope = this.props.scope;
        let formType = scope.toUpperCase();
        if (formType === 'SYSTEM') {
            formType = 'SERVER';
        }

        let formData = {
            type: formType,
            id: this.props.systemId,
            formula_name: this.state.formulaName,
            content: this.getValuesClean()
        };
        console.log(formData);

        Network.post(
            this.props.saveUrl,
            JSON.stringify(formData),
            "application/json"
        ).promise.then(function (data) { if (data instanceof Array) this.setState({ messages: data }); }.bind(this),
            function (error) {
                try {
                    this.setState({
                        errors: [JSON.parse(error.responseText)]
                    });
                } catch (e) {
                    this.setState({
                        errors: [Network.errorMessageByStatus(error.status)]
                    });
                }
            }.bind(this));
        window.scrollTo(0, 0);
    }

    getValuesClean(values = this.state.formulaValues, layout = this.state.formulaLayout) {
        let result = {};
        for (let key in values) {
            let value = values[key];
            let element = layout[key];
            if (element.$type === "group" || element.$type === "namespace") {
                value = this.getValuesClean(value, element);
                if (!$.isEmptyObject(value))
                    result[key] = value;
            }
            else if ((element.$scope === this.props.scope || element.$scope === "system") && !(value && value.length === 0)) {
                value = this.checkIfEmptyValueAttrExists(value, element, values)
                result[key] = value;
            }
        }
        return result;
    }

    checkIfEmptyValueAttrExists(value, element, formulaValues) {
         if((value == null || value.length == 0) && !element.$optional && (element.$ifEmpty || element.$ifEmpty === null)) {
            value = element.$ifEmpty;
            formulaValues[element.$id] = value;
         }
         return value;
    }

    clearValues() {
        if (window.confirm("Are you sure you want to clear all values?")) {
            if (this.props.scope === "system") {
                Network.get(this.props.dataUrl).promise.then(data => {
                    this.setState({
                        formulaValues: generateValues(this.state.formulaLayout, get((data === null ? undefined : data.group_data), {}), {}, this.props.scope),
                        formulaChanged: false
                    });
                });
            }
            else {
                this.setState({
                    formulaValues: generateValues(this.state.formulaLayout, {}, {}, this.props.scope),
                    formulaChanged: false
                });
            }
        }
    }

    handleChange(event) {
        let id, value;
        if (event.id) {
            id = event.id;
            value = event.value;
        }
        else {
            id = event.target.id;

            if (event.target.type === "checkbox")
                value = event.target.checked;
            else if (event.target.type === "number")
                value = isNaN(event.target.valueAsNumber) ? "" : event.target.valueAsNumber;
            else
                value = event.target.value;
        }

        this.setState((state) => {
            let values = state.formulaValues;
            let parents = id.split("$");
            for (var i in parents.slice(0, -1)) {
                if (values[parents[i]] === undefined)
                    values[parents[i]] = {};
                values = values[parents[i]];
            }
            values[parents[parents.length - 1]] = value;
            return {
                formulaValues: state.formulaValues,
                formulaChanged: true
            }
        });
    }

    getValueById(id) {
        let parents = id.split("$");
        let value = this.state.formulaValues;
        for (let i in parents) {
            if (value[parents[i]] === undefined)
                return null;
            value = value[parents[i]];
        }
        return value;
    }

    renderForm() {
        let form = [];
        const layout = this.state.formulaLayout;
        const values = this.state.formulaValues;
        for (let key in layout)
            form.push(generateFormulaComponent(layout[key], values[key], this));
        return form;
    }

    render() {
        let messageItems = this.state.messages.map((msg) => {
            return { severity: "info", text: msg };
        });
        messageItems.concat(this.state.errors.map((msg) => {
            return { severity: "error", text: msg };
        }));
        const messages = <Messages items={messageItems} />;

        if (this.state.formulaLayout === undefined || this.state.formulaLayout === null || $.isEmptyObject(this.state.formulaLayout)) {
            if (this.props.addFormulaNavBar !== undefined)
                this.props.addFormulaNavBar(get(this.state.formulaList, ["Not found"]), this.props.formulaId);
            return (
                <div>
                    {messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Error while loading form!</h4>
                        </div>
                        <div className="panel-body">
                            The requested form could not get loaded! The corresponding formula either does not exist or has no valid layout file.
                        </div>
                    </div>
                </div>
            );
        }
        else {
            if (this.props.addFormulaNavBar !== undefined)
                this.props.addFormulaNavBar(this.state.formulaList, this.props.formulaId);
            const nextHref = this.props.getFormulaUrl(this.props.formulaId + 1);
            const prevHref = this.props.getFormulaUrl(this.props.formulaId - 1);
            return (
                <div>
                    {messages}
                    <form id="formula-form" className="form-horizontal" onSubmit={this.saveFormula}>
                        <div className="spacewalk-section-toolbar">
                            <div className="btn-group">
                                <button id="prev-btn" type="button" onClick={() => window.location.href = prevHref} disabled={this.props.formulaId === 0} className="btn btn-default"><i className="fa fa-arrow-left" /> Prev</button>
                                <button id="next-btn" type="button" onClick={() => window.location.href = nextHref} disabled={this.props.formulaId >= this.state.formulaList.length - 1} className="btn btn-default">Next <i className="fa fa-arrow-right fa-right" /></button>
                            </div>
                            <div className="action-button-wrapper">
                                <div className="btn-group">
                                    <Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className={"btn btn-success"} handler={() => $('<input type="submit">').hide().appendTo($("#formula-form")).click().remove()} />
                                    <Button id="reset-btn" icon="fa-eraser" text="Clear values" className="btn btn-default" handler={this.clearValues} />
                                </div>
                            </div>
                        </div>
                        <div className="panel panel-default">
                            <div className="panel-heading">
                                <h4>{capitalize(get(this.state.formulaName, "Unnamed"))}</h4>
                            </div>
                            <div className="panel-body">
                                {this.renderForm()}
                            </div>
                        </div>
                    </form>
                </div>
            );
        }
    }
}


function preprocessLayout(layout, scope = "system") {
    for (let child_name in layout) {
        if (child_name.startsWith("$")) continue;

        let child = layout[child_name];
        if (!("$scope" in child))
            child.$scope = scope;

        if (child.$type === "hidden-group")
            child.$type = "namespace";

        if (!("$type" in child)) {
            child.$type = "text"
            child.$default = get(child.$default, "");
        }
        else if (child.$type === "boolean")
            child.$default = get(child.$default, false);
        else if (child.$type === "select")
            child.$default = get(child.$default, child.$values[0]);
        else if (child.$type === "color")
            child.$default = get(child.$default, "#000000");
        else if (child.$type === "password")
            child.$default = get(child.$default, "");
        else if (child.$type === "group" || child.$type === "namespace")
            child = preprocessLayout(child, child.$scope);
        else if (child.$type === "edit-group") {
            child.$prototype = preprocessLayout(child.$prototype, child.$scope);
            child.$prototype.$type = "group";
            if (child.$itemName === undefined) child.$itemName = "Item ${i}";
            child.$newItemValue = generateValues(child.$prototype, {}, {});
            child.$default = get(child.$default, []);
            while (child.$default.length < child.$minItems) {
                child.$default.push(Object.assign({}, child.$newItemValue));
            }
        }
        else
            child.$default = get(child.$default, "");

        child.$id = child_name;
        if (child.$name === undefined) child.$name = capitalize(child_name);
        if (child.$help === undefined) child.$help = child.$name;
        if (child.$placeholder === undefined) child.$placeholder = "";
    }
    return layout;
}

function generateValues(layout, group_data, system_data) {
    let result = {};

    for (let key in layout) {
        if (key.startsWith("$")) continue;

        let value = null;
        let element = layout[key];

        if (element.$type === "group" || element.$type === "namespace") {
            value = generateValues(element, get(group_data[key], {}), get(system_data[key], {}), element.$scope);
        } else if (element.$scope === "system") {
            value = get(system_data[key], get(group_data[key], element.$default));
        } else if (element.$scope === "group") {
            value = get(group_data[key], element.$default);
        } else if (element.$scope === "readonly") {
            value = element.$default;
        }

        if (value === null) {
          value = "";
        }
        
        result[key] = value
    }
    return result;
}

function get(value, def) {
    if (value === undefined)
        return def;
    return value;
}


module.exports = {
    FormulaForm: FormulaForm
}
