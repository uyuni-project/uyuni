import { useContext } from "react";

import { FormContext } from "../form/Form";
import { InputBase, InputBaseProps } from "../InputBase";

type Props = InputBaseProps & {
  /** Number of rows */
  rows?: number;

  /** Number of columns */
  cols?: number;

  /** Value placeholder to display when no value is entered */
  placeholder?: string;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;
};

export const TextArea = (props: Props) => {
  const { required = false, disabled = false, rows, cols, placeholder, inputClass, ...propsToPass } = props;
  const formContext = useContext(FormContext);
  return (
    <InputBase required={required} disabled={disabled} {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (event: any) => {
          setValue(event.target.name, event.target.value);
        };
        const fieldValue = formContext.model?.[props.name] || props.defaultValue || "";
        return (
          <textarea
            className={"form-control " + (inputClass ?? "")}
            name={props.name}
            id={props.name}
            value={fieldValue}
            onChange={onChange}
            disabled={props.disabled}
            onBlur={onBlur}
            placeholder={placeholder}
            rows={rows}
            cols={cols}
            title={props.title}
            required={required}
          />
        );
      }}
    </InputBase>
  );
};
