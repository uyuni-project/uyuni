import * as React from "react";
import ReactSelect from "react-select";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

type Props = InputBaseProps<string | string[]> & {
  /** Select options */
  options: Array<Object | string>;

  /** Resolves option data to a string to be displayed as the label by components */
  getOptionLabel: (option: any) => string;

  /** Resolves option data to a string to compare options and specify value attributes */
  getOptionValue: (option: any) => string;

  /** Formats option labels in the menu and control as React components */
  formatOptionLabel?: (option: any, meta: any) => React.ReactNode;

  /** Placeholder for the select value */
  placeholder?: React.ReactNode;

  /** whether the component's data is loading or not (async) */
  isLoading?: boolean;

  /** text to display when there are no options to list */
  emptyText: string | null;

  /** Set to true to allow removing the selected value */
  isClearable: boolean;

  /** Set to true to allow multiple selected values */
  isMulti: boolean;

  /** Value placeholder to display when no value is entered */
  inputClass?: string;

  /** name of the field to map in the form model */
  name: string;
};

export function Select(props: Props) {
  const {
    inputClass,
    options,
    getOptionLabel,
    getOptionValue,
    formatOptionLabel,
    placeholder,
    isLoading,
    emptyText,
    isClearable,
    ...propsToPass
  } = props;
  const formContext = React.useContext(FormContext);
  const convertedOptions = (options || []).map(item =>
    typeof item === "string" ? { label: item, value: item } : item
  );
  const defaultValue = convertedOptions.find(item => getOptionValue(item) === props.defaultValue);

  const bootstrapStyles = {
    control: (styles: {}) => ({
      ...styles,
      minHeight: "34px",
      display: "flex",
    }),
    clearIndicator: (styles: {}) => ({
      ...styles,
      padding: "2px 8px",
    }),
    dropdownIndicator: (styles: {}) => ({
      ...styles,
      padding: "2px 8px",
    }),
    loadingIndicator: (styles: {}) => ({
      ...styles,
      padding: "2px 8px",
    }),
    menu: (styles: {}) => ({
      ...styles,
      zIndex: 3,
    }),
  };

  return (
    <InputBase<string | string[]> {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = newValue => {
          const value = Array.isArray(newValue) ? newValue.map(item => getOptionValue(item)) : getOptionValue(newValue);
          setValue(props.name, value);
        };
        const value = (formContext.model || {})[props.name];
        const optionFinder = needle => convertedOptions.find(option => getOptionValue(option) === needle);
        const valueOption = Array.isArray(value) ? value.map(item => optionFinder(item)) : optionFinder(value);
        return (
          <ReactSelect
            className={inputClass ? ` ${inputClass}` : ""}
            name={props.name}
            id={props.name}
            isDisabled={props.disabled}
            defaultValue={defaultValue}
            value={valueOption}
            onBlur={onBlur}
            onChange={onChange}
            options={convertedOptions}
            getOptionLabel={option => (option != null ? getOptionLabel(option) : "")}
            getOptionValue={option => (option != null ? getOptionValue(option) : "")}
            formatOptionLabel={formatOptionLabel}
            placeholder={placeholder}
            isLoading={isLoading}
            noOptionsMessage={() => emptyText}
            isClearable={isClearable}
            styles={bootstrapStyles}
            isMulti={props.isMulti}
          />
        );
      }}
    </InputBase>
  );
}

Select.defaultProps = {
  isClearable: false,
  getOptionValue: option => (option instanceof Object ? option.value : option),
  getOptionLabel: option => (option instanceof Object ? option.label : option),
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
  isMulti: false,
};
