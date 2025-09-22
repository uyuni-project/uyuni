import * as React from "react";

import { Panel } from "./Panel";

type Props = {
  title?: string;
  icon?: string;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  children?: React.ReactNode;
  buttons?: React.ReactNode;
};

function BootstrapPanel(props: Props) {
  return (
    <Panel
      headingLevel="h2"
      title={props.title}
      icon={props.icon}
      header={props.header}
      footer={props.footer}
      buttons={props.buttons}
    >
      {props.children}
    </Panel>
  );
}

export { BootstrapPanel };
