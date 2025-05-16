/** Usually options have the shape `{ value: string; label:string }`, but the consumer can define any shape */
export type SelectOption = { value: string; label: string } | Record<string, unknown>;

type SingleValue<V> = {
  value?: V;

  onChange?: (newValue: V | undefined) => void;

  /** Set to true to allow multiple selected values */
  isMulti?: never;
};

type MultipleValue<V> = {
  value?: V;

  onChange?: (newValue: V) => void;

  /** Set to true to allow multiple selected values */
  isMulti: true;
};

type Value<V> = V extends any[] ? MultipleValue<V> : SingleValue<V>;

type CommonSelectProps<T extends SelectOption, V> = Value<V> & {
  getOptionValue?: (option: T) => V;
  getOptionLabel?: (option: T) => V;

  /** Formats option labels in the menu and control as React components */
  formatOptionLabel?: (option: any, meta: any) => React.ReactNode;

  /** Placeholder for the select value */
  placeholder?: React.ReactNode;

  /** whether the component's data is loading or not (async) */
  isLoading?: boolean;

  /** text to display when there are no options to list */
  emptyText?: string;

  /** Set to true to allow removing the selected value */
  isClearable?: boolean;

  inputClass?: string;

  /** Id for testing purposes */
  "data-testid"?: string;

  label?: string;

  name?: string;

  disabled?: boolean;

  /** Select options */
  options?: T[];
};

type SimpleSelectProps<T extends SelectOption, V> = CommonSelectProps<T, V> & {
  // Intentionally left blank
};

type AsyncSelectProps<T extends SelectOption, V> = CommonSelectProps<T, V> & {
  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: T;

  paginate?: boolean;

  /**
   * Function that returns a promise, which is the set of options to be used once the promise resolves.
   */
  loadOptions: (searchString: string, callback: (options: T[]) => undefined) => Promise<any> | undefined;
  cacheOptions?: boolean;
};

type AsyncPaginateSelectProps<T extends SelectOption, V> = CommonSelectProps<T, V> & {
  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: T;

  paginate: true;
  /**
   * Function that returns a promise with pagination data and a set of options matching the search string
   * See: https://github.com/vtaits/react-select-async-paginate/tree/master/packages/react-select-async-paginate#loadoptions
   */
  loadOptions: (
    searchString: string,
    previouslyLoaded: any[],
    additional?: any
  ) => Promise<{ options: T[]; hasMore: boolean; additional?: any }>;
};

export type SelectProps<T extends SelectOption, V> =
  | SimpleSelectProps<T, V>
  | AsyncSelectProps<T, V>
  | AsyncPaginateSelectProps<T, V>;
