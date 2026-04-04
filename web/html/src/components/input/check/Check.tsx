import { useEffect, useRef } from "react";

import styles from "./Check.module.scss";

export type CheckProps = Omit<React.InputHTMLAttributes<HTMLInputElement>, "value" | "onChange"> & {
  indeterminate?: boolean;
  onChange?: (value: boolean) => void;
};

export const Check = (props: CheckProps) => {
  const ref = useRef<HTMLInputElement>(null);

  const { indeterminate, ...rest } = props;

  useEffect(() => {
    if (typeof indeterminate !== "undefined" && ref.current) {
      ref.current.indeterminate = indeterminate;
    }
  }, [indeterminate]);

  return (
    <label className={`${styles.check} ${props.className ?? ""}`}>
      <input
        {...rest}
        className="form-check-input"
        checked={props.checked}
        onChange={(event) => props.onChange?.(event.target.checked)}
        type="checkbox"
        ref={ref}
      />
    </label>
  );
};
