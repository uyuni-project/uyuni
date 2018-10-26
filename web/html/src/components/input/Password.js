// @flow

const React = require('react');
const { Text } = require('./Text');
const { InputBase } = require('./InputBase');

type Props = {
  placeholder?: string,
  inputClass?: string,
} & InputBase.Props;

function Password(props: Props) {
  return (<Text type="password" {...props} />);
}

Password.defaultProps = Object.assign({
  placeholder: undefined,
  inputClass: undefined,
}, InputBase.defaultProps);

module.exports = {
  Password,
};
