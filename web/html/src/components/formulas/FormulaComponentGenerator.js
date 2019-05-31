/* eslint-disable */
// needed because of circular deps
module.exports = {
  generateFormulaComponent: generateFormulaComponent,
  generateFormulaComponentForId: generateFormulaComponentForId
}

const React = require("react");
const EditGroup = require("./EditGroup").EditGroup;
const PasswordInput = require("./PasswordInput").PasswordInput;
const BASIC_INPUT_TYPES = ["text", "email", "url", "date", "time"];


function generateFormulaComponent(element, value, formulaForm, parents, wrapper) {
    const id = (parents ? parents + "#" : "") + element.$id;
    return generateFormulaComponentForId(element, value, formulaForm, id, wrapper);
}

function generateFormulaComponentForId(element, value, formulaForm, id, wrapper) {
    wrapper = get(wrapper, defaultWrapper);

    var isDisabled = (formulaForm.props.scope !== element.$scope && element.$scope !== "system");

    if ("$visibleIf" in element && !checkVisibilityCondition(id, element.$visibleIf, formulaForm))
        return null;

    if (BASIC_INPUT_TYPES.indexOf(element.$type) >= 0) //Element is a basic html input type
        return wrapper(
            element.$name,
            <input type={element.$type} name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
        );
    else if (element.$type === "password")
        return <PasswordInput id={id} key={id} element={element} value={value} onChange={formulaForm.handleChange} disabled={isDisabled} />;
    else if (element.$type === "color")
        return wrapper(
                element.$name,
                <div className="input-group small-color-picker">
                    <input type="color" name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} title={element.$help} disabled={isDisabled} value={value} />
                    <span className="input-group-btn">
                        <button className="btn btn-default" title="Reset" onClick={function (event) { event.preventDefault(); console.warn("Reseting color picker is not implemented!"); /* TODO: reset Value */ }}>
                            <i className="fa fa-undo no-margin" />
                        </button>
                    </span>
                </div>
        );
    else if (element.$type === "datetime")
        return wrapper(
                element.$name,
                <input type="datetime-local" name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
        );
    else if (element.$type === "number")
        return wrapper(
                element.$name,
                <input type="number" steps="1" max={get(element.$max, "")} min={get(element.$min, "")} name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />
        );
    else if (element.$type === "group") {
        return (
            <div className="panel panel-default" id={id} key={id}>
                <div className="panel-heading">
                    <h4>{element.$name}</h4>
                </div>
                <div className="panel-body">
                    {generateChildrenFormItems(element, value, formulaForm, id)}
                </div>
            </div>
        );
    }
    else if (element.$type === "namespace")
        return generateChildrenFormItems(element, value, formulaForm, id);
    else if (element.$type === "edit-group") {
        return <EditGroup
            id={id}
            key={element.$name}
            element={element}
            value={value}
            formulaForm={formulaForm} />;
    } else if (element.$type === "select")
        return wrapper(
                element.$name,
                <select className="form-control" name={element.$name} id={id} onChange={formulaForm.handleChange} title={element.$help} disabled={isDisabled} value={value}>
                    {generateSelectList(element.$values)}
                </select>
        );
    else if (element.$type === "boolean")
        return wrapper(
                element.$name,
                <div className="checkbox">
                    <input type="checkbox" onChange={formulaForm.handleChange} name={element.$name} id={id} title={element.$help} disabled={isDisabled} checked={value} />
                </div>
        );
    else {
        console.error("Unknown $type: " + element.$type);
        return wrapper(
                element.$name,
                <div>{JSON.stringify(value)}</div>
        );
    }
}

function getConditionValue(value) {
    value = value.trim();
    if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'")))
        value = value.substring(1, value.length-1);
    return value;
}

function getConditionId(element_id, path) {
    path = path.trim();
    if (path.startsWith(".")) {
        let relpath = path;
        let base = element_id.split('#');
        while (relpath.startsWith(".")) {
            relpath = relpath.substring(1); // remove first dot
            base.pop(); // remove last element
        }
        base.push(relpath);
        path = base.join("#");
    }
    return path;
}

function checkVisibilityCondition(id, condition, formulaForm) {
    if (condition.includes("!=")) {
        condition = condition.split("!=");
        return String(formulaForm.getValueById(getConditionId(id, condition[0]))) !== getConditionValue(condition[1]);
    }
    else if (condition.includes("==")) {
        condition = condition.split("==");
        return String(formulaForm.getValueById(getConditionId(id, condition[0]))) === getConditionValue(condition[1]);
    }
    return false;
}

function generateChildrenFormItems(element, value, formulaForm, id) {
    var child_items = [];
    for (var child_name in element) {
        if (child_name.startsWith("$")) continue;
        child_items.push(generateFormulaComponent(element[child_name], value[child_name], formulaForm, id));
    }
    return child_items;
}

function generateSelectList(data) {
    var options = [];
    for (var key in data)
        options.push(<option value={data[key]}>{data[key]}</option>);
    return options;
}

function defaultWrapper(elementName, element) {
    return wrapFormGroupWithLabel(elementName,
            <div className="col-lg-6">
              {element}
            </div>
        );
}

function wrapFormGroupWithLabel(element_name, innerHTML) {
    return (
        <div className="form-group" key={element_name}>
            {wrapLabel(element_name)}
            {innerHTML}
        </div>
    );
}

function wrapLabel(text, label_for) {
    return (
        <label htmlFor={label_for} className="col-lg-3 control-label">
            {text + ":"}
        </label>
    );
}

function get(value, def) {
    if (value === undefined)
        return def;
    return value;
}


