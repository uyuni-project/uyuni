import * as React from "react";
import { useEffect, useState } from "react";

import _isEqual from "lodash/isEqual";
import ReactSelect from "react-select";
import AsyncSelect from "react-select/async";
import { AsyncPaginate as AsyncPaginateSelect } from "react-select-async-paginate";

import withTestAttributes from "./select-test-attributes";
import { SelectOption, SelectProps } from "./types";

const loadingMessage = () => t("Loading...");

export function Select<T extends SelectOption, V>(props: SelectProps<T, V>) {
  // Make the component controlled. We actually only need this for the async cases, but it's simpler to keep it shared.
  const [value, setValue] = useState(() => {
    // For async, use the default preselected value if available
    if ("defaultValueOption" in props && props.defaultValueOption) {
      if (props.value !== props.getOptionValue(props.defaultValueOption)) {
        throw new RangeError("Select props `value` and `defaultValueOption` don't match");
      }

      return props.defaultValueOption;
    }

    // Otherwise find the right option from the given list
    if ("options" in props) {
      return props.options.find((item) => props.getOptionValue(item) === props.value);
    }

    return undefined;
  });

  useEffect(() => {
    if (_isEqual(value, props.value)) {
      return;
    }
    setValue(value);
  }, [props.value]);

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

  // Common props to pass to both 'react-select' and 'react-select/async'
  const commonProps = Object.assign(
    {
      className: `form-control--react-select ${props.inputClass ?? ""}`,
      name: props.name,
      inputId: props.name,
      isDisabled: props.disabled,
      // TODO: Do we need this?
      // onBlur: props.onBlur,
      onChange: (newValue) => {
        // console.log(newValue);
        setValue(newValue != null ? newValue : undefined);
        if (props.isMulti) {
          props.onChange?.(newValue?.map((item) => props.getOptionValue(item)));
        } else {
          props.onChange?.(props.getOptionValue(newValue) ?? undefined);
        }
      },
      formatOptionLabel: props.formatOptionLabel,
      placeholder: props.placeholder,
      isLoading: props.isLoading,
      loadingMessage,
      noOptionsMessage: () => props.emptyText,
      isClearable: props.isClearable,
      styles: bootstrapStyles,
      isMulti: props.isMulti,
      menuPortalTarget: document.getElementById("menu-portal-target"),
      getOptionLabel: (option) => (option != null ? props.getOptionLabel(option) : undefined),
      getOptionValue: (option) => (option != null ? props.getOptionValue(option) : undefined),
    },
    withTestAttributes(props["data-testid"], props.name)
  );

  if ("loadOptions" in props) {
    if (props.paginate) {
      return (
        <AsyncPaginateSelect
          loadOptions={props.loadOptions}
          defaultOptions
          value={value}
          defaultValue={value}
          aria-label={props.label}
          shouldLoadMore={(scrollHeight, clientHeight, scrollTop) => {
            // Load more items before we hit the complete bottom of the dropdown
            const threshold = 200; //px
            return scrollHeight - clientHeight - scrollTop < threshold;
          }}
          {...commonProps}
        />
      );
    }

    return (
      <AsyncSelect
        loadOptions={props.loadOptions}
        cacheOptions={props.cacheOptions}
        defaultOptions
        value={value}
        defaultValue={value}
        aria-label={props.label}
        {...commonProps}
      />
    );
  }

  return (
    <ReactSelect options={props.options} value={value} defaultValue={value} aria-label={props.label} {...commonProps} />
  );
}

Select.defaultProps = {
  getOptionValue: (option: any) => option?.value ?? undefined,
  getOptionLabel: (option: any) => option?.label ?? undefined,
  isClearable: false,
  isLoading: false,
  emptyText: t("No options"),
  inputClass: undefined,
  label: undefined,
  disabled: false,
  isMulti: false,
  cacheOptions: false,
};
