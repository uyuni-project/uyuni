import * as React from "react";
import { FieldProps } from "formik";
import InputBase, { InputBaseProps } from "./FormikInputBase";

type Props<Value> = InputBaseProps<Value> & {
  maxLength: number;
};

export const Text = <Value extends string>(props: Props<Value>) => {
  const { inputClass, ...propsToPass } = props;
  return (
    <InputBase {...propsToPass}>
      {({ field }: FieldProps<Value>) => (
        <input id={field.name} className={`form-control${props.inputClass ? ` ${props.inputClass}` : ""}`} {...field} />
      )}
    </InputBase>
  );
};

const defaultProps: Partial<Props<string>> = {
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
