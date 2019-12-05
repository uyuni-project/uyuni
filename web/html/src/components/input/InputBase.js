const React = require('react');

const { Label } = require('./Label');
const { FormGroup } = require('./FormGroup');
const { FormContext } = require('./Form');

export type Props = {
  name: string,
  // eslint-disable-next-line
  defaultValue?: string,
  label?: string,
  hint?: string,
  labelClass?: string,
  divClass?: string,
  value: string,
  children: ({
    setValue: (name: string, value: string) => void,
    onBlur: () => void,
  }) => React.Node,
  required?: boolean,
  disabled?: boolean,
  invalidHint?: string,
  onChange?: (name: string, value: string) => void,
};

type State = {
  isValid: boolean,
  showErrors: boolean,
};

class InputBase extends React.Component<Props, State> {
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
      this.context.registerInput(this);

      const { model } = this.context;
      const value = this.context.model[this.props.name] || this.props.defaultValue || '';
      const valueChanged =
        (value instanceof Date && model[this.props.name] instanceof Date
          && value.getTime() !== model[this.props.name].getTime())
        || value !== model[this.props.name];

      if (valueChanged) {
        model[this.props.name] = value;
        this.context.setModelValue(this.props.name, value);
      }
    }
  }

  componentWillUnmount() {
    this.context.unregisterInput(this);
    this.context.setModelValue(this.props.name, undefined);
  }

  onBlur() {
    this.setState({
      showErrors: true,
    });
  }

  validate(model: Object): boolean {
    const { name } = this.props;
    const results = [];
    let isValid = true;

    if (!this.props.disabled && (model[name] || this.props.required)) {
      if (this.props.required && !model[name]) {
        isValid = false;
      } else if (this.props.validators) {
        const validators = Array.isArray(this.props.validators) ? this.props.validators : [this.props.validators];
        validators.forEach((v) => {
          results.push(Promise.resolve(v(`${model[name] || ''}`)));
        });
      }
    }

    Promise.all(results).then((result) => {
      result.forEach((r) => {
        isValid = isValid && r;
      });
      this.setState({isValid});
      this.context.onFieldValidation(name, isValid);
    });

    this.setState({
      isValid,
    });
    return isValid;
  }

  setValue(name: string, value: string) {
    this.context.setModelValue(name, value);

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

module.exports = {
  InputBase,
};
