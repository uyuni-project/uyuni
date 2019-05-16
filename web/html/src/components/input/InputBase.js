const React = require('react');

const { Label } = require('./Label');
const { FormGroup } = require('./FormGroup');

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
  onFormChange?: Function,
  registerInput?: Function,
  unregisterInput?: Function,
  validate?: Function,
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
    onFormChange: undefined,
    registerInput: undefined,
    unregisterInput: undefined,
    validate: undefined,
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      isValid: true,
      showErrors: false,
    };
  }

  // eslint-disable-next-line
  UNSAFE_componentWillMount() {
    if (this.props.registerInput) {
      this.props.registerInput(this);
    }
  }

  // eslint-disable-next-line
  UNSAFE_componentWillReceiveProps(nextProps) {
    if (!(nextProps.value === this.props.value && nextProps.disabled === this.props.disabled
              && nextProps.required === this.props.required)) {
      if (this.props.validate) this.props.validate(this, nextProps);
      this.setState({
        showErrors: false,
      });
    }
  }

  componentWillUnmount() {
    if (this.props.unregisterInput) {
      this.props.unregisterInput(this);
    }
  }

  onBlur() {
    this.setState({
      showErrors: true,
    });
  }

  setValue(name: string, value: string) {
    if (this.props.onFormChange) {
      this.props.onFormChange({
        name,
        value,
      });
    }

    if (this.props.onChange) this.props.onChange(name, value);
  }

  render() {
    const isError = this.state.showErrors && !this.state.isValid;
    const invalidHint = isError && this.props.invalidHint;
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

module.exports = {
  InputBase,
};
