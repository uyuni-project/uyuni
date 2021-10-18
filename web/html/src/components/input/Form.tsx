import * as React from "react";
import { InputBase } from "./InputBase";

type InputBaseRef = React.ElementRef<typeof InputBase>;

type Props = {
  /** Object storing the data of the form.
   *  Each field name in the form needs to map to a property of this
   *  object. The value is the one displayed in the form */
  model: any;

  /** Object storing form field errors */
  errors?: object;

  /** Function to trigger when the Submit button is clicked */
  onSubmit?: Function;

  /** A reference to pass to the <form> element */
  formRef?: React.LegacyRef<HTMLFormElement>;

  /** CSS class of the form */
  className?: string;

  /** CSS class of the div right within the form */
  divClass?: string;

  /** CSS class name for the form direction style */
  formDirection?: string;

  /** Children elements of the form. Usually includes fields and a submit button */
  children: React.ReactNode;

  /** Function called when the model has been changed.
   * Takes a new model as single parameter.
   */
  onChange?: (model: any) => void;

  /** Function called after having validated the form.
   * Takes a single parameter indicating whether the form is valid or not.
   */
  onValidate?: (valid: boolean) => void;

  /** Accessible title of the form */
  title?: string;
};

type FormContextType = {
  model: any;
  errors: any;
  setModelValue: (name: string, value: any) => any;
  registerInput: Function;
  unregisterInput: Function;
  validateForm: () => void;
};

export const FormContext = React.createContext<Partial<FormContextType>>({});

export class Form extends React.Component<Props> {
  static defaultProps = {
    onSubmit: undefined,
    formRef: undefined,
    divClass: "",
    onValidate: undefined,
    formDirection: "form-horizontal",
  };

  inputs: { [key: string]: InputBaseRef | undefined } = {};

  setModelValue = (name: string, value: any) => {
    const { model, errors } = this.props;
    if (value == null && model[name] != null) {
      delete model[name];
      this.props.onChange?.(model);
    } else if (value != null) {
      model[name] = value;
      this.props.onChange?.(model);
    }
    if (errors) {
      delete errors[name];
    }

    // Usually, fields validate themselves bottom up, in this case we pass values top down so we need to validate that way as well
    this.inputs[name]?.validate(value);
  };

  allValid(): boolean {
    return Object.keys(this.inputs).every((name) => this.inputs[name]?.isValid());
  }

  validateForm = () => {
    const valid = this.allValid();
    if (this.props.onValidate) {
      this.props.onValidate(valid);
    }
  };

  getComponentName(component: InputBaseRef) {
    return Array.isArray(component.props.name) ? component.props.name.join() : component.props.name;
  }

  splitComponentName(name: string) {
    return name.split(",");
  }

  unregisterInput = (component: InputBaseRef) => {
    if (component.props && component.props.name) {
      const name = this.getComponentName(component);
      if (typeof name !== "undefined" && this.inputs[name] === component) {
        delete this.inputs[name];
      }
    }
  };

  registerInput = (component: InputBaseRef) => {
    if (component.props && component.props.name) {
      const name = this.getComponentName(component);
      if (typeof name !== "undefined") {
        this.inputs[name] = component;
      }
    }
  };

  submit = (event: any) => {
    event.preventDefault();
    if (this.allValid() && this.props.onSubmit) {
      this.props.onSubmit(this.props.model, event);
    }
  };

  componentDidUpdate(prevProps: any) {
    if (prevProps.model !== this.props.model || prevProps.errors !== this.props.errors) {
      Object.keys(this.inputs).forEach((name) => {
        const names = this.splitComponentName(name);
        if (names.length === 1) {
          this.inputs[name]?.validate(this.props.model[name], this.props.errors && this.props.errors[name]);
        } else {
          const values = Object.keys(this.props.model).reduce((filtered, key) => {
            if (names.includes(key)) {
              filtered[key] = this.props.model[key];
            }
            return filtered;
          }, {});
          const errors = Object.keys(this.props.errors || {}).reduce((filtered, key) => {
            if (names.includes(key) && this.props.errors?.[key]) {
              return filtered.concat(this.props.errors[key]);
            }
            return filtered;
          }, []);
          this.inputs[name]?.validate(values, errors);
        }
      });
    }
  }

  render() {
    return (
      <FormContext.Provider
        value={{
          model: this.props.model,
          errors: this.props.errors,
          /**
           * NB! Please don't use `.bind()` here, children may rely on these functions in `useEffect()` calls.
           * Binding here will create a new reference on every render which will in turn retrigger dependency tracking.
           * Instead of using `.bind()` here, implicitly bind any methods you need here in the class declaration:
           *
           *  class Foo {
           *    method = () => {...}
           *    // ----^
           *  }
           *
           */
          setModelValue: this.setModelValue,
          registerInput: this.registerInput,
          unregisterInput: this.unregisterInput,
          validateForm: this.validateForm,
        }}
      >
        <form ref={this.props.formRef} onSubmit={this.submit} className={this.props.className} title={this.props.title}>
          <div className={`${this.props.formDirection || ""} ${this.props.divClass ? ` ${this.props.divClass}` : ""}`}>
            {this.props.children}
          </div>
        </form>
      </FormContext.Provider>
    );
  }
}
