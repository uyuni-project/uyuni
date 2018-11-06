const React = require('react');

type Props = {
  headingLevel: string,
  title?: string,
  icon?: string,
  header?: string,
  footer?: string,
  children: React.Node,
  buttons?: React.Node,
};

function Panel(props: Props) {
  const { headingLevel: HeadingLevel } = props;
  return (
    <div className="panel panel-default">
      {(props.title || props.header || props.buttons)
        && (
        <div className="panel-heading">
          { props.buttons
            && (
              <div className="pull-right btn-group" style={{ top: '-5px' }}>
                { props.buttons }
              </div>
            )
          }
          { props.title
            && (
              <HeadingLevel>
                { props.icon && <i className={`fa ${props.icon}`} /> }
                { props.title }
              </HeadingLevel>
            )
          }
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
  buttons: undefined,
};

module.exports = {
  Panel,
};
