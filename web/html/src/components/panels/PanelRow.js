// @flow

const React = require('react');

type Props = {
  className: string,
  children: React.Node,
}

function PanelRow(props: Props) {
  return (
    <div className="row">
      <span className="col-md-8 pull-right">
        <span className={props.className}>
          {props.children}
        </span>
      </span>
    </div>
  );
}

module.exports = {
  PanelRow,
};
