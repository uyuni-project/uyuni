import * as React from "react";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

type Props = InputBaseProps & {
  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;
};

export function Check(props: Props) {
  const { label, inputClass, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);

  return (
    <InputBase {...propsToPass}>
      {({ setValue, onBlur }) => {
        const setChecked = (event: any) => {
          setValue(event.target.name, event.target.checked);
        };
        const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || "";
        return (
          <div className="checkbox">
            <label htmlFor={props.name}>
              <input
                id={props.name}
                className={inputClass}
                name={props.name}
                type="checkbox"
                checked={fieldValue}
                onChange={setChecked}
                onBlur={onBlur}
                disabled={props.disabled}
              />
              <span>{label}</span>
            </label>
          </div>
        );
      }}
    </InputBase>
  );
}

Check.defaultProps = {
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
