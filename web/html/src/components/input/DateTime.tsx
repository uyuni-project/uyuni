import * as React from "react";
import { DateTimePicker } from "../datetimepicker";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

type Props = InputBaseProps & {
  /** Time zone for the displayed time */
  timezone?: string;

  /** name of the field to map in the form model */
  name: string;
};

export function DateTime(props: Props) {
  const { timezone, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {({ setValue }) => {
        const onChange = value => {
          setValue(props.name, value);
        };
        const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || "";
        if (fieldValue instanceof Date) {
          return <DateTimePicker onChange={onChange} value={fieldValue} timezone={timezone} />;
        } else {
          return null;
        }
      }}
    </InputBase>
  );
}

DateTime.defaultProps = {
  timezone: undefined,
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
