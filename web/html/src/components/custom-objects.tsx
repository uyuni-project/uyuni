import * as React from "react";

type Props = {
  width: string;

  /** Width unit */
  um: string;

  className?: string;

  title?: string;

  children?: React.ReactNode;
};

export const CustomDiv = (props: Props) => {
  const width = props.width + props.um;
  const styleClass = Number.isNaN(width) ? undefined : { width };
  return (
    <div style={styleClass} className={"customDiv " + (props.className ?? "")} title={props.title}>
      {props.children}
    </div>
  );
};
