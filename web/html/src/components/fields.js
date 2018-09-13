/* eslint-disable */
'use strict';

const React = require("react");

class TextField extends React.Component {

    constructor(props) {
        super(props);
        ["onKeyPress"].forEach(method => this[method] = this[method].bind(this));
    }

    onKeyPress(event) {
        if( event.key == 'Enter' && this.props.onPressEnter ) {
            this.props.onPressEnter(event);
        }
    }

    render() {
        const defaultClassName = this.props.className ? this.props.className : 'form-control';

        return (<input id={this.props.id} className={defaultClassName}
                    value={this.props.defaultValue}
                    placeholder={this.props.placeholder}
                    type="text"
                    onChange={(e) => this.props.onChange(e)}
                    onKeyPress={this.onKeyPress}
                  />);
    }

}

module.exports = {
    TextField : TextField
}
