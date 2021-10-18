import * as React from "react";

import _isNil from "lodash/isNil";

import { Label } from "./Label";
import { FormGroup } from "./FormGroup";
import { FormContext } from "./Form";

type Validator = (...args: any[]) => boolean | Promise<boolean>;

export type InputBaseProps<ValueType = string> = {
  /** name of the field to map in the form model.
   * The value can be an array of names if multiple inputs are contained in this field.
   */
  name?: string | Array<string>;

  /** Default value if none is set.
   * In the case of multiple properties managed by this input, an object with the properties
   * as keys and defaults as values should be passed. If a single value is passed it will be
   * set to all keys.
   */
  defaultValue?: ValueType;

  /** Label to display for the field */
  label?: string;

  /** title of the field */
  title?: string;

  /** Hide the label even if the label property is defined */
  hideLabel?: boolean;

  /** Hint string to display */
  hint?: React.ReactNode;

  /** CSS class to use for the label */
  labelClass?: string;

  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string;

  /** CSS class to use for the field group */
  className?: string;

  /** Function rendering the children in the value <div>.
   * Takes two parameters:
   *
   * - *setValue*: a function to call when the value needs to be set in the model.
   *   This function takes a name and a value parameter.
   * - *onBlur*: a function to call when loosing the focus on the component.
   */
  children?: (arg0: {
    setValue: (name: string | undefined, value: ValueType) => void;
    onBlur: () => void;
  }) => React.ReactNode;

  /** Indicates whether the field is required in the form */
  required?: boolean;

  /** Indicates whether the field is disabled */
  disabled?: boolean;

  /** An array of validators to run against the input, either sync or async, resolve with `true` for valid & `false` for invalid */
  validators?: Validator | Validator[];

  /** Hint to display on a validation error */
  invalidHint?: React.ReactNode;

  /** Function to call when the data model needs to be changed.
   *  Takes a name and a value parameter.
   */
  onChange?: (name: string | undefined, value: ValueType) => void;
};

type State = {
  isValid: boolean;
  showErrors: boolean;

  /** Error messages received from FormContext
   *  (typically errors messages received from server response)
   */
  errors?: Array<string> | Object;
};

export class InputBase<ValueType = string> extends React.Component<InputBaseProps<ValueType>, State> {
  static defaultProps = {
    defaultValue: undefined,
    label: undefined,
    hint: undefined,
    labelClass: undefined,
    divClass: undefined,
    className: undefined,
    required: false,
    disabled: false,
    invalidHint: undefined,
    onChange: undefined,
  };

  constructor(props: InputBaseProps<ValueType>) {
    super(props);
    this.state = {
      isValid: true,
      showErrors: false,
      errors: undefined,
    };
  }

  componentDidMount() {
    if (this.props?.name) {
      if (this.context.registerInput != null) {
        this.context.registerInput(this);
      }

      const model = this.context.model || {};
      const checkValueChange = (name, defaultValue) => {
        // If we don't have a value yet but do have a defaultValue, set it on the model
        if (typeof model[name] === "undefined" && typeof defaultValue !== "undefined") {
          this.setValue(name, defaultValue);
          return;
        }

        /**
         * TODO: This is a bug and only holds for inputs where the value is a string, in dropdowns and the like, we should not fall back to "".
         * In addition, this doesn't allow setting defaultValue to falsey values, we should use ?? instead but this needs to be thoroughly tested.
         */
        const value = model[name] || defaultValue || "";
        const valueChanged =
          (value instanceof Date && model[name] instanceof Date && value.getTime() !== model[name].getTime()) ||
          value !== model[name];

        if (valueChanged) {
          this.setValue(name, value);
        }
      };
      if (this.props.name instanceof Array) {
        this.props.name.forEach((name) => {
          const defaultValue =
            this.props.defaultValue instanceof Object ? this.props.defaultValue[name] : this.props.defaultValue;
          checkValueChange(name, defaultValue);
        });
      } else {
        checkValueChange(this.props.name, this.props.defaultValue);
      }
    }
  }

  componentDidUpdate(prevProps) {
    // Support validation when changing the following props on-the-fly
    if (this.props.required !== prevProps.required || this.props.disabled !== prevProps.disabled) {
      const name = this.props.name;
      if (name instanceof Array) {
        const values = Object.keys(this.context.model).reduce((filtered, key) => {
          if (name.includes(key)) {
            filtered[key] = this.context.model[key];
          }
          return filtered;
        }, {});
        this.validate(values);
      } else if (typeof name !== "undefined") {
        this.validate(this.context.model[name]);
      }
    }
  }

