// @flow
import * as React from 'react';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  /** Value placeholder to display when no value is entered */
  placeholder?: string,
  /** CSS class for the <input> element */
  inputClass?: string,
  /** name prefix of the field to map in the form model.
   * The two values will be named `${prefix}_start` and `${prefix}_end` */
  prefix: string,
  /** Default value for start if none is set */
  defaultStart?: string,
  /** Default value for end if none is set */
  defaultEnd?: string,
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

export const Range = (props: Props) => {
  const {
    placeholder,
    inputClass,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  return (
    <InputBase name={[`${props.prefix}_start`, `${props.prefix}_end`]} {...propsToPass}>
      {
        ({
           setValue,
           onBlur,
         }) => {
          const onChange = (event: Object) => {
            setValue(event.target.name, event.target.value);
          };
          const startValue = (formContext.model || {})[`${props.prefix}_start`] || props.defaultStart || '';
          const endValue = (formContext.model || {})[`${props.prefix}_end`] || props.defaultEnd || '';
          return (
            <div className="input-group">
                <input
                  className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
                  type={'text'}
                  name={`${props.prefix}_start`}
                  value={startValue}
                  onChange={onChange}
                  disabled={props.disabled}
                  onBlur={onBlur}
                  placeholder={placeholder}
                  aria-label={t(`${props.label || ''} start`)}
                  title={t(`${props.label || ''} start`)}
                />
                <span className="input-group-addon">
                    -
                </span>
                <input
                  className={`form-control${inputClass ? ` ${inputClass}` : ''}`}
                  type={'text'}
                  name={`${props.prefix}_end`}
                  value={endValue}
                  onChange={onChange}
                  disabled={props.disabled}
                  onBlur={onBlur}
                  placeholder={placeholder}
                  aria-label={t(`${props.label || ''} end`)}
                  title={t(`${props.label || ''} end`)}
                />
            </div>
          );
        }
      }
    </InputBase>
  )};

Range.defaultProps = {
  placeholder: undefined,
  inputClass: undefined,
  defaultStart: undefined,
  defaultEnd: undefined,
  label: undefined,
  hint: undefined,
  labelClass: undefined,
  divClass: undefined,
  required: false,
  disabled: false,
  invalidHint: undefined,
  onChange: undefined,
};

