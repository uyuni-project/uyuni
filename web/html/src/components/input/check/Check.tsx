import styles from "./Check.module.scss";
import { type CheckInputProps, CheckInput } from "./CheckInput";

const DEFAULT_INPUT_CLASS = "form-check-input";

export type CheckProps = CheckInputProps & {
  // Override the <input> className; defaults to "form-check-input" so existing callers are unaffected.
  inputClass?: string;
  label?: string;
};

export const Check = ({ className, inputClass = DEFAULT_INPUT_CLASS, label, ...rest }: CheckProps) => (
  <label className={`${styles.check} ${className ?? ""}`}>
    <CheckInput {...rest} className={inputClass} />
    {label && <span>{label}</span>}
  </label>
);
