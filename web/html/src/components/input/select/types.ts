/** Usually options have the shape `{ value: string; label: string }`, but the consumer can define any shape */
export type OptionType = { value: string; label: string } | Record<string, unknown>;

type SingleValue<V> = {
  value?: V;
  onChange?: (newValue: V | undefined) => void;
  /** Allow selecting multiple values */
  isMulti?: never;

  /** Allow removing the selected value, for single value this results in `undefined`, for multiple values this results in `[]` */
  isClearable: true;
};

type ClearableSingleValue<V> = {
  value?: V;
  onChange?: (newValue: V) => void;
  /** Allow selecting multiple values */
  isMulti?: never;

  /** Allow removing the selected value, for single value this results in `undefined`, for multiple values this results in `[]` */
  isClearable?: false;
};

type MultipleValue<V> = {
  value?: V;
  onChange?: (newValue: V) => void;
  /** Allow selecting multiple values */
  isMulti: true;

  /** Allow removing the selected value, for single value this results in `undefined`, for multiple values this results in `[]` */
  isClearable?: boolean;
};

type Value<V> = SingleValue<V> | ClearableSingleValue<V> | MultipleValue<V>;

type CommonSelectProps<O extends OptionType, V> = Value<V> & {
  onBlur?: (event: React.FocusEvent<HTMLInputElement>) => void;

  /** Get the value of an option, default: `option.value` */
  getOptionValue?: (option: O) => V;
  /** Get the label of an option, default: `option.label` */
  getOptionLabel?: (option: O) => V;

  /** Formats option labels in the menu and control as React components */
  formatOptionLabel?: (option: any, meta: any) => React.ReactNode;

  /** Placeholder for the select value */
  placeholder?: React.ReactNode;

  /** Whether the parent component is loading async data */
  isLoading?: boolean;

  /** Text to display when there are no options to list */
  emptyText?: string;

  className?: string;

  /** ARIA accessibility label, if none is available on the form */
  label?: string;

  name?: string;

  disabled?: boolean;

  /** List of options to show in the dropdown */
  options?: O[];

  /** Id for testing purposes */
  "data-testid"?: string;
};

type SimpleSelectProps<O extends OptionType, V> = CommonSelectProps<O, V>;

type AsyncSelectProps<O extends OptionType, V> = CommonSelectProps<O, V> & {
  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: O;

  paginate?: boolean;

  /** Get the array of options from an async request */
  loadOptions: (searchString: string, callback: (options: O[]) => undefined) => Promise<unknown> | undefined;
  cacheOptions?: boolean;
};

type AsyncPaginateSelectProps<O extends OptionType, V> = CommonSelectProps<O, V> & {
  /** Default value object if no value is set. This has to be an object corresponding to the rest of the schema. */
  defaultValueOption?: O;

  paginate: true;
  /**
   * Function that returns a promise with pagination data and a set of options matching the search string
   * See: https://github.com/vtaits/react-select-async-paginate/tree/master/packages/react-select-async-paginate#loadoptions
   */
  loadOptions: (
    searchString: string,
    previouslyLoaded: O[],
    additional?: any
  ) => Promise<{ options: O[]; hasMore: boolean; additional?: any }>;
};

/**
 * @param `O` The type of a single input option, e.g. `{ value: string; label: string }`
 * @param `V` The type of the selectable value, e.g. `string`
 */
export type SelectProps<O extends OptionType, V> =
  | SimpleSelectProps<O, V>
  | AsyncSelectProps<O, V>
  | AsyncPaginateSelectProps<O, V>;
