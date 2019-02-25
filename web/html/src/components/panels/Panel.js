const React = require('react');

type Props = {
  headingLevel: string,
  collapseId?: string,
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


  //TODO [LN] REVIEW 5PX retirado!!!!
  return (
    <div className={"panel " + (props.className ? props.className : "panel-default")}>
      {(props.title || props.header || props.buttons)
        && (
        <div className="panel-heading accordion-toggle">
          { props.buttons
            && (
              <div className="pull-right btn-group" style={{ paddingLeft: '0px 10px 0px 100px' }}>
                { props.buttons }
              </div>
            )
          }
          {
            <HeadingLevel>
              {
                props.collapseId ?
                  <div data-toggle="collapse" href={`#${props.collapseId}-panel-closable`} className="accordion-toggle">
                    <i className="fa fa-chevron-right show-on-collapsed" />
                    <i className="fa fa-chevron-down hide-on-collapsed" />
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
