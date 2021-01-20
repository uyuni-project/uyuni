// @flow

import * as React from 'react';

type Props = {
  /** Object storing the data of the form.
   *  Each field name in the form needs to map to a property of this
   *  object. The value is the one displayed in the form */
  model: Object,
  /** Object storing form field errors */
  errors: Object,
  /** Function to trigger when the Submit button is clicked */
  onSubmit?: Function,
  /** Function to trigger when the Submit button is clicked while the model is invalid */
  onSubmitInvalid?: Function,
  /** A reference to pass to the <form> element */
  formRef?: string,
  /** CSS class of the form */
  className?: string,
  /** CSS class of the div right within the form */
  divClass?: string,
  /** CSS class name for the form direction style */
  formDirection?: string,
  /** Children elements of the form. Usually includes fields and a submit button */
  children: React.Node,
  /** Function called when the model has been changed.
   * Takes a new model as single parameter.
   */
  onChange: (model: Object) => void,
  /** Function called after having validated the form.
   * Takes a single parameter indicating whether the form is valid or not.
   */
  onValidate?: (valid: boolean) => void,
};

type FormContextType = {
  model: Object,
  errors: Object,
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
    const { model, errors } = this.props;
    if (value == null && model[name] != null) {
      delete model[name];
      this.props.onChange(model);
    } else if (value != null) {
      model[name] = value;
      this.props.onChange(model);
    }
    if (errors) {
      delete errors[name];
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

  getComponentName(component: React.ElementRef<any>) {
    return Array.isArray(component.props.name) ? component.props.name.join() : component.props.name;
  }

  splitComponentName(name: string) {
    return name.split(',');
  }

  unregisterInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name){
      const name = this.getComponentName(component);
      if (this.inputs[name] === component) {
        delete this.inputs[name];
      }
    }
  }

  registerInput(component: React.ElementRef<any>) {
    if (component.props && component.props.name) {
      const name = this.getComponentName(component);
      this.inputs[name] = component;
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
    if (prevProps.model !== this.props.model ||
        prevProps.errors !== this.props.errors) {
      Object.keys(this.inputs).forEach(name => {
        const names = this.splitComponentName(name);
        if (names.length === 1) {
          this.inputs[name].validate(this.props.model[name], this.props.errors && this.props.errors[name])
        } else {
          const values = Object.keys(this.props.model).reduce((filtered, key) => {
            if (names.includes(key)) {
              filtered[key] = this.props.model[key];
            }
            return filtered;
          }, {});
          const errors = Object.keys(this.props.errors).reduce((filtered, key) => {
            if (names.includes(key)) {
              return filtered.concat(this.props.errors[key]);
            }
            return filtered;
          }, []);
          this.inputs[name].validate(values, errors);
        }
      });
    }
  }

  render() {
    return (
      <FormContext.Provider value={
        {
          model: this.props.model,
          errors: this.props.errors,
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
