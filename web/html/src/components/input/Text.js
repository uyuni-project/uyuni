// @flow
import * as React from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  type: string,
  placeholder?: string,
  inputClass?: string,
} & InputBase.Props;

export const Text = (props: Props) => {
  const {
    type,
    placeholder,
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
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          return (
            <input
              className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
              type={type || 'text'}
              name={props.name}
              value={fieldValue}
              onChange={onChange}
              disabled={props.disabled}
              onBlur={onBlur}
              placeholder={placeholder}
            />
          );
        }
      }
    </InputBase>
  )};

Text.defaultProps = Object.assign({
  type: 'text',
  placeholder: undefined,
  inputClass: undefined,
}, InputBase.defaultProps);
