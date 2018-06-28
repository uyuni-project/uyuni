// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
import CreatableSelect from 'react-select/lib/Creatable';

declare function $(param: any): any;

type ReactSelectItem = {
  value: ?string,
  id: ?any,
  label: string,
}

export type ComboboxItem = {
  id: any,
  text: string
}

type ComboboxProps = {
  id: string,
  name: string,
  data: Array<ComboboxItem>,
  selectedId?: ?number | ?string,
  onFocus?: () => void,
  onSelect: (value: ComboboxItem) => void
};

type ComboboxState = {
  focused: boolean
};

class Combobox extends React.Component<ComboboxProps, ComboboxState> {

  onChange = (selectedOption: ReactSelectItem) => {
    if(selectedOption.id && selectedOption.value) {
      return this.props.onSelect({
        id: selectedOption.id,
        text: selectedOption.label
      });
    }

    const sanitizedLabel = selectedOption.label && selectedOption.label.replace(/[',]/g, "");

    // It means a new option was created
    return this.props.onSelect({
      id: selectedOption.id,
      text: sanitizedLabel
    });
  };

  render() {
    const colourStyles = {
      option: (styles, { data, isDisabled, isFocused, isSelected }) => {

        if (isFocused) {
          return Object.assign(styles, {backgroundColor: "#052940", color: "#ffffff"});
        }

        if (isSelected) {
          return Object.assign(styles, {backgroundColor: "#063559", color: "#ffffff"});
        }

        return styles;
      },
    };

    // The react-select is expecting the value to be a string, but let's keep the original id here so we can propagate
    // correctly the selected option up.
    const options: Array<ReactSelectItem> = this.props.data.map(item => ({value: item.id.toString(), id: item.id, label: item.text}));

    return <CreatableSelect
      id={this.props.id}
      name={this.props.name}
      onFocus={this.props.onFocus}
      onChange={this.onChange}
      value={options.find(option => option.id === this.props.selectedId)}
      options={options}
      styles={colourStyles}
    />;
  }
}

module.exports = {
  Combobox : Combobox
}
