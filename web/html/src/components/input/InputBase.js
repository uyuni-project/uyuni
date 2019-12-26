import * as React from 'react';

import { Label } from './Label';
import { FormGroup } from './FormGroup';
import { FormContext } from './Form';

export type Props = {
  /** name of the field to map in the form model */
  name: string,
  /** Default value if none is set */
  defaultValue?: string,
  /** Label to display for the field */
  label?: string,
  /** Hint string to display */
  hint?: string,
  /** CSS class to use for the label */
  labelClass?: string,
  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string,
  /** Function rendering the children in the value <div>.
   * Takes two parameters:
   *
   * - *setValue*: a function to call when the value needs to be set in the model.
   *   This function takes a name and a value parameter.
   * - *onBlur*: a function to call when loosing the focus on the component.
   */
  children: ({
    setValue: (name: string, value: string) => void,
    onBlur: () => void,
  }) => React.Node,
  /** Indicates whether the field is required in the form */
  required?: boolean,
  /** Indicates whether the field is disabled */
  disabled?: boolean,
  /** Hint to display on a validation error */
  invalidHint?: string,
  /** Function to call when the data model needs to be changed.
   *  Takes a name and a value parameter.
   */
  onChange?: (name: string, value: string) => void,
};

type State = {
  isValid: boolean,
  showErrors: boolean,
};

export class InputBase extends React.Component<Props, State> {
  static defaultProps = {
    defaultValue: undefined,
    label: undefined,
    hint: undefined,
    labelClass: undefined,
    divClass: undefined,
    required: false,
    disabled: false,
    invalidHint: undefined,
    onChange: undefined,
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      isValid: true,
      showErrors: false,
    };
  }

  componentDidMount() {
    if (this.props && this.props.name) {
      if (this.context.registerInput != null) {
        this.context.registerInput(this);
      }

      const model = this.context.model || {};
      const value = model[this.props.name] || this.props.defaultValue || '';
      const valueChanged =
        (value instanceof Date && model[this.props.name] instanceof Date
          && value.getTime() !== model[this.props.name].getTime())
        || value !== model[this.props.name];

      if (valueChanged) {
        this.setValue(this.props.name, value);
      }
    }
  }

  componentWillUnmount() {
    if (Object.keys(this.context).length > 0) {
      this.context.unregisterInput(this);
      this.context.setModelValue(this.props.name, undefined);
    }
  }

  onBlur() {
    this.setState({
      showErrors: true,
    });
  }

  isValid() {
    return this.state.isValid;
  }

  validate (value: string): void {
    const results = [];
    let isValid = true;

    if (!this.props.disabled && (value || this.props.required)) {
      if (this.props.required && !value) {
        isValid = false;
      } else if (this.props.validators) {
        const validators = Array.isArray(this.props.validators) ? this.props.validators : [this.props.validators];
        validators.forEach((v) => {
          results.push(Promise.resolve(v(`${value || ''}`)));
        });
      }
    }

    Promise.all(results).then((result) => {
      result.forEach((r) => {
        isValid = isValid && r;
      });
      this.setState({isValid}, () => {
        if (this.context.validateForm != null) {
          this.context.validateForm();
        }
      });
    });
  }

  setValue(name: string, value: string) {
    if (this.context.setModelValue != null) {
      this.context.setModelValue(name, value);
    }
    this.validate(value);

    if (this.props.onChange) this.props.onChange(name, value);
  }

  render() {
    const isError = this.state.showErrors && !this.state.isValid;
    const invalidHint = isError && (
      this.props.invalidHint || (this.props.required && (`${this.props.label} is required.`))
    );
    const hint = [this.props.hint, (invalidHint && this.props.hint && <br />), invalidHint];
    return (
      <FormGroup isError={isError}>
        {
          this.props.label
          && (
            <Label
              name={this.props.label}
              className={this.props.labelClass}
              required={this.props.required}
              htmlFor={this.props.name}
            />)
        }
        <div className={this.props.divClass}>
          {
            this.props.children({
              setValue: this.setValue.bind(this),
              onBlur: this.onBlur.bind(this),
            })
          }
          { hint
            && (
              <div className="help-block">
                {hint}
              </div>
            )
          }
        </div>
      </FormGroup>
    );
  }
}
InputBase.contextType = FormContext;
