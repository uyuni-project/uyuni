/* eslint-disable */
const React = require("react");
const EditGroupSubtype = require("../../utils/functions").Formulas.EditGroupSubtype;
const getEditGroupSubtype = require("../../utils/functions").Formulas.getEditGroupSubtype;
const deepCopy = require("../../utils/functions").Utils.deepCopy;
// circular dependencies are bad
const generateFormulaComponent = require("./FormulaComponentGenerator").generateFormulaComponent;
const generateFormulaComponentForId = require("./FormulaComponentGenerator").generateFormulaComponentForId;

/*
 * Base class for edit-group.
 * Based on the edit-group data, the corresponing shape of component is used.
 */
class EditGroup extends React.Component {
    constructor(props) {
        super(props);

        this.handleAddItem = this.handleAddItem.bind(this);
        this.handleRemoveItem = this.handleRemoveItem.bind(this);
        this.isDisabled = this.isDisabled.bind(this);
    }

    isDisabled() {
        const formScope = this.props.formulaForm.props.scope;
        const elementScope = this.props.element.$scope;
        return elementScope === "readonly" || (formScope !== elementScope && elementScope !== "system");
    }

    handleAddItem(event) {
        if (this.props.element.$maxItems <= this.props.value.length || this.isDisabled())
            return;

        let newValueProps = this.props.value;
        let newValue = deepCopy(this.props.element.$newItemValue);

        newValueProps.push(newValue);

        this.props.formulaForm.handleChange({
            id: this.props.id,
            value: newValueProps
        });
    }

    handleRemoveItem(index) {
        if (this.props.element.$minItems >= this.props.value.length || this.isDisabled())
            return;

        this.props.value.splice(index, 1);
        this.props.formulaForm.handleChange({
            id: this.props.id,
            value: this.props.value
        });
    }

    render() {
        const element = this.props.element;
        const subType = getEditGroupSubtype(element);

        let Component;
        if (subType === EditGroupSubtype.PRIMITIVE_LIST) {
            Component = EditPrimitiveGroup;
        } else if (subType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
            Component = EditPrimitiveDictionaryGroup;
        } else {
            Component = EditDictionaryGroup;
        }

        return (
            <div className="panel panel-default" id={this.props.id}>
                <div className="panel-heading">
                    <h4>{this.props.element.$name}</h4>
                </div>
                <div className="panel-body">
                    <Component
                        handleRemoveItem={this.handleRemoveItem}
                        isDisabled={this.isDisabled}
                        id={this.props.id}
                        key={this.props.key}
                        element={this.props.element}
                        value={this.props.value}
                        formulaForm={this.props.formulaForm}/>
                    <button className="btn btn-default"
                        type="button"
                        id={this.props.id + '#add_item'}
                        title={this.props.element.$maxItems <= this.props.value.length ? "Max number of items reached" : "Add Element"}
                        onClick={this.handleAddItem}
                        disabled={this.props.element.$maxItems <= this.props.value.length}>
                        <i className="fa fa-plus" /> Add Item
                    </button>
                </div>
            </div>);
    }
}


/*
 * Used for rendering edit-groups in the form of "list of primitive types",
 * to be rendered as a list of simple form elements in the UI.
 */
class EditPrimitiveGroup extends React.Component {

    constructor(props) {
        super(props);
    };

    simpleWrapper(name, element) {
        return (
            <div className="col-lg-3">
                {element}
            </div>);
    }

    render() {
        let elements = [];
        for (let i in this.props.value) {
            let id = this.props.id + "#" + i;
            elements.push(
                <div className="form-group" id={id} key={id}>
                    {generateFormulaComponentForId(
                        this.props.element.$prototype,
                        this.props.value[i],
                        this.props.formulaForm,
                        id,
                        this.simpleWrapper)}
                    <RemoveButton
                        minItems={this.props.element.$minItems}
                        currentLength={this.props.value.length}
                        handleRemoveItem={() => this.props.handleRemoveItem(i)} />
                </div>
            );
        }
        return (<div id={this.props.id + "$elements"}>{elements}</div>);
    }
}


