/* eslint-disable */
const React = require("react");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Buttons = require("../components/buttons");
const Messages = require("../components/messages").Messages;
const generateFormulaComponent = require("./formulas/FormulaComponentGenerator").generateFormulaComponent;

const Button = Buttons.Button;
const getEditGroupSubtype = Functions.Formulas.getEditGroupSubtype;
const EditGroupSubtype = Functions.Formulas.EditGroupSubtype;
const deepCopy = Functions.Utils.deepCopy;
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

        ["saveFormula", "handleChange", "clearValues", "getMessageText"].forEach(method => this[method] = this[method].bind(this));

        const previewMessage = <p>On this page you can configure <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank" rel="noopener noreferrer">Salt Formulas</a> to automatically install and configure software.</p>;

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
                preprocessData(layout, get(data.system_data, {}));
                preprocessData(layout, get(data.group_data, {}));
                const values = generateValues(layout, get(data.group_data, {}), get(data.system_data, {}));

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
            content: this.getValuesClean(preprocessCleanValues(this.state.formulaValues, this.state.formulaLayout))
        };

        Network.post(
            this.props.saveUrl,
            JSON.stringify(formData),
            "application/json"
        ).promise.then(function (data) { if (data instanceof Array) this.setState({ messages: data.map(msg => this.getMessageText(msg)) }); }.bind(this),
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
            else if ((element.$scope === this.props.currentScope || element.$scope === "system") && !(value && value.length === 0)) {
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
                        formulaValues: generateValues(this.state.formulaLayout, get((data === null ? undefined : data.group_data), {}), {}),
                        formulaChanged: false
                    });
                });
            }
            else {
                this.setState({
                    formulaValues: generateValues(this.state.formulaLayout, {}, {}),
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
            let parents = id.split("#");
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
        let parents = id.split("#");
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

    getMessageText(msg) {
      return this.props.messageTexts[msg] ? t(this.props.messageTexts[msg]) : msg;
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

/*
 * Set missing attributes ($type, $scope) to default values,
 * set $default values,
 * set $newItemValues for edit-groups.
 */
function preprocessLayout(layout, scope = "system") {
    Object.entries(layout)
        .filter(([name, element]) => !name.startsWith("$") || name === "$key")
        .forEach(([name, element]) => {
            adjustElementBasicAttrs([name, element], scope);

            // groups are processed recursively
            if (element.$type === "group" || element.$type === "namespace") {
                preprocessLayout(element, element.$scope)
            }
            // edit-groups too
            else if (element.$type === "edit-group") {
                element.$prototype = preprocessLayout(element.$prototype, element.$scope);
                adjustEditGroup(element);
            }
        });

    return layout;
}

// consumes an element entry: [element name, element]
function adjustElementBasicAttrs([elementName, element], scope) {
    if (!("$scope" in element)) {
        element.$scope = scope;
    }

    if (element.$type === "hidden-group") {
        element.$type = "namespace";
    }

    if (!("$type" in element)) {
        element.$type = "text"
    }

    if (element.$name === undefined && elementName === "$key") {
        element.$name = "Key";
    }

    element.$id = elementName;
    element.$name = get(element.$name, capitalize(elementName));
    element.$help = get(element.$help, element.$name);
    element.$placeholder = get(element.$placeholder, "");

    if (isPrimitiveElement(element)) {
        element.$default = defaultValueForElement(element);
    };
}

function defaultValue(type, defValue, selectValues) {
    if (type === "boolean")
        return get(defValue, false);
    else if (type === "select")
        return get(defValue, selectValues[0]);
    else if (type === "color")
        return get(defValue, "#000000");
    else if (type === "password")
        return get(defValue, "");

    return get(defValue, "");
}

function defaultValueForElement(element) {
    return defaultValue(element.$type, element.$default, element.$values);
}

function isPrimitiveElement(element) {
    return element.$type !== "group" &&
        element.$type !== "namespace" &&
        element.$type !== "edit-group";
}

/*
 * Adjust edit group default and 'new item' value of edit-group
 * Some subtypes of edit-group need special handling (e.g. nested dictionary
 * needs to be converted to lists).
 */
function adjustEditGroup(element) {
    // Adjust common edit-group attributes
    if (element.$prototype.$type === undefined) {
        element.$prototype.$type = "group";
    }
    else if (element.$prototype.$scope === undefined) {
        element.$prototype.$scope = "system";
    }

    if (element.$itemName === undefined) {
        element.$itemName = "Item ${i}";
    }

    // Adjust defaults && new values
    element.$default = adjustNestedDefault(element, element.$default);
    const editGroupSubType = getEditGroupSubtype(element);
    if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES
            || editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES)
    {
        element.$newItemValue = generateValues(element.$prototype, {}, {});
    }
    else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
        element.$newItemValue = [
            defaultValue(element.$prototype.$key.$type, element.$prototype.$key.$default),
            defaultValue(element.$prototype.$type, element.$prototype.$default)
        ];
    }
    else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_LIST) {
        element.$newItemValue = defaultValue(element.$prototype.$type, element.$prototype.$default);
    }

    // if we don't have minimum number of items, let's create some
    // todo handle the edit groups where their key must be unique
    element.$default = get(element.$default, [])
    while (element.$default.length < element.$minItems) {
        element.$default.push(deepCopy(element.$newItemValue));
    }
}

/*
 * This needs to be recursive as the element.$default can be a complex nested
 * structure of edit-groups/groups which we need to adjust.
 */
function adjustNestedDefault(element, defVal) {
    // helper function for setting values from the prototype
    const setMissingValues = (value) => {
        Object.keys(element.$prototype)
            .filter(att => !att.startsWith("$") && !Object.keys(value).includes(att))
            .forEach(att => value[att] = element.$prototype[att].$default)
    }

    const recurOnVals = (value) => {
        return Object.entries(value)
            .map(([nestedName, nestedVal]) => [nestedName, adjustNestedDefault(element.$prototype[nestedName], nestedVal)])
            .reduce((acc, entry) => {
                acc[entry[0]] = entry[1];
                return acc;
            }, {})
    };

    const editGroupSubType = getEditGroupSubtype(element);
    if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
        // normalize the default (convert from an object to array)
        return Object.entries(defVal || {})
            .map(([name, value]) => {
                value = recurOnVals(value);
                value['$key'] = name;
                setMissingValues(value);
                return value;
            });
    }
    else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
        return (defVal || [])
            .map(defEntry => {
                defEntry = recurOnVals(defEntry);
                setMissingValues(defEntry);
                return defEntry;
            });
    }
    else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
        return Object.entries(defVal || {});
    }

    return defVal;
}

