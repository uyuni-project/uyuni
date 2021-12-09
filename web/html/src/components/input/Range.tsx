import * as React from "react";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

type Props = InputBaseProps & {
  /** Value placeholder to display when no value is entered */
  placeholder?: string;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name prefix of the field to map in the form model.
   * The two values will be named `${prefix}_start` and `${prefix}_end` */
  prefix: string;

  /** Default value for start if none is set */
  defaultStart?: string;

  /** Default value for end if none is set */
  defaultEnd?: string;
};

export const Range = (props: Props) => {
  const { placeholder, inputClass, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass} name={[`${props.prefix}_start`, `${props.prefix}_end`]}>
      {({ setValue, onBlur }) => {
        const onChange = (event: any) => {
          setValue(event.target.name, event.target.value);
        };
        const startValue = (formContext.model || {})[`${props.prefix}_start`] || props.defaultStart || "";
        const endValue = (formContext.model || {})[`${props.prefix}_end`] || props.defaultEnd || "";
        return (
          <div className="input-group">
            <input
              className={`form-control${inputClass ? ` ${inputClass}` : ""}`}
              type={"text"}
              name={`${props.prefix}_start`}
              value={startValue}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              placeholder={placeholder}
              aria-label={t(`${props.label || ""} start`)}
              title={t(`${props.title || ""} start`)}
            />
            <span className="input-group-addon">-</span>
            <input
              className={`form-control${inputClass ? ` ${inputClass}` : ""}`}
              type={"text"}
              name={`${props.prefix}_end`}
              value={endValue}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              placeholder={placeholder}
              aria-label={t(`${props.label || ""} end`)}
              title={t(`${props.title || ""} end`)}
            />
          </div>
        );
      }}
    </InputBase>
  );
};

Range.defaultProps = {
  placeholder: undefined,
  inputClass: undefined,
  defaultStart: undefined,
  defaultEnd: undefined,
  label: undefined,
  hint: undefined,
  labelClass: undefined,
  divClass: undefined,
  required: false,
  disabled: false,
  invalidHint: undefined,
  onChange: undefined,
};
