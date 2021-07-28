import * as React from "react";
import { Field, FieldProps, FieldAttributes } from "formik";

type Props = FieldAttributes<unknown> & {
  /** CSS class for the <input> element */
  inputClass?: string;
};

export const Text = <Value extends string>(props: Props) => {
  const { inputClass, ...propsToPass } = props;
  return (
    <Field {...propsToPass}>
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
    </Field>
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