import { Fragment } from "react";

type Props = {
  headingLevel?: keyof JSX.IntrinsicElements;
  collapseId?: string | null | undefined;
  customIconClass?: string | null | undefined;
  title?: string | null | undefined;
  className?: string;
  icon?: string | null | undefined;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  children: React.ReactNode;
  buttons?: React.ReactNode;
  collapsClose?: boolean;
};

export const Panel = (props: Props) => {
  const { headingLevel: HeadingLevel = "h1" } = props;

  // header takes precedence over title
  const headerContent =
    props.header ??
    (props.title ? (
      <>
        {props.icon && <i className={`fa ${props.icon}`} />}
        {props.title}
      </>
    ) : null);

  const bodyContent = (
    <Fragment>
      <div className="panel-body">{props.children}</div>

      {props.footer && <div className="panel-footer">{props.footer}</div>}
    </Fragment>
  );

  return (
    <div className={"panel " + (props.className ?? "panel-default")}>
      {(headerContent || props.buttons) && (
        <div
          className="panel-heading"
          style={{
            position: "relative",
          }}
        >
          {props.buttons && (
            <div
              className="pull-right btn-group"
              style={{
                position: "absolute",
                right: "15px",
                top: "50%",
                transform: "translateY(-50%)",
              }}
            >
              {props.buttons}
            </div>
          )}

          <HeadingLevel style={{ width: "85%" }}>
            {props.collapseId ? (
              <div
                data-bs-toggle="collapse"
                data-bs-target={`#${props.collapseId}-panel-closable`}
                className={`accordion-toggle d-flex align-items-center ${props.collapsClose ? "collapsed" : ""}`}
                aria-expanded={!props.collapsClose}
              >
                <i className={`fa fa-chevron-down show-on-collapsed ${props.customIconClass ?? ""}`} />
                <i className={`fa fa-chevron-right hide-on-collapsed ${props.customIconClass ?? ""}`} />

                {headerContent}
              </div>
            ) : (
              headerContent
            )}
          </HeadingLevel>
        </div>
      )}

      {props.collapseId ? (
        <div
          id={`${props.collapseId}-panel-closable`}
          className={`panel-collapse collapse ${props.collapsClose ? "" : "show"}`}
        >
          {bodyContent}
        </div>
      ) : (
        bodyContent
      )}
    </div>
  );
};
