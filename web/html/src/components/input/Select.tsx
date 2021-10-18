import * as React from "react";
import { useEffect } from "react";
import ReactSelect from "react-select";
import AsyncSelect from "react-select/async";
import { InputBase, InputBaseProps } from "./InputBase";
import { FormContext } from "./Form";

type SingleMode = InputBaseProps<string> & {
  /** Set to true to allow multiple selected values */
  isMulti?: false;

  /** Resolves option data to a string to compare options and specify value attributes */
  getOptionValue: (option: any) => string;
};

type MultiMode = InputBaseProps<string | string[]> & {
  /** Set to true to allow multiple selected values */
  isMulti: true;

  /** Resolves option data to a string to compare options and specify value attributes */
  getOptionValue: (option: any) => string | string[];
};

type CommonSelectProps = (SingleMode | MultiMode) & {
  /** Resolves option data to a string to be displayed as the label by components */
  getOptionLabel: (option: any) => string;

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

  /** Value placeholder to display when no value is entered */
  inputClass?: string;

  /** name of the field to map in the form model */
  name?: string;
};

type SelectProps = CommonSelectProps & {
  /** Select options */
  options: Array<Object | string>;
};

type AsyncSelectProps = Omit<CommonSelectProps, "value" | "defaultValue"> & {
  // 'value' and 'defaultValue' are not currently supported with the async Select
  // because string => Object value conversion is not possible with dynamic options

  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: Object;

  /**
   * Function that returns a promise, which is the set of options to be used once the promise resolves.
   */
  loadOptions: (inputValue: string, callback: (options: Array<Object>) => undefined) => Promise<any> | undefined;
  cacheOptions?: boolean;
};

export function Select(props: SelectProps | AsyncSelectProps) {
  const {
    inputClass,
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
  const isAsync = (props: SelectProps | AsyncSelectProps): props is AsyncSelectProps => {
    return (props as AsyncSelectProps).loadOptions !== undefined;
  };

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
    menuPortal: (styles: {}) => ({
      ...styles,
      zIndex: 9999,
    }),
  };

  let defaultValueOption;
  if (isAsync(props)) {
    defaultValueOption = props.defaultValueOption;
  }
  useEffect(() => {
    const value = (formContext.model || {})[props.name || ""];
    // Since defaultValueOption is not bound to the model, ensure sanity
    if (isAsync(props) && typeof defaultValueOption !== "undefined" && getOptionValue(defaultValueOption) !== value) {
      console.error(
        `Mismatched defaultValueOption for async select for form field "${props.name}": expected ${getOptionValue(
          defaultValueOption
        )}, got ${value}`
      );
    }
  }, []);

  // TODO: This `any` should be inferred based on the props instead, currently the props expose the right interfaces but we don't have strict checks here
  return (
    <InputBase<any> {...propsToPass}>
      {({ setValue, onBlur }) => {
        const onChange = (newValue) => {
          const value = Array.isArray(newValue)
            ? newValue.map((item) => getOptionValue(item))
            : getOptionValue(newValue);
          setValue(props.name, value);
        };
        const value = (formContext.model || {})[props.name || ""];

        // Common props to pass to both 'react-select' and 'react-select/async'
        const commonProps = {
          className: inputClass ? ` ${inputClass}` : "",
          name: props.name,
          inputId: props.name,
          isDisabled: props.disabled,
          onBlur: onBlur,
          onChange: onChange,
          getOptionLabel: (option) => (option != null ? getOptionLabel(option) : ""),
          getOptionValue: (option) => (option != null ? getOptionValue(option) : ""),
          formatOptionLabel: formatOptionLabel,
          placeholder: placeholder,
          isLoading: isLoading,
          noOptionsMessage: () => emptyText,
          isClearable: isClearable,
          styles: bootstrapStyles,
          isMulti: props.isMulti,
          menuPortalTarget: document.body,
          classNamePrefix: `class-${props.name}`,
        };

        if (isAsync(props)) {
          return (
            <AsyncSelect
              loadOptions={props.loadOptions}
              cacheOptions={props.cacheOptions}
              defaultOptions
              aria-label={props.title}
              defaultValue={defaultValueOption}
              {...commonProps}
            />
          );
        } else {
          const convertedOptions = (props.options || []).map((item) =>
            typeof item === "string" ? { label: item, value: item } : item
          );
          const defaultValue = convertedOptions.find((item) => getOptionValue(item) === props.defaultValue);
          const optionFinder = (needle) => convertedOptions.find((option) => getOptionValue(option) === needle);
          const valueOption = Array.isArray(value) ? value.map((item) => optionFinder(item)) : optionFinder(value);

          return (
            <ReactSelect
              options={convertedOptions}
              value={valueOption ?? defaultValue ?? null}
              defaultValue={defaultValue}
              aria-label={props.title}
              {...commonProps}
            />
          );
        }
      }}
    </InputBase>
  );
}

Select.defaultProps = {
  isClearable: false,
  getOptionValue: (option) => (option instanceof Object ? option.value : option),
  getOptionLabel: (option) => (option instanceof Object ? option.label : option),
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
  cacheOptions: false,
};
