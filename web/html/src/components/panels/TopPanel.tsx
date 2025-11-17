import type { ReactNode } from "react";

import { HelpLink } from "components/utils/HelpLink";

type Props = {
  helpUrl?: string;
  button?: ReactNode;
  title: string;
  icon?: string;
  children?: ReactNode;
};

export function TopPanel(props: Props) {
  const help = props.helpUrl ? <HelpLink url={props.helpUrl} /> : null;

  return (
    <div>
      <div className="spacewalk-toolbar-h1">
        {props.button}
        <h1>
          {props.icon && <i className={`fa ${props.icon}`} />}
          {t(props.title)}
          &nbsp;
          {help}
        </h1>
      </div>
      {props.children}
    </div>
  );
}
