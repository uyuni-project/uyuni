// @flow

import React from 'react';
import { useState } from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

import styles from './Radio.css';

type Props = {
  /** Items to display in an array of objects with label and value properties. */
  items: Array<{label: string, value: string}>,
  /** Show the choices in a line or not */
  inline?: boolean,
  /** Whether to let the user input another value than the proposed ones */
  openOption?: boolean,
  /** CSS class for the <input> element */
  inputClass?: string,
  /** name of the field to map in the form model */
  name: string,
  /** Default value if none is set */
  defaultValue?: string,
  /** Label to display for the field */
  label?: string,
  /** Hint string to display */
  hint?: string,
  /** CSS class to use for the label */
  labelClass?: string,
  /** CSS class to use for the <div> element wrapping the field input part */
  divClass?: string,
  /** Indicates whether the field is required in the form */
  required?: boolean,
  /** Indicates whether the field is disabled */
  disabled?: boolean,
  /** Hint to display on a validation error */
  invalidHint?: string,
  /** Function to call when the data model needs to be changed.
   *  Takes a name and a value parameter.
   */
  onChange?: (name: string, value: string) => void,
};

export function Radio(props: Props) {
  const [isPristine, setIsPristine] = useState(true);

  const {
    items,
    inputClass,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {
        ({
           setValue,
           onBlur,
         }) => {

          const onChange = (name, value) => {
            setValue(name, value);
            setIsPristine(false);
          };

          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          const isOpenOption = props.openOption
            && !props.items.some(item => item.value === fieldValue)
            && (fieldValue || !isPristine);

          const radioClass = props.inline ? "radio-inline" : "radio";
          return (
            <span className={styles.radio}>
              {
                props.items.map(({label, value}) =>
                  <label className={radioClass}>
                    <input
                      type="radio"
                      name={props.name}
                      value={value}
                      checked={fieldValue === value}
                      className={inputClass}
                      onBlur={onBlur}
                      onChange={event => onChange(event.target.name, event.target.value)} />
                    {label}
                  </label>
                )
              }

              {
                props.openOption &&
                <div className={`radio ${styles['open_option_wrapper']}`}>
                  <label className={`radio-inline ${styles.open_option_wrapper_align_wrapper}`}>
                    <input
                      className={styles.open_option_wrapper_align_content}
                      type="radio"
                      name={props.name}
                      checked={isOpenOption}
                      onChange={() => onChange(props.name, '')}
                    />
                    {"Other keyword: "}
                  </label>
                  <input
                    name={props.name}
                    type="text"
                    disabled={!isOpenOption}
                    value={isOpenOption ? fieldValue : ''}
                    onChange={event => onChange(event.target.name, event.target.value)}
                  />
                </div>
              }
              </span>
          );
        }
      }
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
