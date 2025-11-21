import { type ReactNode, useContext } from "react";

import { ControlledInput } from "../ControlledInput";
import { FormContext } from "../form/Form";
import { InputBase, InputBaseProps } from "../InputBase";

type Props = Omit<InputBaseProps, "label"> & {
  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;

  /** for historical reasons, label handles differently here  */
  label: ReactNode;
};

/**
 * @deprecated For standalone components use `import { Check } from "components/input";`, for Formik forms use `<Field as={Field.Check} />`.
 */
export function DEPRECATED_Check({ required = false, disabled = false, ...props }: Props) {
  const { label, inputClass, ...propsToPass } = props;
  const formContext = useContext(FormContext);

  return (
    <InputBase required={required} disabled={disabled} {...propsToPass}>
      {({ setValue, onBlur }) => {
        const setChecked = (event: any) => {
          setValue(event.target.name, event.target.checked);
        };
        const fieldValue = formContext.model?.[props.name] || props.defaultValue || "";
        return (
          <div className="checkbox">
            <label htmlFor={props.name}>
              <ControlledInput
                id={props.name}
                className={inputClass}
                name={props.name}
                type="checkbox"
                checked={fieldValue}
                onChange={setChecked}
                onBlur={onBlur}
                disabled={disabled}
                title={props.title}
              />
              <span>{label}</span>
            </label>
          </div>
        );
      }}
    </InputBase>
  );
}
