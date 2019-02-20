// @flow
const React = require('react');

type Props = {
  helpUrl?: string,
  button?: React.Node,
  title: string,
  icon?: string,
  children: React.Node,
};

function TopPanel(props: Props) {
  const help = props.helpUrl
    ? (
      <a href={props.helpUrl} target="_blank" rel="noopener noreferrer">
        <i className="fa fa-question-circle spacewalk-help-link" />
      </a>
    )
    : null;

  return (
    <div>
      <div className="spacewalk-toolbar-h1">
        {props.button}
        <h1>
          {props.icon && <i className={`fa ${props.icon}`} />}
          {t(props.title)}
          &nbsp;
          {help}
        </h1>
      </div>
      {props.children}
    </div>
  );
}

TopPanel.defaultProps = {
  helpUrl: undefined,
  button: undefined,
  icon: undefined,
};

module.exports = {
  TopPanel,
};
