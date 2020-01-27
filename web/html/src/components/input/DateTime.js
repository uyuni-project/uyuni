// @flow

import React from 'react';
import { DateTimePicker } from '../datetimepicker';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  /** Time zone for the displayed time */
  timezone?: string,
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

export function DateTime(props: Props) {
  const {
    timezone,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase {...propsToPass}>
      {
        ({
          setValue,
        }) => {
          const onChange = (value) => {
            setValue(props.name, value);
          };
          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          if(fieldValue instanceof Date) {
            return (
              <DateTimePicker
                onChange={onChange}
                value={fieldValue}
                timezone={timezone}
              />
            );
          } else {
            return null
          }
        }
      }
    </InputBase>
  );
}

DateTime.defaultProps = {
  timezone: undefined,
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
