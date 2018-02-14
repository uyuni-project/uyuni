const React = require("react");

class EditGroup extends React.Component {
    constructor(props) {
        super(props);

        this.handleAddItem = this.handleAddItem.bind(this);
        this.handleRemoveItem = this.handleRemoveItem.bind(this);
    }

    handleAddItem(event) {
        if (this.props.element.$maxItems <= this.props.value.length)
            return;

        let newValue = this.props.value;
        newValue.push(Object.assign({}, this.props.element.$newItemValue));
        this.props.formulaForm.handleChange({
            id: this.props.id,
            value: newValue
        });
    }

    handleRemoveItem(index) {
        if (this.props.element.$minItems >= this.props.value.length)
            return;

        this.props.value.splice(index, 1);
        this.props.formulaForm.handleChange({
            id: this.props.id,
            value: this.props.value
        });
    }

    generateItems() {
        let elements = [];
        for (let i in this.props.value) {
            let id = this.props.id + "$" + i;
            let item_elements = [];
            for (var element_name in this.props.element.$prototype) {
                if (element_name.startsWith("$")) continue;
                item_elements.push(this.props.generateFormulaComponent(
                    this.props.element.$prototype[element_name],
                    this.props.value[i][element_name],
                    this.props.formulaForm,
                    id));
            }

            elements.push(
                <div className="panel panel-default" id={id} key={id}>
                    <div className="panel-heading edit-group-heading">
                        <h4>{this.generateItemName(i)}</h4>
                        <i className="fa fa-minus"
                            onClick={() => this.handleRemoveItem(i)}
                            title={this.props.element.$minItems >= this.props.value.length ? "Min number of items reached" : "Remove item"}
                            disabled={this.props.element.$minItems >= this.props.value.length} />
                    </div>
                    <div className="panel-body">
                        {item_elements}
                    </div>
                </div>
            );
        }
        return elements;
    }

    generateItemName(item_index) {
        let name = this.props.element.$itemName;
        name = name.replace(/\${i}/g, parseInt(item_index, 10) + 1);
        name = name.replace(/\${.*}/g, txt => get(this.props.value[item_index][txt.substring(2, txt.length - 1)], txt));
        return name;
    }

    render() {
        return (
            <div className="panel panel-default" id={this.props.id}>
                <div className="panel-heading">
                    <h4>{this.props.element.$name}</h4>
                </div>
                <div className="panel-body">
                    <div id={this.props.id + "$elements"}>
                        {this.generateItems()}
                    </div>
                    <button className="btn btn-default"
                        type="button"
                        title={this.props.element.$maxItems <= this.props.value.length ? "Max number of items reached" : "Add Element"}
                        onClick={this.handleAddItem}
                        disabled={this.props.element.$maxItems <= this.props.value.length}>
                        <i className="fa fa-plus" /> Add Item
                    </button>
                </div>
            </div>
        );
    }
}

function get(value, default_value) {
    if (value === undefined)
        return default_value;
    return value;
}


module.exports.EditGroup = EditGroup;
