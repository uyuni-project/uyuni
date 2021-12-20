import * as React from "react";
import EditGroup from "./EditGroup";
import Group from "./Group";
import PasswordInput from "./PasswordInput";
import { default as Jexl } from "jexl";
import HelpIcon from "components/utils/HelpIcon";
import { Utils, Formulas } from "utils/functions";
const { capitalize, deepCopy } = Utils;
const { getEditGroupSubtype, EditGroupSubtype } = Formulas;

const BASIC_INPUT_TYPES = ["text", "email", "url", "date", "time"];

export type ElementDefinition = {
  $id?: string;
  $type: string;
  $name: string;
  $scope?: string;
  $disabled?: boolean;
  $visible?: boolean;
  $required?: boolean;
  $help?: string;
  $placeholder?: string;
  $values?: any;
  $match?: any;
  $min?: number;
  $max?: number;
  $minItems?: number;
  $maxItems?: number;
  $newItemValue?: any;
  $prototype?: any;
  $itemName?: any;
  /** DEPRECATED, use `$visible` instead */
  $visibleIf?: () => boolean;
};

type Context = {
  scope: any | null;
  layout: any;
  values: any;
  onFormulaChange?: any | null;
  getCleanValues?: any | null;
  clearValues: any | null;
  validate: any | null;
};

export const FormulaFormContext = React.createContext<Context>({
  scope: null,
  layout: {},
  values: {},
  onFormulaChange: null,
  getCleanValues: null,
  clearValues: null,
  validate: null,
});

export function generateFormulaComponent(
  element: ElementDefinition,
  value: string,
  formulaForm: any,
  parents?: any,
  wrapper?: any,
  disabled = false
) {
  const id = (parents ? parents + "#" : "") + element.$id;
  return generateFormulaComponentForId(element, value, formulaForm, id, wrapper, disabled);
}

export function generateFormulaComponentForId(
  element: ElementDefinition,
  value: any,
  formulaForm: any,
  id,
  wrapper,
  disabled = false
) {
  wrapper = get(wrapper, defaultWrapper);

  var isDisabled =
    (formulaForm.props.scope !== element.$scope && element.$scope !== "system") ||
    ("$disabled" in element && evalExpression(id, element.$disabled, formulaForm)) ||
    (!("$disabled" in element) && disabled);

  if ("$visible" in element && !evalExpression(id, element.$visible, formulaForm)) {
    return null;
  }

  // $visibleIf is deprecated
  if ("$visibleIf" in element && !checkVisibilityCondition(id, element.$visibleIf, formulaForm)) return null;

  const required = "$required" in element && evalExpression(id, element.$required, formulaForm);

  if (BASIC_INPUT_TYPES.indexOf(element.$type) >= 0)
    //Element is a basic html input type
    return wrapper(
      element.$name,
      required,
      <input
        type={element.$type}
        name={element.$name}
        id={id}
        className="form-control"
        onChange={formulaForm.handleChange}
        placeholder={element.$placeholder}
        title={element.$help}
        disabled={isDisabled}
        value={value}
        required={required}
        pattern={element.$match}
      />,
      element.$help
    );
  else if (element.$type === "password")
    return (
      <PasswordInput
        id={id}
        key={id}
        element={element}
        value={value}
        onChange={formulaForm.handleChange}
        disabled={isDisabled}
        required={required}
      />
    );
  else if (element.$type === "color")
    return wrapper(
      element.$name,
      required,
      <div className="input-group small-color-picker">
        <input
          type="color"
          name={element.$name}
          id={id}
          className="form-control"
          onChange={formulaForm.handleChange}
          title={element.$help}
          disabled={isDisabled}
          value={value}
        />
        <span className="input-group-btn">
          <button
            className="btn btn-default"
            title={t("Reset")}
            onClick={function (event) {
              event.preventDefault();
              console.warn("Reseting color picker is not implemented!");
              /* TODO: reset Value */
            }}
          >
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
      <input
        type="datetime-local"
        name={element.$name}
        id={id}
        className="form-control"
        onChange={formulaForm.handleChange}
        placeholder={element.$placeholder}
        title={element.$help}
        disabled={isDisabled}
        value={value}
      />,
      element.$help
    );
  else if (element.$type === "number")
    return wrapper(
      element.$name,
      required,
      <input
        type="number"
        step="1"
        max={get(element.$max, "")}
        min={get(element.$min, "")}
        name={element.$name}
        id={id}
        className="form-control"
        onChange={formulaForm.handleChange}
        placeholder={element.$placeholder}
        title={element.$help}
        disabled={isDisabled}
        value={value}
        required={required}
      />,
      element.$help
    );
  else if (element.$type === "group") {
    return (
      <Group id={id} key={id} header={element.$name} help={element.$help}>
        {generateChildrenFormItems(element, value, formulaForm, id, isDisabled)}
      </Group>
    );
  } else if (element.$type === "namespace") return generateChildrenFormItems(element, value, formulaForm, id);
  else if (element.$type === "edit-group") {
    return (
      <EditGroup
        id={id}
        key={element.$name}
        element={element}
        value={value}
        formulaForm={formulaForm}
        disabled={isDisabled}
      />
    );
  } else if (element.$type === "select")
    return wrapper(
      element.$name,
      required,
      <select
        className="form-control"
        name={element.$name}
        id={id}
        onChange={formulaForm.handleChange}
        title={element.$help}
        disabled={isDisabled}
        value={value}
      >
        {generateSelectList(element.$values)}
      </select>,
      element.$help
    );
  else if (element.$type === "boolean")
    return wrapper(
      element.$name,
      required,
      <div className="checkbox">
        <input
          type="checkbox"
          onChange={formulaForm.handleChange}
          name={element.$name}
          id={id}
          title={element.$help}
          disabled={isDisabled}
          checked={value}
        />
      </div>,
      element.$help
    );
  else {
    console.error("Unknown $type: " + element.$type);
    return wrapper(element.$name, required, <div>{JSON.stringify(value)}</div>);
  }
}

