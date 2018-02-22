// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

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

    combobox: any;

    constructor(props: ComboboxProps) {
        super(props);
        this.combobox = null;
        this.state = {
          focused: false
        };
    }

    componentDidMount() {
      this.combobox = $(ReactDOM.findDOMNode(this));

      // init widget
      this.combobox.select2({
        width: "20em",
        data: this.props.data,
        createSearchChoice: function(term: string, data: Array<ComboboxItem>) {
          // returns a new search choice if term is new
          var matchingChoices = $(data).filter(() => {
            // "this" is bound by jquery to the select2 component
            return this.text.localeCompare(term) == 0;
          });

          if (matchingChoices.length == 0) {
            var sanitizedTerm = term.replace(/[',]/g, "");
            return {id: sanitizedTerm, text: sanitizedTerm};
          }
        },
        maximumInputLength: 256,
        // initSelection: (element, callback) => callback(element.data("combobox-options"))
      });

      // init initial selection
      this.combobox.select2("val", this.props.value);

      // select radio button when combobox has focus
      this.combobox.on("select2-focus", (event) => {
        if(!this.state.focused) {
          this.setState({focused: true});
          this.props.onFocus && this.props.onFocus();
        }
      });

      this.combobox.on("select2-blur", (event) => {
        this.setState({focused: false});
      });

      this.combobox.on("select2-selecting", (event) => {
        console.log("change: " + event.val + " " + JSON.stringify(event.choice));
        this.props.onSelect(event.val);
      })

      this.combobox.on("change", (event) => {
        console.log("change: " + event.val + " " + JSON.stringify(event.added))
        this.props.onSelect(event.val);
      });
    }

    render() {
      return (
        <input type="hidden"
            id={this.props.id}
            name={this.props.name}
        />);
    }

}

module.exports = {
    Combobox : Combobox
}