/*
 * Goes through the layout and adjusts data for certain structures
 * (edit-group in a form of nested dictionary needs to be converted to lists, etc.)
 */
function preprocessData(layout, data) {
    Object.entries(layout)
        .filter(([name, element]) => !(data[name] === undefined || name.startsWith("$") && name !== "$key"))
        .forEach(([name, element]) => {
            const editGroupSubType = getEditGroupSubtype(element);
            // other edit-group as-a-dictionary needs to be converted to an array
            if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
                data[name] = Object.entries(data[name] || {})
                    .map(e => {
                        e[1]['$key'] = e[0];
                        preprocessData(element.$prototype, e[1]);
                        return e[1];
                    });
            }
            // edit-group as-a-primitive-dictionary too
            else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
                data[name] = Object.entries(data[name] || {});
            }
            // the last form of a recursive edit-group needs to be processed recursively
            else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
                Object.entries(data[name] || {})
                    .forEach(([name, value]) => preprocessData(element.$prototype, value));
            }
            // group elements also must be processed as they can contain edit-groups
            else if (!isPrimitiveElement(element)) {
                preprocessData(element, data[name]);
            }
        });
}

/*
 * Traverses the form values and adjusts certain elements.
 * (e.g. converts arrays to dictionaries in case of edit-group as a dictionary)
 */
function preprocessCleanValues(values, layout) {
    const result = {};
    Object.entries(values)
        .forEach(([key, value]) => {
            const element = layout[key];
            const editGroupSubType = getEditGroupSubtype(element);

            if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
                result[key] = value
                    .map(entry => preprocessCleanValues(entry, element.$prototype))
                    .filter(entry => entry["$key"] !== '')
                    .reduce((acc, entry) => {
                        acc[entry["$key"]] = entry;
                        delete entry["$key"];
                        return acc;
                    }, {});
            } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
                result[key] = value
                    .filter(entry => entry[0] !== '')
                    .reduce((acc, entry) => {
                        acc[entry[0]] = entry[1];
                        return acc;
                    }, {});
            } else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
                result[key] = value
                    .map(entry => preprocessCleanValues(entry, element.$prototype));
            } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_LIST) {
                result[key] = value.filter(entry => entry !== '');
            // we need to recur to groups as they can contain edit-groups that need an adjustment
            } else if (element !== undefined && (element.$type === "group" || element.$type === "namespace")) {
                result[key] = preprocessCleanValues(value, element);
            } else {
                result[key] = value;
            }
        });
    return result;
}

/*
 * For each key in the layout, generate and return a defensive copy of an
 * object that follows the structure of the layout and has values populated
 * based on the system data, group data and layout default.
 */
function generateValues(layout, group_data, system_data) {
    const generateValuesInternal = (layout, group_data, system_data) => {
        let result = {};
        for (let key in layout) {
            if (key.startsWith("$") && key !== "$key") continue;

            let value = null;
            let element = layout[key];

            if (element.$type === "group" || element.$type === "namespace") {
                value = generateValuesInternal(element, get(group_data[key], {}), get(system_data[key], {}));
            } else if (element.$scope === "system") {
                value = get(system_data[key], get(group_data[key], element.$default));
            } else if (element.$scope === "group") {
                value = get(group_data[key], element.$default);
            } else if (element.$scope === "readonly") {
                value = element.$default;
            }

            if (element.$type === "edit-group") {
                if (!Array.isArray(value)) {
                    // array is expected here
                    // we can get other types if the saved data are incomplete or outdated
                    value = [];
                }
                const editGroupSubType = getEditGroupSubtype(element);
                if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES ||
                    editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
                    // do not do merging of edit-group values,
                    // take either system or group value based on the logic above
                    // and process it recursively (hack: pass it always as "system" scope)
                    value = value.map(entry => generateValuesInternal(element.$prototype, entry, {}));
                }
            }

            if (value === null) {
                value = "";
            }

            result[key] = value
        }
        return result;
    }

    return deepCopy(generateValuesInternal(layout, group_data, system_data));
}

function get(value, def) {
    if (value === undefined)
        return def;
    return value;
}


module.exports = {
    FormulaForm: FormulaForm
}
