// @flow

const React = require('react');

type Props = {
  className?: string,
  children: React.Node,
}

function PanelRow(props: Props) {
  return (
    <div className="row">
      <span className={props.className}>
        {props.children}
      </span>
    </div>
  );
}

module.exports = {
  PanelRow,
};
