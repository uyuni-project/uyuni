import * as React from "react";

import Creatable from "react-select/creatable";

type ReactSelectItem = {
  value: string | null | undefined;
  id: any | null | undefined;
  label: string;
};

export type ComboboxItem = {
  id: any;
  text: string;
};

type ComboboxProps = {
  id: string;
  name: string;
  data?: Array<ComboboxItem>;
  selectedId?: (number | null | undefined) | (string | null | undefined);
  onFocus?: () => void;
  onSelect: (value: ComboboxItem) => void;
};

type ComboboxState = {
  focused: boolean;
};

export class Combobox extends React.Component<ComboboxProps, ComboboxState> {
  onChange = (selectedOption: ReactSelectItem) => {
    if (selectedOption.id && selectedOption.value) {
      return this.props.onSelect({
        id: selectedOption.id,
        text: selectedOption.label,
      });
    }

    const sanitizedLabel = selectedOption.label && selectedOption.label.replace(/[',]/g, "");

    // It means a new option was created
    return this.props.onSelect({
      id: selectedOption.id,
      text: sanitizedLabel,
    });
  };

  render() {
    const colourStyles = {
      option: (styles, { data, isDisabled, isFocused, isSelected }) => {
        if (isFocused) {
          return Object.assign(styles, { backgroundColor: "#052940", color: "#ffffff" });
        }

        if (isSelected) {
          return Object.assign(styles, { backgroundColor: "#063559", color: "#ffffff" });
        }

        return styles;
      },
      menu: (styles: {}) => ({
        ...styles,
        zIndex: 3,
      }),
      menuPortal: (styles: {}) => ({
        ...styles,
        zIndex: 9999,
      }),
    };

    // The react-select is expecting the value to be a string, but let's keep the original id here so we can propagate
    // correctly the selected option up.
    const options: Array<ReactSelectItem> = (this.props.data || []).map((item) => ({
      value: item.id.toString(),
      id: item.id,
      label: item.text,
    }));

    return (
      <Creatable
        id={this.props.id}
        name={this.props.name}
        onFocus={this.props.onFocus}
        onChange={this.onChange}
        value={options.find((option) => option.id === this.props.selectedId)}
        options={options}
        styles={colourStyles}
        menuPortalTarget={document.body}
        classNamePrefix={`class-${this.props.name}`}
      />
    );
  }
}
