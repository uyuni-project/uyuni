// @flow

const React = require('react');

type Props = {
  title: string,
  icon: string,
  buttons: React.Node,
  children: React.Node,
}

function InnerPanel(props: Props) {
  return (
    <div>
      <h2>
        <i className={`fa ${props.icon}`} />
        {props.title}
      </h2>
      <div className="spacewalk-section-toolbar">
        <div className="action-button-wrapper">
          <div className="btn-group">
            {props.buttons}
          </div>
        </div>
      </div>
      <div className="row">
        <div className="panel panel-default">
          <div className="panel-body">
            {props.children}
          </div>
        </div>
      </div>
    </div>
  );
}

module.exports = {
  InnerPanel,
};
