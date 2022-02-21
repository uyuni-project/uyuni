import * as React from "react";

import HelpIcon from "./HelpIcon";

type Props = {
  /** URL of the link */
  url: string;

  /** Title of the icon */
  text: string;
};

export function HelpLink(props: Props) {
  return (
    <a href={"/docs/" + window.docsLocale + "/" + props.url} target="_blank" rel="noopener noreferrer">
      <HelpIcon text={props.text} />
    </a>
  );
}
HelpLink.defaultProps = {
  text: t("Help"),
};
