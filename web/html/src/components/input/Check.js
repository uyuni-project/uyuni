// @flow

import React from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
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

Check.defaultProps = {
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
