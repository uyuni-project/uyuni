import * as React from "react";
import { Field, FieldProps, FieldAttributes, FieldConfig } from "formik";
import { FormGroup } from "./FormGroup";
import { Label } from "./Label";

type Props = FieldAttributes<unknown> & {
  /** CSS class for the <input> element */
  inputClass?: string;
};

function isCallable(input: any): input is (...args: any[]) => any {
  return typeof input === "function";
}

const FieldWrapper = <Value extends unknown>(props: Props) => {
  const { label, labelClass, divClass, ...propsToPass } = props;
  return (
    <Field {...propsToPass}>
      {(fieldProps: FieldProps<Value>) => {
        // TODO: Check
        const isError = !!fieldProps.form.errors[fieldProps.field.name];
        // TODO: Implement
        const hints = null;
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
              {hints && <div className="help-block">{hints}</div>}
            </div>
          </FormGroup>
        );
      }}
    </Field>
  );
};

export const Text = <Value extends string>(props: Props) => {
  const { inputClass, ...propsToPass } = props;
  return (
    <FieldWrapper {...propsToPass}>
      {({
        field, // { name, value, onChange, onBlur }
        form: { touched, errors }, // also values, setXXXX, handleXXXX, dirty, isValid, status, etc.
        meta,
      }: FieldProps<Value>) => (
        <div>
          <input
            id={field.name}
            className={`form-control${props.inputClass ? ` ${props.inputClass}` : ""}`}
            {...field}
          />
          {meta.touched && meta.error && <div className="error">{meta.error}</div>}
        </div>
      )}
    </FieldWrapper>
  );
};

const defaultProps: Partial<Props> = {
  type: "text",
  maxLength: undefined,
  placeholder: undefined,
  inputClass: undefined,
  defaultValue: undefined,
  // TODO: Fix these
  // label: undefined,
  // hint: undefined,
  // labelClass: undefined,
  // divClass: undefined,
  className: undefined,
  required: false,
  disabled: false,
  // invalidHint: undefined,
  onChange: undefined,
};

Text.defaultProps = defaultProps;
