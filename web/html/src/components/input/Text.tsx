import * as React from "react";
import { InputBase } from "./InputBase";
import { FormContext } from "./Form";

type Props = {
  /** <input> type */
  type?: string;

  /** Maximum number of characters of the input field */
  maxLength?: number;

  /** Value placeholder to display when no value is entered */
  placeholder?: string;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;

  /** Default value if none is set */
  defaultValue?: string;

  /** Label to display for the field */
  label?: string;

  /** Hint string to display */
  hint?: React.ReactNode;

  /** CSS class to use for the label */
  labelClass?: string;

  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string;

  /** Indicates whether the field is required in the form */
  required?: boolean;

  /** Indicates whether the field is disabled */
  disabled?: boolean;

  /** Hint to display on a validation error */
  invalidHint?: React.ReactNode;

  /** Function to call when the data model needs to be changed.
   *  Takes a name and a value parameter.
   */
  onChange?: (name: string, value: string) => void;
};

export const Text = (props: Props) => {
  const { type, maxLength, placeholder, inputClass, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (event: any) => {
          setValue(event.target.name, event.target.value);
        };
        const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || "";
        return (
          <input
            className={`form-control${inputClass ? ` ${inputClass}` : ""}`}
            type={type || "text"}
            name={props.name}
            value={fieldValue}
            onChange={onChange}
            disabled={props.disabled}
            onBlur={onBlur}
            placeholder={placeholder}
            maxLength={maxLength}
          />
        );
      }}
    </InputBase>
  );
};

Text.defaultProps = {
  type: "text",
  maxLength: undefined,
  placeholder: undefined,
  inputClass: undefined,
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
