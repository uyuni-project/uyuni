import * as React from "react";

interface Props {
  className?: string;
  children: React.ReactNode;
}

function PanelRow(props: Props) {
  return <div className={`row ${props.className || ""}`}>{props.children}</div>;
}

export { PanelRow };
