// @flow

import React from 'react';
import { useState } from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

import styles from './Radio.css';

type Props = {
  items: Array<{label: string, value: string}>,
  inline?: boolean,
  openOption?: boolean,
  inputClass?: string,
} & InputBase.Props;

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

          const fieldValue = formContext.model[props.name] || props.defaultValue || '';
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

Radio.defaultProps = Object.assign({
  inputClass: undefined,
}, InputBase.defaultProps);
