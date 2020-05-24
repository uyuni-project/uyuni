// @flow

import * as React from 'react';
import Select from 'react-select';
import {InputBase} from './InputBase';
import {FormContext} from './Form';

export type OptionType = {
  value: number | string,
  label: string
};

type Props = {
  /** options to display */
  options: Array<OptionType>,
  /** Value placeholder to display when no value is entered */
  placeholder?: string,
  /** whether the component's data is loading or not (async) */
  isLoading?: boolean,
  /** text to display when there are no options to list */
  emptyText: string | null,
  /** CSS class for the <input> element */
  inputClass?: string,
  /** name of the field to map in the form model */
  name: string,
  /** default value if none is set */
  defaultValue?: string,
  /** Label to display for the field */
  label?: string,
  /** hint string to display */
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

/** A simple, single select combo-box implementation using 'react-select' */
export default function Combo(props: Props) {

  const bootstrapStyles = {
    control: (styles: {}) => ({
      ...styles,
      minHeight: '34px',
      display: 'flex'
    }),
    clearIndicator: (styles: {}) => ({
      ...styles,
      padding: '2px 8px',
    }),
    dropdownIndicator: (styles: {}) => ({
      ...styles,
      padding: '2px 8px',
    }),
    loadingIndicator: (styles: {}) => ({
      ...styles,
      padding: '2px 8px',
    }),
    menu: (styles: {}) => ({
      ...styles,
      zIndex: 3,
    }),
  };

  const {
    options,
    placeholder,
    isLoading,
    emptyText,
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
          const onChange = (option: any, action: any) => {
            setValue(props.name, option.value);
          };
          const fieldValue = (formContext.model || {})[props.name] || props.defaultValue || '';
          return (
            <Select
              className={inputClass}
              name={props.name}
              options={props.options}
              isLoading={isLoading}
              isDisabled={props.disabled}
              value={props.options.find(o => o.value === fieldValue)}
              noOptionsMessage={() => emptyText}
              onBlur={onBlur}
              onChange={onChange}
              placeholder={placeholder}
              styles={bootstrapStyles}
            />
          );
        }
      }
    </InputBase>
  );
}

Combo.defaultProps = {
  placeholder: undefined,
  isLoading: false,
  emptyText: "No options",
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
