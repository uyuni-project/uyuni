import * as React from "react";
import { useEffect, useState } from "react";

import _isEqual from "lodash/isEqual";
import ReactSelect from "react-select";
import AsyncSelect from "react-select/async";
import { AsyncPaginate as AsyncPaginateSelect } from "react-select-async-paginate";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { OptionType, SelectProps } from "./types";
import withCustomComponents from "./withCustomComponents";

const loadingMessage = () => t("Loading...");
const noOptionsMessage = () => t("No options");

export function Select<O extends OptionType, V>(props: SelectProps<O, V>) {
  const getOptionValue = (option) => {
    // Filter out null values so consumers don't have to worry about this edge case
    if (DEPRECATED_unsafeEquals(option, null)) {
      // This cast is safe because it can only ever happen when `isClearable` is true and `undefined` is an expected value
      return undefined as V;
    }
    if (props.getOptionValue) {
      return props.getOptionValue(option);
    }
    return option?.value as V;
  };

  const getOptionLabel = (option: O) => {
    if (DEPRECATED_unsafeEquals(option, null)) {
      return undefined;
    }
    if (props.getOptionLabel) {
      return props.getOptionLabel(option);
    }
    return option?.label;
  };

  // Make the component controlled. We actually only need this for the async cases, but it's simpler to keep it shared.
  const [value, setValue] = useState(() => {
    // For async, use the default preselected value if available
    if ("defaultValueOption" in props && props.defaultValueOption) {
      if (props.value !== getOptionValue(props.defaultValueOption)) {
        throw new RangeError("Select props `value` and `defaultValueOption` don't match");
      }

      return props.defaultValueOption;
    }

    if (props.isMulti) {
      return props.options?.filter((item) => (props.value as V[])?.includes(getOptionValue(item)));
    }
    // Otherwise find the right option from the given list
    return props.options?.find((item) => getOptionValue(item) === props.value) ?? undefined;
  });

  useEffect(() => {
    if (props.isMulti) {
      const newValue = props.options?.filter((item) => (props.value as V[])?.includes(getOptionValue(item))) ?? [];

      if (!_isEqual(value, newValue)) {
        setValue(newValue);
      }
    } else {
      const newValue = props.options?.find((item) => getOptionValue(item) === props.value) ?? undefined;

      if (!_isEqual(value, newValue)) {
        setValue(newValue);
      }
    }
  }, [props.value, props.options]);

  const bootstrapStyles = {
    control: (styles: Record<string, any>) => ({
      ...styles,
      minHeight: "34px",
      display: "flex",
    }),
    clearIndicator: (styles: Record<string, any>) => ({
      ...styles,
      padding: "2px 8px",
    }),
    dropdownIndicator: (styles: Record<string, any>) => ({
      ...styles,
      padding: "2px 8px",
    }),
    loadingIndicator: (styles: Record<string, any>) => ({
      ...styles,
      padding: "2px 8px",
    }),
    menu: (styles: Record<string, any>) => ({
      ...styles,
      zIndex: 3,
    }),
    menuPortal: (styles: Record<string, any>) => ({
      ...styles,
      zIndex: 9999,
    }),
  };

  // Common props to pass to both 'react-select' and 'react-select/async'
  const commonProps = Object.assign(
    {
      className: `form-control--react-select ${props.className ?? ""}`,
      name: props.name,
      inputId: props.name,
      isDisabled: props.disabled,
      onBlur: props.onBlur,
      onChange: (newValue) => {
        if (props.isMulti) {
          setValue(!DEPRECATED_unsafeEquals(newValue, null) ? newValue : []);
          props.onChange?.(newValue?.map((item) => getOptionValue(item)));
        } else {
          setValue(!DEPRECATED_unsafeEquals(newValue, null) ? newValue : undefined);
          props.onChange?.(getOptionValue(newValue));
        }
      },
      clearValue: () => undefined,
      formatOptionLabel: props.formatOptionLabel,
      placeholder: props.placeholder,
      isLoading: props.isLoading,
      loadingMessage,
      noOptionsMessage,
      isClearable: props.isClearable,
      styles: bootstrapStyles,
      isMulti: props.isMulti,
      menuPortalTarget: document.getElementById("menu-portal-target"),
      getOptionLabel,
      getOptionValue,
    },
    withCustomComponents(props["data-testid"], props.name)
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
