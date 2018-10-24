const React = require('react');

type Props = {
  title?: string,
  icon?: string,
  header?: string,
  footer?: string,
  children: React.Node,
};

function BootstrapPanel(props: Props) {
  return (
    <div className="panel panel-default">
      { props.title && (
        <div className="panel-heading">
          <h2>
            { props.icon && <i className={`fa ${props.icon}`} /> }
            {props.title}
          </h2>
          { props.header && <span>{props.header}</span>}
        </div>)
      }
      <div className="panel-body">
        { props.children }
      </div>
      { props.footer && (
        <div className="panel-footer">
          {props.footer}
        </div>)
      }
    </div>
  );
}

BootstrapPanel.defaultProps = {
  title: undefined,
  icon: undefined,
  header: undefined,
  footer: undefined,
};

module.exports = {
  BootstrapPanel,
};
