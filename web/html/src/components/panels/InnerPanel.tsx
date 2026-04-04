import { type ReactNode, Children } from "react";

import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { cloneReactElement, HelpLink } from "components/utils";

type Props = {
  title: string;
  icon: string;
  buttons?: ReactNode[];
  buttonsLeft?: ReactNode[];
  children: ReactNode;
  summary?: ReactNode;
  helpUrl?: string;
};

function InnerPanel(props: Props) {
  const help = props.helpUrl ? <HelpLink url={props.helpUrl} /> : null;
  let toolbar: ReactNode = null;

  if (props.buttons?.length || props.buttonsLeft?.length) {
    toolbar = (
      <SectionToolbar>
        {props.buttonsLeft?.length ? (
          <div className="selector-button-wrapper">
            <div className="btn-group">
              {Children.toArray(props.buttonsLeft).map((child, index) => cloneReactElement(child, { key: index }))}
            </div>
          </div>
        ) : null}
        {props.buttons?.length ? (
          <div className="action-button-wrapper">
            <div className="btn-group">
              {Children.toArray(props.buttons).map((child, index) => cloneReactElement(child, { key: index }))}
            </div>
          </div>
        ) : null}
      </SectionToolbar>
    );
  }

  return (
    <div>
      <h2>
        <i className={`fa ${props.icon}`} />
        {props.title}
        &nbsp;
        {help}
      </h2>
      <p>{props.summary}</p>
      {toolbar}
      <div className="row">
        <div className="panel panel-default">
          <div className="panel-body">{props.children}</div>
        </div>
      </div>
    </div>
  );
}

export { InnerPanel };