  componentWillUnmount() {
    if (Object.keys(this.context).length > 0) {
      this.context.unregisterInput(this);
      if (this.props.name instanceof Array) {
        this.props.name.forEach((name) => this.context.setModelValue(name, undefined));
      } else {
        this.context.setModelValue(this.props.name, undefined);
      }
    }
  }

  onBlur = () => {
    this.setState({
      showErrors: true,
    });
  };

  isValid() {
    return this.state.isValid;
  }

  isEmptyValue(input: any) {
    if (typeof input === "string") {
      return input.trim() === "";
    }
    return _isNil(input);
  }

  /**
   * Validate the input, updating state and errors if necessary.
   *
   * The default case is for InputBase<ValueType = string>, but different inputs may use any type, for example an object when
   * `this.props.name` is an array. This makes inferring validation types tricky, so we accept whatever inputs make sense
   * for a given branch.
   */
  validate<InferredValueType = ValueType>(value: InferredValueType, errors?: Array<string> | Object): void {
    const results: ReturnType<Validator>[] = [];
    let isValid = true;

    if (Array.isArray(errors) && errors.length > 0) {
      isValid = false;
    }

    if (!this.props.disabled && (value || this.props.required)) {
      const noValue =
        this.isEmptyValue(value) ||
        (Array.isArray(this.props.name) && Object.values(value).filter((v) => !this.isEmptyValue(v)).length === 0);
      if (this.props.required && noValue) {
        isValid = false;
      } else if (this.props.validators) {
        const validators = Array.isArray(this.props.validators) ? this.props.validators : [this.props.validators];
        validators.forEach((v) => {
          results.push(Promise.resolve(v(value instanceof Object ? value : `${value || ""}`)));
        });
      }
    }

    Promise.all(results).then((result) => {
      result.forEach((r) => {
        isValid = isValid && r;
      });
      this.setState(
        (state) => ({
          isValid: isValid,
          errors: errors,
          showErrors: state.showErrors || (Array.isArray(errors) && errors.length > 0),
        }),
        () => {
          if (this.context.validateForm != null) {
            this.context.validateForm();
          }
        }
      );
    });
  }

  setValue = (name: string | undefined = undefined, value: ValueType) => {
    if (name && this.context.setModelValue != null) {
      this.context.setModelValue(name, value);
    }
    const propsName = this.props.name;
    if (propsName instanceof Array) {
      const values = Object.keys(this.context.model).reduce((filtered, key) => {
        if (propsName.includes(key)) {
          filtered[key] = this.context.model[key];
        }
        return filtered;
      }, {});
      if (name) {
        this.validate(Object.assign({}, values, { [name]: value }));
      } else {
        this.validate(Object.assign({}, values));
      }
    } else {
      this.validate(value);
    }

    if (this.props.onChange) this.props.onChange(name, value);
  };

  pushHint(hints: React.ReactNode[], hint: React.ReactNode) {
    if (hint) {
      if (hints.length > 0) {
        hints.push(<br />);
      }
      hints.push(hint);
    }
  }

  render() {
    const isError = this.state.showErrors && !this.state.isValid;
    const requiredHint = this.props.label ? t(`${this.props.label} is required.`) : t("required");
    const invalidHint = isError && (this.props.invalidHint || (this.props.required && requiredHint));
    const hints: React.ReactNode[] = [];
    this.pushHint(hints, this.props.hint);

    const errors = Array.isArray(this.state.errors) ? this.state.errors : this.state.errors ? [this.state.errors] : [];
    if (errors.length > 0) {
      errors.forEach((error) => this.pushHint(hints, error));
    } else {
      this.pushHint(hints, invalidHint);
    }

    return (
      <FormGroup isError={isError} key={`${this.props.name}-group`} className={this.props.className}>
        {this.props.label && (
          <Label
            name={this.props.label}
            className={this.props.labelClass}
            required={this.props.required}
            key={`${this.props.name}-label`}
            htmlFor={typeof this.props.name === "string" ? this.props.name : undefined}
          />
        )}
        <div className={this.props.divClass}>
          {this.props.children?.({
            setValue: this.setValue,
            onBlur: this.onBlur,
          })}
          {hints && <div className="help-block">{hints}</div>}
        </div>
      </FormGroup>
    );
  }
}
InputBase.contextType = FormContext;
