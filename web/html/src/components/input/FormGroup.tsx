import * as React from "react";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

type Props = {
  isError?: boolean;
  children: React.ReactNode;

  /** CSS class name to apply to the component */
  className?: string;
};

export function FormGroup(props: Props) {
  const className = !DEPRECATED_unsafeEquals(props.className, null) ? props.className : "";
  return <div className={`form-group${props.isError ? " has-error" : ""} ${className}`}>{props.children}</div>;
}
