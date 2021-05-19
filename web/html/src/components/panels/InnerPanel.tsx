import * as React from "react";

import { SectionToolbar } from "components/section-toolbar/section-toolbar";

type Props = {
  title: string;
  icon: string;
  buttons: React.ReactNode[];
  buttonsLeft?: React.ReactNode[];
  children: React.ReactNode;
};

function InnerPanel(props: Props) {
  let toolbar;

  if (props.buttons?.length > 0 || props.buttonsLeft?.length !== 0) {
    toolbar =
      <SectionToolbar>
        {
          props.buttonsLeft?.length !== 0 ?
            <div className="selector-button-wrapper">
              <div className="btn-group">{props.buttonsLeft}</div>
            </div>
            : null
        }
        {
          props.buttons?.length > 0 ?
            <div className="action-button-wrapper">
              <div className="btn-group">{props.buttons}</div>
            </div>
            : null
        }
      </SectionToolbar>;
  }

  return (
    <div>
      <h2>
        <i className={`fa ${props.icon}`} />
        {props.title}
      </h2>
      { toolbar }
      <div className="row">
        <div className="panel panel-default">
          <div className="panel-body">{props.children}</div>
        </div>
      </div>
    </div>
  );
}

export { InnerPanel };
