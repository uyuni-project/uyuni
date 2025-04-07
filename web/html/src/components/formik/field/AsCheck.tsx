import styles from "./AsCheck.module.scss";

export const AsCheck = (props: React.InputHTMLAttributes<HTMLInputElement>) => {
  // Formik uses the `value` field, native inputs use `checked`
  const { value, className, ...rest } = props;
  const checked = props.checked ?? !!value;
  return (
    <div className={styles.container}>
      <label className={styles.item} key={`${props.name}_${value}`}>
        <input {...rest} className="form-check-input" checked={checked} type="checkbox" />
      </label>
    </div>
  );
};
