import { useField } from "formik";

import { Check, CheckProps } from "components/input";

import styles from "./AsCheck.module.scss";

type Props = CheckProps & {
  name: string;
};

export const AsCheck = (props: Props) => {
  const [field, _, helper] = useField(props.name);

  return (
    <div className={styles.container}>
      <Check
        {...props}
        checked={field.value}
        // Formik expects `onChange` to fire an event that's the same shape as a browser native change event, so we can't pass `onChange` through directly
        onChange={(newValue) => helper.setValue(newValue)}
      />
    </div>
  );
};
