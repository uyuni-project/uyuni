// @flow

const React = require('react');

type Props = {
  isError: boolean,
  children: React.Node,
};

function FormGroup(props: Props) {
  return (
    <div className={`form-group${props.isError ? ' has-error' : ''}`}>
      {props.children}
    </div>
  );
}

module.exports = {
  FormGroup,
};
