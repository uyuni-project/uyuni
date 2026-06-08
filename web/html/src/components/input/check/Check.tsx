import { useEffect, useRef } from "react";

import styles from "./Check.module.scss";

const DEFAULT_INPUT_CLASS = "form-check-input";

export type CheckProps = Omit<React.InputHTMLAttributes<HTMLInputElement>, "onChange"> & {
  indeterminate?: boolean;
  onChange?: (value: boolean) => void;
  // Skip the <label> wrapper; lets custom checks (e.g. ProductCheck) opt out of the default layout.
  noLabel?: boolean;
  // Override the <input> className; defaults to "form-check-input" so existing callers are unaffected.
  inputClass?: string;
};

export const Check = (props: CheckProps) => {
  const ref = useRef<HTMLInputElement>(null);
  const { indeterminate, noLabel, className, inputClass = DEFAULT_INPUT_CLASS, ...rest } = props;

  useEffect(() => {
    if (ref.current) {
      ref.current.indeterminate = Boolean(indeterminate);
    }
  }, [indeterminate]);

  const input = (
    <input
      {...rest}
      className={inputClass}
      checked={props.checked}
      onChange={(event) => props.onChange?.(event.target.checked)}
      type="checkbox"
      ref={ref}
    />
  );

  return noLabel ? input : <label className={`${styles.check} ${className ?? ""}`}>{input}</label>;
};