export function evalExpression(id: string, condition: any | undefined, formulaForm: any) {
  const context = {
    this: buildValuePath(id, formulaForm.getFormulaValues()),
    formValues: formulaForm.getFormulaValues(),
  };
  return Jexl.evalSync(condition + "", context);
}

function buildValuePath<ValueType>(id: string, formValues) {
  const tokens = id.split("#");
  let value = formValues;

  interface ValuePath<T = unknown> {
    parent: ValuePath<T> | null;
    value: T;
  }

  let prevPath: ValuePath<ValueType> | null = null;
  let path: ValuePath<ValueType> | null = null;
  for (let i in tokens) {
    if (value[tokens[i]] === undefined) {
      return null;
    }
    value = value[tokens[i]];
    path = {
      parent: prevPath,
      value: value,
    };
    prevPath = path;
  }
  return path;
}

function getConditionValue(value) {
  value = value.trim();
  if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'")))
    value = value.substring(1, value.length - 1);
  return value;
}

function getConditionId(element_id, path) {
  path = path.trim();
  if (path.startsWith(".")) {
    let relpath = path;
    let base = element_id.split("#");
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
function checkVisibilityCondition(id, condition, formulaForm) {
  if (condition.includes("!=")) {
    condition = condition.split("!=");
    return (
      String(getValueById(formulaForm.getFormulaValues(), getConditionId(id, condition[0]))) !==
      getConditionValue(condition[1])
    );
  } else if (condition.includes("==")) {
    condition = condition.split("==");
    return (
      String(getValueById(formulaForm.getFormulaValues(), getConditionId(id, condition[0]))) ===
      getConditionValue(condition[1])
    );
  }
  return false;
}

function generateChildrenFormItems(element, value, formulaForm, id, disabled = false) {
  var child_items: React.ReactNode[] = [];
  for (var child_name in element) {
    if (child_name.startsWith("$")) continue;
    child_items.push(
      generateFormulaComponent(element[child_name], value[child_name], formulaForm, id, undefined, disabled)
    );
  }
  return child_items;
}

function generateSelectList(data) {
  var options: React.ReactNode[] = [];
  for (var key in data)
    options.push(
      <option key={key} value={data[key]}>
        {data[key]}
      </option>
    );
  return options;
}

function defaultWrapper(elementName, required, element, help = null) {
  return wrapFormGroupWithLabel(
    elementName,
    required,
    <React.Fragment>
      <div className="col-lg-6">{element}</div>
      <HelpIcon text={help} />
    </React.Fragment>
  );
}

function wrapFormGroupWithLabel(element_name: string, required?: boolean, innerHTML?: React.ReactNode) {
  return (
    <div className="form-group" key={element_name}>
      {wrapLabel(element_name, required)}
      {innerHTML}
    </div>
  );
}

function wrapLabel(text: React.ReactNode, required?: boolean, label_for?: string) {
  return (
    <label htmlFor={label_for} className="col-lg-3 control-label">
      {text}
      {required ? <span className="required-form-field"> *</span> : null}:
    </label>
  );
}

function getValueById(values, id) {
  let parents = id.split("#");
  let value = values;
  for (let i in parents) {
    if (value[parents[i]] === undefined) {
      return null;
    }
    value = value[parents[i]];
  }
  return value;
}

export function get(value, def) {
  if (value === undefined) return def;
  return value;
}

export function text(txt) {
  if (!txt) {
    return "";
  }
  // replace variables
  if (typeof txt === "string" || txt instanceof String) {
    txt = txt.replace(/\${productName}/g, Utils.getProductName());
  }
  return txt;
}

export const FormulaFormRenderer = () => (
  <FormulaFormContext.Consumer>
    {(context) => <UnwrappedFormulaFormRenderer {...context} />}
  </FormulaFormContext.Consumer>
);

type UnwrappedFormulaFormRendererProps = {
  scope: string | null;
  values: any;
  layout?: any;
  onChange?: (id: string, value: string) => any;
  registerValidationTrigger?: (...args: any[]) => any;
};

// layout
// values
// onValuesChanged
class UnwrappedFormulaFormRenderer extends React.Component<UnwrappedFormulaFormRendererProps> {
  submitButton = React.createRef<HTMLInputElement>();

  handleChange = (event) => {
    let id, value;
    if (event.id) {
      id = event.id;
      value = event.value;
    } else {
      id = event.target.id;

      if (event.target.type === "checkbox") {
        value = event.target.checked;
      } else if (event.target.type === "number") {
        value = isNaN(event.target.valueAsNumber) ? "" : event.target.valueAsNumber;
      } else {
        value = event.target.value;
      }
    }

    // notify the context provider about the values change
    this.props.onChange?.(id, value);
  };

  componentDidMount() {
    this.props.registerValidationTrigger?.(this.triggerValidation);
  }

  triggerValidation = () => {
    this.submitButton.current?.click();
  };

  dontSubmitForm = (event) => {
    event.preventDefault();
  };

  getFormulaValues = () => {
    return this.props.values;
  };

  render() {
    const { layout, values } = this.props;
    if (!values) {
      return null;
    }

    let form: React.ReactNode[] = [];
    for (const key in layout) {
      form.push(generateFormulaComponent(layout[key], values[key], this));
    }
    return (
      <form
        id="formula-form"
        className="form-horizontal"
        onSubmit={(event) => {
          event.preventDefault();
          return false;
        }}
      >
        <input ref={this.submitButton} type="submit" hidden />
        {form}
      </form>
    );
  }
}

/*
 * Remove $meta attrs and add $ifEmpty values if needed.
 */
function getValuesClean(values, layout) {
  let result: any = {};
  for (let key in values) {
    if (key.startsWith("$meta")) {
      continue;
    }
    let value = values[key];
    let element = layout[key];
    if (element.$type === "group" || element.$type === "namespace") {
      value = getValuesClean(value, element);
      if (!jQuery.isEmptyObject(value)) result[key] = value;
    } else if (element.$scope === "system" && !(value && value.length === 0)) {
      value = checkIfEmptyValueAttrExists(value, element, values);
      result[key] = cleanMeta(value);
    }
  }
  return result;
}

/*
 * Remove $meta element from all form values.
 */
function cleanMeta(value) {
  let result: any = {};
  if (value instanceof Object && !Array.isArray(value)) {
    for (let key in value) {
      if (key.startsWith("$meta")) {
        continue;
      }
      result[key] = cleanMeta(value[key]);
    }
  } else {
    result = value;
  }
  return result;
}

function checkIfEmptyValueAttrExists(value, element, formulaValues) {
  if ((value == null || value.length === 0) && !element.$optional && (element.$ifEmpty || element.$ifEmpty === null)) {
    value = element.$ifEmpty;
    formulaValues[element.$id] = value;
  }
  return value;
}

function isPrimitiveElement(element) {
  return element.$type !== "group" && element.$type !== "namespace" && element.$type !== "edit-group";
}

/*
 * Traverses the form values and adjusts certain elements.
 * (e.g. converts arrays to dictionaries in case of edit-group as a dictionary)
 */
function preprocessCleanValues(values, layout) {
  const result: any = {};
  Object.entries(values).forEach(([key, value]: [string, any]) => {
    if (key === "$required") {
      return;
    }

    const element = layout[key];
    const editGroupSubType = getEditGroupSubtype(element);

    if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
      result[key] = value
        .map((entry) => preprocessCleanValues(entry, element.$prototype))
        .filter((entry) => entry["$key"] !== "")
        .reduce((acc, entry) => {
          acc[entry["$key"]] = entry;
          delete entry["$key"];
          return acc;
        }, {});
    } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
      result[key] = value
        .filter((entry) => entry[0] !== "")
        .reduce((acc, entry) => {
          acc[entry[0]] = entry[1];
          return acc;
        }, {});
    } else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
      result[key] = value.map((entry) => preprocessCleanValues(entry, element.$prototype));
    } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_LIST) {
      result[key] = value.filter((entry) => entry !== "");
      // we need to recur to groups as they can contain edit-groups that need an adjustment
    } else if (element !== undefined && (element.$type === "group" || element.$type === "namespace")) {
      result[key] = preprocessCleanValues(value, element);
    } else {
      result[key] = value;
    }
  });
  return result;
}

