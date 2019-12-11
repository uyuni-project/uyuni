// @flow

import React from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  inputClass?: string,
} & InputBase.Props;

export function Check(props: Props) {
  const {
    label,
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
          const setChecked = (event: Object) => {
            setValue(event.target.name, event.target.checked);
          };
          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          return (
            <div className="checkbox">
              <label htmlFor={props.name}>
                <input
                  id={props.name}
                  className={inputClass}
                  name={props.name}
                  type="checkbox"
                  checked={fieldValue}
                  onChange={setChecked}
                  onBlur={onBlur}
                  disabled={props.disabled}
                />
                <span>{label}</span>
              </label>
            </div>
          );
        }
      }
    </InputBase>
  );
}

Check.defaultProps = Object.assign({
  inputClass: undefined,
}, InputBase.defaultProps);
