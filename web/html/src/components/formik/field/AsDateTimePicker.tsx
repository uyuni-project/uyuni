import { useField } from "formik";

import { DateTimePicker } from "components/datetime";

import { LocalizedMoment } from "utils/datetime";

interface BaseProps<ValueType> {
  name: string;
  value: ValueType;
}

type Props = BaseProps<LocalizedMoment> & React.ComponentProps<typeof DateTimePicker>;

export const AsDateTimePicker = (props: Props) => {
  const [field, meta, helper] = useField(props.name);
  return (
    <DateTimePicker
      {...props}
      value={props.value}
      // Formik expects `onChange` to fire an event that's the same shape as a browser native change event, so we can't pass `onChange` through directly
      onChange={(newValue) => helper.setValue(newValue)}
    />
  );
};
