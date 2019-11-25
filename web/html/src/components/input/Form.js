// @flow

const React = require('react');

type Props = {
  model: Object,
  onSubmit?: Function,
  onSubmitInvalid?: Function,
  formRef?: string,
  className?: string,
  divClass?: string,
  formDirection?: string,
  children: React.Node,
  onChange: Function,
  onValidate?: Function,
};

type State = {
  inputsValidationStatus: Object,
};

type FormContextType = {
  model: Object,
  setModelValue: Function,
  onFieldValidation: (string, boolean) => void,
  registerInput: Function,
  unregisterInput: Function,
};

const FormContext = React.createContext<FormContextType>({});

class Form extends React.Component<Props, State> {
  static defaultProps = {
    onSubmit: undefined,
    onSubmitInvalid: undefined,
    formRef: undefined,
    divClass: '',
    onValidate: undefined,
    formDirection: "form-horizontal"
  };

  inputs = {};

  constructor(props: Props) {
    super(props);
    this.state = {
      inputsValidationStatus: {}
    };
  }

  setModelValue(name: string, value: any) {
    const { model } = this.props;
    if (value == null && model[name] != null) {
      delete model[name];
      this.validateForm(model);
      this.props.onChange(model);
    } else if (value != null) {
      model[name] = value;
      this.validateForm(model);
      this.props.onChange(model);
    }
  }

  allValid(): boolean {
    return Object.values(this.state.inputsValidationStatus).every(status => status);
  }

  validateForm(model: Object) {
    const inputsValidationStatus = Object.keys(this.inputs).reduce((ret, name) => {
      ret[name] = this.inputs[name].validate(model);
      return ret;
    }, {});

    this.setState({
      inputsValidationStatus,
    }, () => {
      const validForm = this.allValid();
      if (this.props.onValidate != null) {
        this.props.onValidate(validForm);
      }
    });
  }

  onFieldValidation(name: string, isValid: boolean) {
    this.setState(state => {
      let { inputsValidationStatus } = state;
      inputsValidationStatus[name] = isValid;
      return { inputsValidationStatus };
    },
    () => {
      const validForm = this.allValid();
      if (this.props.onValidate) {
        this.props.onValidate(validForm);
      }
    });
  }

  unregisterInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name && this.inputs[component.props.name] === component) {
      delete this.inputs[component.props.name];
    }
  }

  registerInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name) {
      this.inputs[component.props.name] = component;
    } else {
      throw new Error('Can not add input without "name" attribute');
    }
  }

  submit(event: Object) {
    event.preventDefault();
    if (this.allValid() && this.props.onSubmit) {
      this.props.onSubmit(this.props.model, event);
    } else if (this.props.onSubmitInvalid) {
      this.props.onSubmitInvalid(this.props.model, event);
    }
  }

  componentDidUpdate(prevProps: Object) {
    if (prevProps.model !== this.props.model) {
      this.validateForm(this.props.model);
    }
  }

  render() {
    return (
      <FormContext.Provider value={
        {
          model: this.props.model,
          setModelValue: this.setModelValue.bind(this),
          registerInput: this.registerInput.bind(this),
          unregisterInput: this.unregisterInput.bind(this),
          onFieldValidation: this.onFieldValidation.bind(this),
        }
      }>
        <form ref={this.props.formRef} onSubmit={this.submit.bind(this)} className={this.props.className}>
          <div className={`${this.props.formDirection || ''} ${this.props.divClass ? ` ${this.props.divClass}` : ''}`}>
            {this.props.children}
          </div>
        </form>
      </FormContext.Provider>
    );
  }
}

module.exports = {
  Form,
  FormContext,
};
