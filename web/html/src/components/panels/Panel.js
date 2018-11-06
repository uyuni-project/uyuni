const React = require('react');

type Props = {
  HeadingLevel: string,
  title?: string,
  icon?: string,
  header?: string,
  footer?: string,
  children: React.Node,
};

function Panel(props: Props) {
  const { headingLevel: HeadingLevel } = props;
  return (
    <div className="panel panel-default">
      {props.title
        && (
        <div className="panel-heading">
          <HeadingLevel>
            { props.icon && <i className={`fa ${props.icon}`} /> }
            { props.title }
          </HeadingLevel>
          { props.header && <span>{props.header}</span>}
        </div>)
      }
      <div className="panel-body">
        { props.children }
      </div>
      { props.footer
        && (
          <div className="panel-footer">
            {props.footer}
          </div>
        )
      }
    </div>
  );
}

Panel.defaultProps = {
  title: undefined,
  icon: undefined,
  header: undefined,
  footer: undefined,
};

module.exports = {
  Panel,
};
