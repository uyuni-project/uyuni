// @flow

import React from 'react';
import { DateTimePicker } from '../datetimepicker';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  timezone?: string,
} & InputBase.Props;

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
          const fieldValue = formContext.model[props.name] || props.defaultValue || '';
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

DateTime.defaultProps = Object.assign({
  timezone: undefined,
}, InputBase.defaultProps);
