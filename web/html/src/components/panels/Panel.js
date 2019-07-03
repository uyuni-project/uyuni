const React = require('react');

type Props = {
  headingLevel: string,
  collapseId?: string,
  customIconClass?: string,
  title?: string,
  className?: string,
  icon?: string,
  header?: string,
  footer?: string,
  children: React.Node,
  buttons?: React.Node,
};

function Panel(props: Props) {
  const { headingLevel: HeadingLevel } = props;

  const titleContent = props.title && <React.Fragment>
    { props.icon && <i className={`fa ${props.icon}`} /> }
    { props.title }
  </React.Fragment>

  const bodyContent = <React.Fragment>
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
  </React.Fragment>

  return (
    <div className={"panel " + (props.className ? props.className : "panel-default")}>
      {(props.title || props.header || props.buttons)
      && (
        <div
          style={{
            position: "relative"
          }}
          className="panel-heading accordion-toggle"
        >
          { props.buttons
          && (
            <div
              className="pull-right btn-group"
              style={{
                position: "absolute",
                right: "20px",
                top: "50%",
                transform: "translateY(-50%)",
              }}>
              { props.buttons }
            </div>
          )
          }
          {
            <HeadingLevel
              style={{width: "85%"}}
            >
              {
                props.collapseId ?
                  <div data-toggle="collapse" href={`#${props.collapseId}-panel-closable`} className="accordion-toggle">
                    <i className={`fa fa-chevron-down show-on-collapsed ${props.customIconClass}`} />
                    <i className={`fa fa-chevron-right hide-on-collapsed ${props.customIconClass}`} />
                    {titleContent}
                  </div>
                  : titleContent
              }
            </HeadingLevel>
          }
          { props.header && <span>{props.header}</span>}
        </div>)
      }

      {
        props.collapseId ?
          <div id={`${props.collapseId}-panel-closable`} className="panel-collapse collapse in">
            {bodyContent}
          </div>
          : bodyContent
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
