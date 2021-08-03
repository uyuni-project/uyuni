import * as React from "react";
import { Field, FieldProps, FieldAttributes } from "formik";
import _isNil from "lodash/isNil";

import { FormGroup } from "./FormGroup";
import { Label } from "./Label";

type SimpleValidator<Value> = (value: Value) => boolean | Promise<boolean>;
type FormikValidator<Value> = (value: Value) => void | string | Promise<void | string>;

export type InputBaseProps<Value> = Omit<FieldAttributes<unknown>, "validate"> & {
  /** CSS class for the <input> element */
  inputClass?: string;

  /** Label to display for the field */
  label?: string;

  /** CSS class to use for the label */
  labelClass?: string;

  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string;

  /** Indicates whether the field is required in the form */
  required?: boolean;

  /** An array of validators to run against the input, either sync or async, resolve with `true` for valid & `false` for invalid */
  validators?: SimpleValidator<Value> | SimpleValidator<Value>[];

  /** Hint to display on a validation error */
  invalidHint?: string;
};

function isCallable(input: any): input is (...args: any[]) => any {
  return typeof input === "function";
}

const isEmptyValue = (input: any) => {
  if (typeof input === "string") {
    return input.trim() === "";
  }
  return _isNil(input);
};

const requiredValidator = <Value,>(label?: string) => {
  return (input: Value) => {
    if (isEmptyValue(input)) {
      return label ? t(`${label} is required.`) : t("required");
    }
  };
};

const InputBase = <Value,>(props: InputBaseProps<Value>) => {
  const { label, labelClass, divClass, invalidHint, ...propsToPass } = props;

  const validators: FormikValidator<Value>[] = [];
  if (props.required) {
    validators.push(requiredValidator(props.label));
  }

  if (props.validators) {
    const simpleValidators = Array.isArray(props.validators) ? props.validators : [props.validators];
    const mergedValidator = async (input: Value) => {
      const results = await Promise.all(simpleValidators.map(item => item(input)));
      if (results.some(result => result === false)) {
        return invalidHint || "Not valid";
      }
    };
    validators.push(mergedValidator);
  }

  const validate = async (input: Value) => {
    const results = await Promise.all(validators.map(validator => validator(input)));
    // Show the first error we find by default, usually this will be the required validation
    return results.filter(Boolean)[0];
  };

  return (
    <Field {...propsToPass} validate={validate}>
      {(fieldProps: FieldProps<Value>) => {
        const isError = Boolean(fieldProps.meta.touched && fieldProps.meta.error);
        return (
          <FormGroup isError={isError} key={`${props.name}-group`} className={props.className}>
            {props.label && (
              <Label
                name={props.label}
                className={props.labelClass}
                required={props.required}
                key={`${props.name}-label`}
                htmlFor={typeof props.name === "string" ? props.name : undefined}
              />
            )}
            <div className={props.divClass}>
              {isCallable(props.children) ? props.children(fieldProps) : props.children}
              {isError && <div className="help-block">{fieldProps.meta.error}</div>}
            </div>
          </FormGroup>
        );
      }}
    </Field>
  );
};

export default InputBase;
