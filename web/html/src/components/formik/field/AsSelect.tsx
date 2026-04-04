import { useField } from "formik";

import { Select } from "components/input";

import styles from "./AsSelect.module.scss";

export const AsSelect = (props: React.ComponentProps<typeof Select> & { name: string }) => {
  const [field, _, helper] = useField(props.name);

  return (
    <Select
      {...props}
      className={styles.container}
      value={field.value}
      // Formik expects `onChange` to fire an event that's the same shape as a browser native change event, so we can't pass `onChange` through directly
      onChange={(newValue) => helper.setValue(newValue)}
    />
  );
};
