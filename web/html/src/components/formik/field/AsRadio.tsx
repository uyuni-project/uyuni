import { useState } from "react";

import styles from "./AsRadio.module.scss";

type Props = React.InputHTMLAttributes<HTMLInputElement> & {
  /** Items to display in an array of objects with label and value properties. */
  items: { label: React.ReactNode; value: string }[];

  /** Show the choices in a line or not */
  inline?: boolean;

  /** Whether to let the user input another value than the proposed ones */
  openOption?: boolean;
};

export const AsRadio = (props: Props) => {
  const [openOptionValue, setOpenOptionValue] = useState("");
  const { id, className, items, openOption, inline, ...rest } = props;

  return (
    <div className={`${styles.container} ${inline ? styles.inline : ""}`}>
      {props.items.map(({ label, value }) => (
        <label className={styles.item} key={`${props.name}_${value}`}>
          <input
            {...rest}
            className="form-check-input"
            title={value}
            value={value}
            checked={value === props.value}
            type="radio"
            id={`${id}_${value}`}
          />
          <div>{label}</div>
        </label>
      ))}
      {openOption ? (
        <label className={styles.item} key="open-option">
          <input
            {...rest}
            className="form-check-input"
            title={t("Custom value")}
            value={openOptionValue}
            checked={openOptionValue === props.value}
            type="radio"
          />
          <input
            name={props.name}
            value={openOptionValue}
            onChange={(event) => {
              if (openOptionValue === props.value) {
                props.onChange?.(event);
              }
              setOpenOptionValue(event.target.value);
            }}
            className="form-control"
          />
        </label>
      ) : null}
    </div>
  );
};
