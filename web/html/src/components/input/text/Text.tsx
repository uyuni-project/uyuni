import * as React from "react";

import { FormContext } from "../form/Form";
import { InputBase, InputBaseProps } from "../InputBase";

type Props = InputBaseProps & {
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
            id={props.name}
            value={fieldValue}
            onChange={onChange}
            disabled={props.disabled}
            onBlur={onBlur}
            placeholder={placeholder}
            maxLength={maxLength}
            title={props.title}
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
  className: undefined,
  required: false,
  disabled: false,
  invalidHint: undefined,
  onChange: undefined,
};
