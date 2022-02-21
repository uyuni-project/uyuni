import * as React from "react";

import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { cloneReactElement } from "components/utils";

type Props = {
  title: string;
  icon: string;
  buttons?: React.ReactNode[];
  buttonsLeft?: React.ReactNode[];
  children: React.ReactNode;
};

function InnerPanel(props: Props) {
  let toolbar: React.ReactNode = null;

  if (props.buttons?.length || props.buttonsLeft?.length) {
    toolbar = (
      <SectionToolbar>
        {props.buttonsLeft?.length ? (
          <div className="selector-button-wrapper">
            <div className="btn-group">
              {React.Children.toArray(props.buttonsLeft).map((child, index) =>
                cloneReactElement(child, { key: index })
              )}
            </div>
          </div>
        ) : null}
        {props.buttons?.length ? (
          <div className="action-button-wrapper">
            <div className="btn-group">
              {React.Children.toArray(props.buttons).map((child, index) => cloneReactElement(child, { key: index }))}
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
      </h2>
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
