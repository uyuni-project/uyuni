/* eslint-disable */
import * as React from 'react';
import EditGroup from './EditGroup';
import Group from './Group';
import PasswordInput from './PasswordInput';
import {default as Jexl} from 'jexl';
import HelpIcon from 'components/utils/HelpIcon';

const BASIC_INPUT_TYPES = ["text", "email", "url", "date", "time"];


export function generateFormulaComponent(element, value, formulaForm, parents, wrapper, disabled = false) {
    const id = (parents ? parents + "#" : "") + element.$id;
    return generateFormulaComponentForId(element, value, formulaForm, id, wrapper, disabled);
}

export function generateFormulaComponentForId(element, value, formulaForm, id, wrapper, disabled = false) {
    wrapper = get(wrapper, defaultWrapper);

    var isDisabled = (formulaForm.props.scope !== element.$scope && element.$scope !== "system") || 
        ("$disabled" in element && evalExpression(id, element.$disabled, formulaForm)) ||
        (!("$disabled" in element) && disabled);

    if ("$visible" in element && !evalExpression(id, element.$visible, formulaForm)) {
        return null;
    }

    // $visibleIf is deprecated
    if ("$visibleIf" in element && !checkVisibilityCondition(id, element.$visibleIf, formulaForm))
        return null;

    const required = ("$required" in element) && evalExpression(id, element.$required, formulaForm);

    if (BASIC_INPUT_TYPES.indexOf(element.$type) >= 0) //Element is a basic html input type
        return wrapper(
            element.$name,
            required,
            <input type={element.$type} name={element.$name} id={id} className="form-control"
                onChange={formulaForm.handleChange} placeholder={element.$placeholder} title={element.$help}
                disabled={isDisabled} value={value}
                required={required}
                pattern={element.$match} />,
            element.$help
        );
    else if (element.$type === "password")
        return <PasswordInput id={id} key={id} element={element} value={value} onChange={formulaForm.handleChange} disabled={isDisabled} required={required}
            help={element.$help}
        />;
    else if (element.$type === "color")
        return wrapper(
                element.$name,
                required,
                <div className="input-group small-color-picker">
                    <input type="color" name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} title={element.$help} disabled={isDisabled} value={value} />
                    <span className="input-group-btn">
                        <button className="btn btn-default" title={t("Reset")} onClick={function (event) { event.preventDefault(); console.warn("Reseting color picker is not implemented!"); /* TODO: reset Value */ }}>
                            <i className="fa fa-undo no-margin" />
                        </button>
                    </span>
                </div>,
                element.$help
        );
    else if (element.$type === "datetime")
        return wrapper(
                element.$name,
                required,
                <input type="datetime-local" name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} placeholder={element.$placeholder} title={element.$help} disabled={isDisabled} value={value} />,
                element.$help
        );
    else if (element.$type === "number")
        return wrapper(
                element.$name,
                required,
                <input type="number" steps="1" max={get(element.$max, "")} min={get(element.$min, "")} name={element.$name} id={id} className="form-control" onChange={formulaForm.handleChange} placeholder={element.$placeholder}
                    title={element.$help}
                    disabled={isDisabled}
                    value={value}
                    required={required} />,
                element.$help
        );
    else if (element.$type === "group") {
        return <Group id={id} key={id} header={element.$name} help={element.$help}>
            {generateChildrenFormItems(element, value, formulaForm, id, isDisabled)}
        </Group>;
    }
    else if (element.$type === "namespace")
        return generateChildrenFormItems(element, value, formulaForm, id);
    else if (element.$type === "edit-group") {
        return <EditGroup
            id={id}
            key={element.$name}
            element={element}
            value={value}
            formulaForm={formulaForm}
            disabled={isDisabled} />;
    } else if (element.$type === "select")
        return wrapper(
                element.$name,
                required,
                <select className="form-control" name={element.$name} id={id} onChange={formulaForm.handleChange} title={element.$help} disabled={isDisabled} value={value}>
                    {generateSelectList(element.$values)}
                </select>,
                element.$help
        );
    else if (element.$type === "boolean")
        return wrapper(
                element.$name,
                required,
                <div className="checkbox">
                    <input type="checkbox" onChange={formulaForm.handleChange} name={element.$name} id={id} title={element.$help} disabled={isDisabled} checked={value} />
                </div>,
                element.$help
        );
    else {
        console.error("Unknown $type: " + element.$type);
        return wrapper(
                element.$name,
                required,
                <div>{JSON.stringify(value)}</div>
        );
    }
}

export function evalExpression(id, condition, formulaForm) {
    const context = {
        "this": buildValuePath(id, formulaForm.getFormulaValues()),
        formValues: formulaForm.getFormulaValues()
    }
    return Jexl.evalSync(condition + "", context);
}

function buildValuePath(id, formValues) {
    const tokens = id.split("#");
    let value = formValues;
    let prevPath = null;
    let path = null;
    for (let i in tokens) {
        if (value[tokens[i]] === undefined) {
            return null;
        }
        value = value[tokens[i]];
        path = {
            parent: prevPath,
            value: value
        };
        prevPath = path;
    }
    return path;
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

/**
 * This function is deprecated.
 * @param {*} id 
 * @param {*} condition 
 * @param {*} formulaForm
 * @deprecated 
 */
export function checkVisibilityCondition(id, condition, formulaForm) {
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

function generateChildrenFormItems(element, value, formulaForm, id, disabled = false) {
    var child_items = [];
    for (var child_name in element) {
        if (child_name.startsWith("$")) continue;
        child_items.push(generateFormulaComponent(element[child_name], value[child_name], formulaForm, id, undefined, disabled));
    }
    return child_items;
}

function generateSelectList(data) {
    var options = [];
    for (var key in data)
        options.push(<option value={data[key]}>{data[key]}</option>);
    return options;
}

function defaultWrapper(elementName, required, element, help = null) {
    return wrapFormGroupWithLabel(elementName, required,
        <React.Fragment>
            <div className="col-lg-6">
              {element}
            </div>
            <HelpIcon text={help}/>
        </React.Fragment>
        );
}

function wrapFormGroupWithLabel(element_name, required, innerHTML) {
    return (
        <div className="form-group" key={element_name}>
            {wrapLabel(element_name, required)}
            {innerHTML}
        </div>
    );
}

function wrapLabel(text, required, label_for) {
    return (
        <label htmlFor={label_for} className="col-lg-3 control-label">
            {text}
            {required ? <span className="required-form-field"> *</span> : null}
            :
        </label>
    );
}

function get(value, def) {
    if (value === undefined)
        return def;
    return value;
}
