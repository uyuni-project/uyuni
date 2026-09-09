import { type ReactNode, useEffect, useRef } from "react";

type Props = {
  headingLevel?: keyof JSX.IntrinsicElements;
  collapseId?: string | null | undefined;
  customIconClass?: string | null | undefined;
  title?: string | null | undefined;
  className?: string;
  icon?: string | null | undefined;
  header?: ReactNode;
  footer?: ReactNode;
  children: ReactNode;
  buttons?: ReactNode;
  collapsClose?: boolean;
  onCollapsedChange?: (collapsed: boolean) => void;
};

export const Panel = (props: Props) => {
  const { headingLevel: HeadingLevel = "h1" } = props;

  const hasTitle = !!props.title;
  const hasHeader = !!props.header;

  const titleContent = hasTitle ? (
    <HeadingLevel style={{ width: "85%" }}>
      {props.icon && <i className={`fa ${props.icon}`} />}
      {props.title}
    </HeadingLevel>
  ) : null;

  const headerContent = hasHeader ? (
    <>
      {props.header}
    </>
  ) : null;

  const header = (
    <>
      {titleContent}
      {headerContent}
    </>
  );

  const bodyContent = (
    <>
      <div className="panel-body">{props.children}</div>

      {props.footer && <div className="panel-footer">{props.footer}</div>}
    </>
  );

  const collapseRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!collapseRef.current || !props.onCollapsedChange) {
      return;
    }

    const element = collapseRef.current;

    const onShown = (event: Event) => {
      if (event.target !== element) return;
      props.onCollapsedChange?.(false);
    };

    const onHidden = (event: Event) => {
      if (event.target !== element) return;
      props.onCollapsedChange?.(true);
    };

    element.addEventListener("shown.bs.collapse", onShown);
    element.addEventListener("hidden.bs.collapse", onHidden);

    return () => {
      element.removeEventListener("shown.bs.collapse", onShown);
      element.removeEventListener("hidden.bs.collapse", onHidden);
    };
  }, [props.onCollapsedChange]);

  const panelHeader = (titleContent || headerContent || props.buttons) && (
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

      {props.collapseId ? (
        <div
          data-bs-toggle="collapse"
          data-bs-target={`#${props.collapseId}-panel-closable`}
          className={`accordion-toggle d-flex align-items-center ${
            props.collapsClose ? "collapsed" : ""
          }`}
          aria-expanded={!props.collapsClose}
        >
          <i
            className={`fa fa-chevron-down show-on-collapsed ${
              props.customIconClass ?? ""
            }`}
          />
          <i
            className={`fa fa-chevron-right hide-on-collapsed ${
              props.customIconClass ?? ""
            }`}
          />

          {header}
        </div>
      ) : (
        header
      )}
      
    </div>
  );

  return (
    <div className={"panel " + (props.className ?? "panel-default")}>
      {panelHeader}

      {props.collapseId ? (
        <div
          ref={collapseRef}
          id={`${props.collapseId}-panel-closable`}
          className={`panel-collapse collapse ${
            props.collapsClose ? "" : "show"
          }`}
        >
          {bodyContent}
        </div>
      ) : (
        bodyContent
      )}
    </div>
  );
};
