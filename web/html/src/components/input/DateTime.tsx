import * as React from "react";
import { DateTimePicker } from "components/datetime";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";
import { localizedMoment } from "utils";

type Props = InputBaseProps<moment.Moment> & {
  /** name of the field to map in the form model */
  name: string;
};

export function DateTime(props: Props) {
  const formContext = React.useContext(FormContext);
  return (
    <InputBase<moment.Moment> {...props}>
      {({ setValue }) => {
        const onChange = (value: moment.Moment) => {
          setValue(props.name, value);
        };
        const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || undefined;
        // TODO: Double-check what's reasonable here
        const isValid = fieldValue?.isValid?.() ?? true;
        if (fieldValue && isValid) {
          return <DateTimePicker onChange={onChange} value={localizedMoment(fieldValue)} />;
        } else {
          return null;
        }
      }}
    </InputBase>
  );
}

DateTime.defaultProps = {
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
