import type { ReactNode } from "react";

import { IconTag } from "components/icontag";
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
    <>
      <div className="spacewalk-toolbar-h1">
        {props.button}
        <h1>
          {props.icon && <IconTag icon={`${props.icon}`} />}
          {t(props.title)}
          &nbsp;
          {help}
        </h1>
      </div>
      {props.children}
    </>
  );
}
