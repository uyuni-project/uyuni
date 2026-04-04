import { useContext } from "react";

import { DateTimePicker } from "components/datetime";

import { localizedMoment } from "utils";

import { FormContext } from "../form/Form";
import { InputBase, InputBaseProps } from "../InputBase";

type Props = InputBaseProps<moment.Moment> & {
  /** name of the field to map in the form model */
  name: string;
};

export function DateTime({ required = false, disabled = false, ...props }: Props) {
  const formContext = useContext(FormContext);
  return (
    <InputBase<moment.Moment> required={required} disabled={disabled} {...props}>
      {({ setValue }) => {
        const onChange = (value: moment.Moment) => {
          setValue(props.name, value);
        };
        const fieldValue = formContext.model?.[props.name] || props.defaultValue || undefined;
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
