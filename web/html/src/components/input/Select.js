// @flow

import * as React from 'react';
import ReactSelect from 'react-select';
import { InputBase } from './InputBase';
import { FormContext } from './Form';

type Props = {
  /** Select options */
  options: Array<Object | string>,
  /** Resolves option data to a string to be displayed as the label by components */
  getOptionLabel: (option: Object) => string,
  /** Resolves option data to a string to compare options and specify value attributes */
  getOptionValue: (option: Object) => string,
  /** Formats option labels in the menu and control as React components */
  formatOptionLabel?: (option: Object, meta: Object) => React.Node,
  /** Placeholder for the select value */
  placeholder?: React.Node,
  /** Set to true to allow removing the selected value */
  isClearable: boolean,
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
    options,
    getOptionLabel,
    getOptionValue,
    formatOptionLabel,
    placeholder,
    isClearable,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  const convertedOptions = (options || []).map(item => typeof item === 'string' ? { label: item, value: item } : item);
  const defaultValue = convertedOptions.find(item => getOptionValue(item) === props.defaultValue);
  return (
    <InputBase {...propsToPass}>
      {
        ({
          setValue,
          onBlur,
        }) => {
          const onChange = (newValue) => {
            setValue(props.name, getOptionValue(newValue));
          };
          const value = (formContext.model || {})[props.name];
          const valueOption = convertedOptions.find(option => getOptionValue(option) === value);
          return (
            <ReactSelect
              className={inputClass ? ` ${inputClass}` : ''}
              name={props.name}
              id={props.name}
              isDisabled={props.disabled}
              defaultValue={defaultValue}
              value={valueOption}
              onBlur={onBlur}
              onChange={onChange}
              options={convertedOptions}
              getOptionLabel={(option) => option != null ? getOptionLabel(option) : ""}
              getOptionValue={(option) => option != null ? getOptionValue(option) : ""}
              formatOptionLabel={formatOptionLabel}
              placeholder={placeholder}
              isClearable={isClearable}
              styles={
                {menu: (provided) => ({...provided, zIndex: 3})}
              }
            />
          );
        }
      }
    </InputBase>
  );
}

Select.defaultProps = {
  isClearable: false,
  getOptionValue: (option) => option instanceof Object ? option.value : option,
  getOptionLabel: (option) => option instanceof Object ? option.label : option,
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
