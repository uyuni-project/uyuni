import * as React from "react";

type Props = {
  isError?: boolean;
  children: React.ReactNode;

  /** CSS class name to apply to the component */
  className?: string;
};

export function FormGroup(props: Props) {
  const className = props.className != null ? props.className : "";
  return <div className={`form-group${props.isError ? " has-error" : ""} ${className}`}>{props.children}</div>;
}
