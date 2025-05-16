import * as React from "react";

import ReactSelect from "react-select";
import AsyncSelect from "react-select/async";
import { AsyncPaginate as AsyncPaginateSelect } from "react-select-async-paginate";

import withTestAttributes from "./select-test-attributes";

type SingleMode = {
  value?: string;

  onChange?: (newValue: string | undefined) => void;

  /** Set to true to allow multiple selected values */
  isMulti?: false;
};

type MultiMode = {
  value?: string[];

  onChange?: (newValue: string[]) => void;

  /** Set to true to allow multiple selected values */
  isMulti: true;
};

type CommonSelectProps = (SingleMode | MultiMode) & {
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

  inputClass?: string;

  /** Id for testing purposes */
  "data-testid"?: string;

  label?: string;

  name?: string;

  disabled?: boolean;
};

type SelectProps = CommonSelectProps & {
  /** Select options */
  options: { value: string; label: string }[];
};

type AsyncSelectProps = Omit<CommonSelectProps, "value"> & {
  // 'value' is currently not supported with the async Select
  // because string => Object value conversion is not possible with dynamic options

  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: Object;

  paginate?: boolean;

  /**
   * Function that returns a promise, which is the set of options to be used once the promise resolves.
   */
  loadOptions: (searchString: string, callback: (options: Array<Object>) => undefined) => Promise<any> | undefined;
  cacheOptions?: boolean;
};
type AsyncPaginateSelectProps = Omit<CommonSelectProps, "value"> & {
  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: Object;

  paginate: true;
  /**
   * Function that returns a promise with pagination data and a set of options matching the search string
   * See: https://github.com/vtaits/react-select-async-paginate/tree/master/packages/react-select-async-paginate#loadoptions
   */
  loadOptions: (
    searchString: string,
    previouslyLoaded: any[],
    additional?: any
  ) => Promise<{ options: any[]; hasMore: boolean; additional?: any }>;
};

type Props = SelectProps | AsyncSelectProps | AsyncPaginateSelectProps;

export function Select(props: Props) {
  const isAsync = (props: Props): props is AsyncSelectProps | AsyncPaginateSelectProps => {
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

  // Common props to pass to both 'react-select' and 'react-select/async'
  const commonProps = Object.assign(
    {
      className: `form-control--react-select ${props.inputClass ?? ""}`,
      name: props.name,
      inputId: props.name,
      isDisabled: props.disabled,
      // TODO: Fix
      // onBlur: props.onBlur,
      onChange: (newValue) => {
        if (props.isMulti) {
          props.onChange?.(newValue?.map((item) => item.value) ?? undefined);
        } else {
          props.onChange?.(newValue?.value ?? undefined);
        }
      },
      formatOptionLabel: props.formatOptionLabel,
      placeholder: props.placeholder,
      isLoading: props.isLoading,
      noOptionsMessage: () => props.emptyText,
      isClearable: props.isClearable,
      styles: bootstrapStyles,
      isMulti: props.isMulti,
      menuPortalTarget: document.getElementById("menu-portal-target"),
    },
    withTestAttributes(props["data-testid"], props.name)
  );

  if (isAsync(props)) {
    if (props.paginate) {
      return (
        <AsyncPaginateSelect
          loadOptions={props.loadOptions}
          defaultOptions
          aria-label={props.label}
          defaultValue={defaultValueOption}
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
        aria-label={props.label}
        defaultValue={defaultValueOption}
        {...commonProps}
      />
    );
  } else {
    const value = props.options.find((item) => item.value === props.value);
    return (
      <ReactSelect
        options={props.options}
        value={value}
        defaultValue={value}
        aria-label={props.label}
        {...commonProps}
      />
    );
  }
}

// TODO: Review all of these and remove what we no longer use
Select.defaultProps = {
  isClearable: false,
  isLoading: false,
  emptyText: t("No options"),
  inputClass: undefined,
  label: undefined,
  hint: undefined,
  labelClass: undefined,
  disabled: false,
  isMulti: false,
  cacheOptions: false,
};
