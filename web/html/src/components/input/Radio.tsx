import * as React from "react";
import { useState } from "react";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

import styles from "./Radio.css";

type Props = InputBaseProps & {
  /** Items to display in an array of objects with label and value properties. */
  items: Array<{ label: React.ReactNode; value: string }>;

  /** Show the choices in a line or not */
  inline?: boolean;

  /** Whether to let the user input another value than the proposed ones */
  openOption?: boolean;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name?: string;
};

export function Radio(props: Props) {
  const [isPristine, setIsPristine] = useState(true);

  const { items, inputClass, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (name, value) => {
          setValue(name, value);
          setIsPristine(false);
        };

        const fieldValue = (formContext.model || {})[props.name || ""] || props.defaultValue || "";
        const isOpenOption =
          props.openOption && !props.items.some((item) => item.value === fieldValue) && (fieldValue || !isPristine);

        const radioClass = props.inline ? "radio-inline" : "radio";
        return (
          <span className={styles.radio}>
            {props.items.map(({ label, value }) => (
              <label className={radioClass} key={`${props.name}_${value}`}>
                <input
                  type="radio"
                  name={props.name}
                  title={`${props.title} ${value}`}
                  value={value}
                  checked={fieldValue === value}
                  className={inputClass}
                  onBlur={onBlur}
                  onChange={(event) => onChange(event.target.name, event.target.value)}
                />
                {label}
              </label>
            ))}

            {props.openOption && (
              <div className={`radio ${styles["open_option_wrapper"]}`}>
                <label className={`radio-inline ${styles.open_option_wrapper_align_wrapper}`}>
                  <input
                    className={styles.open_option_wrapper_align_content}
                    type="radio"
                    name={props.name}
                    checked={isOpenOption}
                    onChange={() => onChange(props.name, "")}
                  />
                  {"Other keyword: "}
                </label>
                <input
                  name={props.name}
                  type="text"
                  disabled={!isOpenOption}
                  value={isOpenOption ? fieldValue : ""}
                  onChange={(event) => onChange(event.target.name, event.target.value)}
                  title={t(`${props.title} other`)}
                />
              </div>
            )}
          </span>
        );
      }}
    </InputBase>
  );
}

Radio.defaultProps = {
  inline: false,
  openOption: false,
  inputClass: undefined,
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
