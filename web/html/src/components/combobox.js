// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
import Select from 'react-select';

declare function $(param: any): any;

type ComboboxItem = {
  id: any,
  text: string
}

type ComboboxProps = {
  id: string,
  name: string,
  data: Array<ComboboxItem>,
  value?: ?number | ?string,
  onFocus?: () => void,
  onSelect: (value: string) => void
};

type ComboboxState = {
  focused: boolean
};

class Combobox extends React.Component<ComboboxProps, ComboboxState> {
  render() {
    const options = this.props.data.map(item => ({value: item.id, label: item.text}));

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

    return <Select
      id={this.props.id}
      name={this.props.name}
      onFocus={this.props.onFocus}
      onChange={(selectedOption) => this.props.onSelect(selectedOption.value)}
      value={options.find(option => option.value === this.props.value)}
      options={options}
      styles={colourStyles}
    />;
  }
}

module.exports = {
  Combobox : Combobox
}
