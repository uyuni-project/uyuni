import { useContext } from "react";

import { ControlledInput } from "../ControlledInput";
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
  const {
    type = "text",
    required = false,
    disabled = false,
    maxLength,
    placeholder,
    inputClass,
    ...propsToPass
  } = props;
  const formContext = useContext(FormContext);
  return (
    <InputBase required={required} disabled={disabled} {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (event: any) => {
          const value = type === "file" ? (event.target.files?.[0]?.name ?? event.target.value) : event.target.value;
          setValue(event.target.name, value);
        };
        const fieldValue = formContext.model?.[props.name] || props.defaultValue || "";
        // File inputs cannot have their value set programmatically (browser security restriction),
        // so we render them as uncontrolled and derive the model value from the selected file name.
        if (type === "file") {
          return (
            <input
              className={`form-control${inputClass ? ` ${inputClass}` : ""}`}
              type="file"
              name={props.name}
              id={props.name}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              title={props.title}
              required={required}
            />
          );
        }
        return (
          <ControlledInput
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
            autoComplete={props.autoComplete}
            required={required}
          />
        );
      }}
    </InputBase>
  );
};
