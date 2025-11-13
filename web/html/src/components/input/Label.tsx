import * as React from "react";

type Props = {
  name?: string;
  htmlFor?: string;
  className?: string;
  required?: boolean;
};

export function Label({ required = false, ...props }: Props) {
  return (
    <label className={`control-label${props.className ? ` ${props.className}` : ""}`} htmlFor={props.htmlFor}>
      {props.name}
      {required ? <span className="required-form-field"> *</span> : null}
      {props.name || required ? ":" : null}
    </label>
  );
}