type FormulaFormContextProviderProps = {
  layout?: any;
  systemData?: any;
  groupData?: any;
  scope?: any;
};

type FormulaFormContextProviderState = {
  formulaLayout?: any;
  formulaValues?: any;
  formulaChanged: boolean;
  validationTrigger?: any;
};

// layout
// systemData
// groupData
// scope
export class FormulaFormContextProvider extends React.Component<
  FormulaFormContextProviderProps,
  FormulaFormContextProviderState
> {
  constructor(props: FormulaFormContextProviderProps) {
    super(props);
    const layout = this.preprocessLayout(props.layout);
    this.preprocessData(layout, get(props.systemData, {}));
    this.preprocessData(layout, get(props.groupData, {}));
    const values = this.generateValues(layout, props.groupData, props.systemData);

    this.state = {
      formulaLayout: layout,
      formulaValues: values,
      formulaChanged: false,
    };
  }

  // TODO implement componentDidUpdate

  render() {
    const contextValue = {
      scope: this.props.scope,
      layout: this.state.formulaLayout,
      values: this.state.formulaValues,
      onChange: this.onFormulaChange,
      clearValues: this.clearValues,
      validate: this.validate,
      registerValidationTrigger: this.registerValidationTrigger,
    };

    return <FormulaFormContext.Provider value={contextValue}>{this.props.children}</FormulaFormContext.Provider>;
  }

  getFormulaValues = () => {
    return this.state.formulaValues;
  };

  onFormulaChange = (id, value) => {
    let values = this.state.formulaValues;
    let parents = id.split("#");
    for (var i in parents.slice(0, -1)) {
      if (values[parents[i]] === undefined) values[parents[i]] = {};
      values = values[parents[i]];
    }
    values[parents[parents.length - 1]] = value;

    this.setFormulaValues(this.state.formulaValues);
  };

  registerValidationTrigger = (validationTrigger) => {
    this.setState({
      validationTrigger: validationTrigger,
    });
  };

  setFormulaValues = (values) => {
    this.setState({
      formulaValues: values,
      formulaChanged: true,
    });
  };

  getCleanValues = () => {
    return getValuesClean(
      preprocessCleanValues(this.state.formulaValues, this.state.formulaLayout),
      this.state.formulaLayout
    );
  };

  walkValueTree = (value, formulaForm, formulaValues, validationFunc) => {
    if (value instanceof Object) {
      for (let key in value) {
        if (!key.startsWith("$meta$")) {
          if ("$meta$" + key in value) {
            const meta = value["$meta$" + key];
            const keepWalking = validationFunc(value, key, meta, formulaForm, formulaValues);
            if (keepWalking) {
              this.walkValueTree(value[key], formulaForm, formulaValues, validationFunc);
            }
          } else {
            this.walkValueTree(value[key], formulaForm, formulaValues, validationFunc);
          }
        }
      }
    }
  };

  getEmptyValues = () => {
    let requiredErrors: any[] = [];
    this.walkValueTree(
      this.state.formulaValues,
      this,
      this.state.formulaValues,
      (parentVal, key, meta, formulaForm, formulaValues) => {
        const val = parentVal[key];
        if (meta["visibleIf"]) {
          const visibleIf = checkVisibilityCondition(meta["id"], meta["visibleIf"], formulaForm);
          if (!visibleIf) {
            return false;
          }
        } else if (meta["visible"]) {
          const visible = evalExpression(meta["id"], meta["visible"], formulaForm);
          if (!visible) {
            return false;
          }
        }
        if (meta["required"]) {
          const required = evalExpression(meta["id"], meta["required"] + "", formulaForm);
          if (required) {
            if (Array.isArray(val) && val.some((v) => !v)) {
              requiredErrors.push(meta["name"]);
            } else if (!val) {
              requiredErrors.push(meta["name"]);
            }
          }
        }
        return true;
      }
    );
    return requiredErrors;
  };

  checkFieldsFormat = () => {
    const errors: any[] = [];
    this.walkValueTree(
      this.state.formulaValues,
      this,
      this.state.formulaValues,
      (parentVal, key, meta, formulaForm, formulaValues) => {
        const value = parentVal[key];
        if (meta["match"]) {
          try {
            let regex = meta["match"].startsWith("^") ? meta["match"] : "^" + meta["match"];
            regex = regex.endsWith("$") ? regex : regex + "$";
            const re = new RegExp(regex, "u");
            if (Array.isArray(value)) {
              // match each value
              if (!value.every((v) => re.test(v))) {
                errors.push(meta["name"]);
              }
            } else {
              if (!re.test(value)) {
                errors.push(meta["name"]);
              }
            }
          } catch (err) {
            console.log("Error matching regex: '" + meta["match"] + "':" + err);
          }
        }
        return true;
      }
    );
    return errors;
  };

  /**
   * Validate and return errors (if any) + clean values
   */
  validate = () => {
    if (this.state.validationTrigger) {
      this.state.validationTrigger();
    }
    const emptyRequiredFields = [...new Set(this.getEmptyValues())];
    const invalidFields = [...new Set(this.checkFieldsFormat())];

    let errors;
    if (emptyRequiredFields.length > 0 || invalidFields.length > 0) {
      errors = {
        required: emptyRequiredFields,
        invalid: invalidFields,
      };
    }

    return {
      errors: errors,
      values: this.getCleanValues(),
    };
  };

  /*
   * Set missing attributes ($type, $scope) to default values,
   * set $default values,
   * set $newItemValues for edit-groups.
   */
  preprocessLayout = (layout: ElementDefinition, scope = "system") => {
    Object.entries(layout)
      .filter(([name, element]) => !name.startsWith("$") || name === "$key")
      .forEach(([name, element]) => {
        this.adjustElementBasicAttrs([name, element], scope);

        // groups are processed recursively
        if (element.$type === "group" || element.$type === "namespace") {
          this.preprocessLayout(element, element.$scope);
        } // edit-groups too
        else if (element.$type === "edit-group") {
          element.$prototype = this.preprocessLayout(element.$prototype, element.$scope);
          this.adjustEditGroup(element);
        }
      });

    return layout;
  };

  /*
   * Goes through the layout and adjusts data for certain structures
   * (edit-group in a form of nested dictionary needs to be converted to lists, etc.)
   */
  preprocessData = (layout: ElementDefinition, data: any) => {
    Object.entries(layout)
      .filter(([name, element]) => !(data[name] === undefined || (name.startsWith("$") && name !== "$key")))
      .forEach(([name, element]) => {
        const editGroupSubType = getEditGroupSubtype(element);
        // other edit-group as-a-dictionary needs to be converted to an array
        if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
          data[name] = Object.entries(data[name] || {}).map((e: any) => {
            e[1]["$key"] = e[0];
            this.preprocessData(element.$prototype, e[1]);
            return e[1];
          });
        } // edit-group as-a-primitive-dictionary too
        else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
          data[name] = Object.entries(data[name] || {});
        } // the last form of a recursive edit-group needs to be processed recursively
        else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
          Object.entries(data[name] || {}).forEach(([name, value]) => this.preprocessData(element.$prototype, value));
        } // group elements also must be processed as they can contain edit-groups
        else if (!isPrimitiveElement(element)) {
          this.preprocessData(element, data[name]);
        }
      });
  };

  /*
   * Adjust edit group default and 'new item' value of edit-group
   * Some subtypes of edit-group need special handling (e.g. nested dictionary
   * needs to be converted to lists).
   */
  adjustEditGroup = (element) => {
    // Adjust common edit-group attributes
    if (element.$prototype.$type === undefined) {
      element.$prototype.$type = "group";
    } else if (element.$prototype.$scope === undefined) {
      element.$prototype.$scope = "system";
    }

    if (element.$itemName === undefined) {
      // eslint-disable-next-line no-template-curly-in-string
      element.$itemName = "Item ${i}";
    }

    // Adjust defaults && new values
    element.$default = this.adjustNestedDefault(element, element.$default);
    const editGroupSubType = getEditGroupSubtype(element);
    if (
      editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES ||
      editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES
    ) {
      element.$newItemValue = this.generateValues(element.$prototype, {}, {}, element);
    } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
      element.$newItemValue = [
        this.defaultValue(element.$prototype.$key.$type, element.$prototype.$key.$default),
        this.defaultValue(element.$prototype.$type, element.$prototype.$default),
      ];
    } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_LIST) {
      element.$newItemValue = this.defaultValue(element.$prototype.$type, element.$prototype.$default);
    }

    // if we don't have minimum number of items, let's create some
    // todo handle the edit groups where their key must be unique
    element.$default = get(element.$default, []);
    while (element.$default.length < element.$minItems) {
      element.$default.push(deepCopy(element.$newItemValue));
    }
  };

  /*
   * This needs to be recursive as the element.$default can be a complex nested
   * structure of edit-groups/groups which we need to adjust.
   */
  adjustNestedDefault = (element, defVal) => {
    // helper function for setting values from the prototype
    const setMissingValues = (value) => {
      Object.keys(element.$prototype)
        .filter((att) => !att.startsWith("$") && !Object.keys(value).includes(att))
        .forEach((att) => (value[att] = element.$prototype[att].$default));
    };

    const recurOnVals = (value) => {
      return Object.entries(value)
        .map(([nestedName, nestedVal]) => [
          nestedName,
          this.adjustNestedDefault(element.$prototype[nestedName], nestedVal),
        ])
        .reduce((acc, entry) => {
          acc[entry[0]] = entry[1];
          return acc;
        }, {});
    };

    const editGroupSubType = getEditGroupSubtype(element);
    if (editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES) {
      // normalize the default (convert from an object to array)
      return Object.entries(defVal || {}).map(([name, value]: [string, any]) => {
        value = recurOnVals(value);
        value["$key"] = name;
        setMissingValues(value);
        return value;
      });
    } else if (editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES) {
      return (defVal || []).map((defEntry) => {
        defEntry = recurOnVals(defEntry);
        setMissingValues(defEntry);
        return defEntry;
      });
    } else if (editGroupSubType === EditGroupSubtype.PRIMITIVE_DICTIONARY) {
      return Object.entries(defVal || {});
    }

    return defVal;
  };

  // consumes an element entry: [element name, element]
  adjustElementBasicAttrs = ([elementName, element], scope) => {
    if (!("$scope" in element)) {
      element.$scope = scope;
    }

    if (element.$type === "hidden-group") {
      element.$type = "namespace";
    }

    if (!("$type" in element)) {
      element.$type = "text";
    }

    if (element.$name === undefined && elementName === "$key") {
      element.$name = "Key";
    }

    if (elementName === "$key") {
      element["$required"] = true;
    }

    element.$id = elementName;
    element.$name = text(get(element.$name, capitalize(elementName)));
    element.$help = text(get(element.$help, element.$name));
    element.$placeholder = text(get(element.$placeholder, ""));

    if (isPrimitiveElement(element)) {
      element.$default = this.defaultValueForElement(element);
    }
  };

  defaultValue = (type?: string, defValue?: any, selectValues?: any[]) => {
    if (type === "boolean") return get(defValue, false);
    else if (type === "select") return get(defValue, selectValues?.[0]);
    else if (type === "color") return get(defValue, "#000000");
    else if (type === "password") return get(defValue, "");

    return get(defValue, "");
  };

  defaultValueForElement = (element) => {
    return this.defaultValue(element.$type, element.$default, element.$values);
  };

  /*
   * For each key in the layout, generate and return a defensive copy of an
   * object that follows the structure of the layout and has values populated
   * based on the system data, group data and layout default.
   */
  generateValues = (layout, group_data, system_data, UNUSED_ARG?: any) => {
    const generateValuesInternal = (layout, group_data, system_data, prototypeParentId?: any, elementIndex?: any) => {
      let result: any = {};
      for (let key in layout) {
        if (key.startsWith("$") && key !== "$key") continue;

        let value: any = null;
        let element = layout[key];
        let elementId;
        if (prototypeParentId && typeof elementIndex !== "undefined" && elementIndex !== null) {
          elementId = prototypeParentId + "#" + elementIndex + "#" + element.$id;
        } else {
          elementId = (layout.$id ? layout.$id + "#" : "") + element.$id;
        }

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
          if (
            editGroupSubType === EditGroupSubtype.LIST_OF_DICTIONARIES ||
            editGroupSubType === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES
          ) {
            // do not do merging of edit-group values,
            // take either system or group value based on the logic above
            // and process it recursively (hack: pass it always as "system" scope)
            value = value.map((entry, index) =>
              generateValuesInternal(element.$prototype, entry, {}, elementId, index)
            );
          }
        }

        if (value === null) {
          value = "";
        }

        result[key] = value;

        if (
          element.$type === "edit-group" &&
          !(
            getEditGroupSubtype(element) === EditGroupSubtype.LIST_OF_DICTIONARIES ||
            getEditGroupSubtype(element) === EditGroupSubtype.DICTIONARY_OF_DICTIONARIES
          )
        ) {
          result["$meta$" + key] = {
            id: elementId,
            required: key === "$key" ? true : element.$prototype["$required"],
            disabled: element.$prototype["$disabled"],
            name: element["$name"],
            match: element.$prototype["$match"],
            visibleIf: element["$visibleIf"],
            visible: element["$visible"],
          };
        } else {
          result["$meta$" + key] = {
            id: elementId,
            required: key === "$key" ? true : element["$required"],
            disabled: element["$disabled"],
            name: element["$name"],
            match: element["$match"],
            visibleIf: element["$visibleIf"],
            visible: element["$visible"],
          };
        }
      }
      return result;
    };

    return deepCopy(generateValuesInternal(layout, group_data, system_data));
  };

  clearValues = (clearValuesConfirmation) => {
    const layout = this.state.formulaLayout;
    if (clearValuesConfirmation()) {
      let clearValues: any = {};
      if (this.props.scope === "system") {
        clearValues = this.generateValues(layout, get(this.props.groupData, {}), {});
      } else {
        clearValues = this.generateValues(layout, {}, {});
      }

      this.setFormulaValues(clearValues);
    }
  };
}