/*
 * Used for rendering edit-groups in the form of "dictionary of primitive types",
 * to be rendered as a list of [key, value] in the UI.
 */
class EditPrimitiveDictionaryGroup extends React.Component {

    constructor(props) {
        super(props);
    };

    pairElementWrapper(elementName) {
        return (name, element) => (
            <div key={elementName}>
                <label className="col-lg-1 control-label">
                    {elementName + ":"}
                </label>
                <div className="col-lg-3">
                    {element}
                </div>
            </div>);
    }

    render() {
        let elements = [];

        for(let i in this.props.value) {
            let id = this.props.id + "#" + i;
            elements.push(
                <div className="form-group" id={id} key={id}>
                    {generateFormulaComponentForId(
                        this.props.element.$prototype.$key,
                        this.props.value[i][0],
                        this.props.formulaForm,
                        id + "#0",
                        this.pairElementWrapper(this.props.element.$prototype.$key.$name))}
                    {generateFormulaComponentForId(
                        this.props.element.$prototype,
                        this.props.value[i][1],
                        this.props.formulaForm,
                        id + "#1",
                        this.pairElementWrapper(get(this.props.element.$prototype.$name, "Value")))}
                    <RemoveButton
                        minItems={this.props.element.$minItems}
                        currentLength={this.props.value.length}
                        handleRemoveItem={() => this.props.handleRemoveItem(i)} />
                </div>
            );
        }
        return (<div id={this.props.id + "$elements"}>{elements}</div>);
    }
}


class RemoveButton extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (<button className="btn btn-default"
            type="button"
            title={this.props.minItems >= this.props.currentLength ? "Min number of items reached" : "Remove item"}
            onClick={() => this.props.handleRemoveItem()}
            disabled={this.props.minItems >= this.props.currentLength}>
            <i className="fa fa-minus" /> Remove
        </button>);
    }
}


/*
 * Used for rendering edit-groups that are backed up list of dictionaries
 * to be rendered as a list of key-value groups in the UI.
 */
class EditDictionaryGroup extends React.Component {
    constructor(props) {
        super(props);
    }

    wrapKeyGroup(element_name, innerHTML) {
        return (
            <div className="form-group" key={element_name}>
                <label className="col-lg-3 control-label" style={{"color": "green"}}>
                    {element_name + ":"}
                </label>
                <div className="col-lg-6" >
                    {innerHTML}
                </div>
                <i className="fa fa-question-circle" title={t("This field is used as a 'key' identifier in the resulting pillar data.")}></i>
            </div>
        );
    }

    generateItemName(item_index) {
        let name = this.props.element.$itemName;
        name = name.replace(/\${i}/g, parseInt(item_index, 10) + 1);
        name = name.replace(/\${.*}/g, txt => get(this.props.value[item_index][txt.substring(2, txt.length - 1)], txt));
        return name;
    }

    render() {
        let elements = [];
        for (let i in this.props.value) {
            let id = this.props.id + "#" + i;

            let item_elements = [];
            for (var element_name in this.props.element.$prototype) {
                if (element_name.startsWith("$") && element_name !== "$key") continue;
                item_elements.push(generateFormulaComponent(
                    this.props.element.$prototype[element_name],
                    this.props.value[i][element_name],
                    this.props.formulaForm,
                    id,
                    element_name === "$key" ? this.wrapKeyGroup : undefined
                 ));
            }

            elements.push(
                <div className="panel panel-default" id={id} key={id}>
                    <div className="panel-heading edit-group-heading">
                        <h4>{this.generateItemName(i)}</h4>
                        <i className="fa fa-minus"
                            onClick={() => this.props.handleRemoveItem(i)}
                            title={this.props.element.$minItems >= this.props.value.length ? "Min number of items reached" : "Remove item"}
                            disabled={this.props.element.$minItems >= this.props.value.length || this.props.isDisabled()} />
                    </div>
                    <div className="panel-body">
                        {item_elements}
                    </div>
                </div>
            );
        }
        return (<div id={this.props.id + "$elements"}>{elements}</div>);
    }
}

function get(value, default_value) {
    if (value === undefined)
        return default_value;
    return value;
}


module.exports = {
  EditGroup: EditGroup
}
