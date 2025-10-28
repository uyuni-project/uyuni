import * as React from "react";
import { useState } from "react";

import { ControlledInput } from "../ControlledInput";
import { FormContext } from "../form/Form";
import { InputBase, InputBaseProps } from "../InputBase";
import styles from "./Radio.module.scss";

type RadioOption = {
  /** The label of this option */
  label: React.ReactNode;
  /** The value to set when this option is selected */
  value: string;
  /** Specific title of this option */
  title?: string;
  /** true if this option is currently not selectable */
  disabled?: boolean;
};

type Props = InputBaseProps & {
  /** Items to display in an array of objects with label and value properties. */
  items: RadioOption[];

  /** Show the choices in a line or not */
  inline?: boolean;

  /** Whether to let the user input another value than the proposed ones */
  openOption?: boolean;

  /** CSS class for the <input> element */
  inputClass?: string;

  /** name of the field to map in the form model */
  name?: string;
};

export function Radio({ inline = false, openOption = false, required = false, disabled = false, ...props }: Props) {
  const [isPristine, setIsPristine] = useState(true);

  const { items, inputClass, ...propsToPass } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase required={required} disabled={disabled} {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (name, value) => {
          setValue(name, value);
          setIsPristine(false);
        };

        const fieldValue = formContext.model?.[props.name || ""] || props.defaultValue || "";
        const isOpenOption =
          openOption && !props.items.some((item) => item.value === fieldValue) && (fieldValue || !isPristine);

        const radioClass = inline ? "radio-inline" : "radio";
        return (
          <span className={styles.radio}>
            {props.items.map(({ label, value, title, disabled }) => (
              <label className={radioClass} key={`${props.name}_${value}`} title={title}>
                <ControlledInput
                  type="radio"
                  name={props.name}
                  title={`${props.title} ${value}`}
                  value={value}
                  checked={fieldValue === value}
                  className={inputClass}
                  onBlur={onBlur}
                  onChange={(event) => onChange(event.target.name, event.target.value)}
                  disabled={disabled ?? false}
                />
                {label}
              </label>
            ))}

            {openOption && (
              <div className={`radio ${styles["open_option_wrapper"]}`}>
                <label className={`radio-inline ${styles.open_option_wrapper_align_wrapper}`}>
                  <ControlledInput
                    className={styles.open_option_wrapper_align_content}
                    type="radio"
                    name={props.name}
                    checked={isOpenOption}
                    onChange={() => onChange(props.name, "")}
                  />
                  {"Other keyword: "}
                </label>
                <ControlledInput
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
