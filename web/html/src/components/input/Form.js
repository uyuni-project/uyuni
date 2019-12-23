// @flow

import * as React from 'react';

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

type FormContextType = {
  model: Object,
  setModelValue: Function,
  registerInput: Function,
  unregisterInput: Function,
  validateForm: () => void,
};

export const FormContext = React.createContext<FormContextType>({});

export class Form extends React.Component<Props> {
  static defaultProps = {
    onSubmit: undefined,
    onSubmitInvalid: undefined,
    formRef: undefined,
    divClass: '',
    onValidate: undefined,
    formDirection: "form-horizontal"
  };

  inputs = {};

  setModelValue(name: string, value: any) {
    const { model } = this.props;
    if (value == null && model[name] != null) {
      delete model[name];
      this.props.onChange(model);
    } else if (value != null) {
      model[name] = value;
      this.props.onChange(model);
    }
  }

  allValid(): boolean {
    return Object.keys(this.inputs).every(name => this.inputs[name].isValid());
  }

  validateForm(): void {
    const valid = this.allValid();
    if (this.props.onValidate) {
      this.props.onValidate(valid);
    }
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
      Object.keys(this.inputs).forEach(name => this.inputs[name].validate(this.props.model[name]));
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
          validateForm: this.validateForm.bind(this),
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
