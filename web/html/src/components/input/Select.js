// @flow

import * as React from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  /** <option> elements to display */
  children: React.Node,
  /** Value placeholder to display when no value is entered */
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

export function Select(props: Props) {
  const {
    inputClass,
    children,
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
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          return (
            <select
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              name={props.name}
              disabled={props.disabled}
              value={fieldValue}
              onBlur={onBlur}
              onChange={onChange}
            >
              {children}
            </select>
          );
        }
      }
    </InputBase>
  );
}

Select.defaultProps = {
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
