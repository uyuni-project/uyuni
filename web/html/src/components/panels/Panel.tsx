import * as React from "react";

type Props = {
  headingLevel?: keyof JSX.IntrinsicElements;
  collapseId?: string | null | undefined;
  customIconClass?: string | null | undefined;
  title: string | null | undefined;
  className?: string;
  icon?: string | null | undefined;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  children: React.ReactNode;
  buttons?: React.ReactNode;
  collapsClose?: boolean
};

const Panel = (props: Props) => {
  const { headingLevel: HeadingLevel = "h1" } = props;

  const titleContent = props.title && (
    <React.Fragment>
      {props.icon && <i className={`fa ${props.icon}`} />}
      {props.title}
    </React.Fragment>
  );

  const bodyContent = (
    <React.Fragment>
      <div className="panel-body">{props.children}</div>
      {props.footer && <div className="panel-footer">{props.footer}</div>}
    </React.Fragment>
  );

  return (
    <div className={"panel " + (props.className ? props.className : "panel-default")}>
      {(props.title || props.header || props.buttons) && (
        <div
          style={{
            position: "relative",
          }}
          className="panel-heading accordion-toggle"
        >
          {props.buttons && (
            <div
              className="pull-right btn-group"
              style={{
                position: "absolute",
                right: "20px",
                top: "50%",
                transform: "translateY(-50%)",
              }}
            >
              {props.buttons}
            </div>
          )}
          {
            <HeadingLevel style={{ width: "85%" }}>
              {props.collapseId ? (
                <div
                  data-bs-toggle="collapse"
                  data-bs-target={`#${props.collapseId}-panel-closable`}
                  className="accordion-toggle"
                  aria-expanded="false"
                >
                  <i
                    className={`fa fa-chevron-down show-on-collapsed ${props.customIconClass ? props.customIconClass : ""
                      }`}
                  />
                  <i
                    className={`fa fa-chevron-right hide-on-collapsed ${props.customIconClass ? props.customIconClass : ""
                      }`}
                  />
                  {titleContent}
                </div>
              ) : (
                titleContent
              )}
            </HeadingLevel>
          }
          {props.header && <span>{props.header}</span>}
        </div>
      )}

      {props.collapseId ? (
        <div id={`${props.collapseId}-panel-closable`} className={`panel-collapse collapse ${props.collapsClose ? "" : 'show'}`}>
          {bodyContent}
        </div>
      ) : (
        bodyContent
      )}
    </div>
  );
};

Panel.defaultProps = {
  title: undefined,
  icon: undefined,
  header: undefined,
  footer: undefined,
  buttons: undefined,
};

export { Panel };
