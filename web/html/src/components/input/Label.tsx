import * as React from "react";

interface Props {
  name?: string;
  htmlFor?: string;
  className?: string;
  required?: boolean;
}

export function Label(props: Props) {
  return (
    <label className={`control-label${props.className ? ` ${props.className}` : ""}`} htmlFor={props.htmlFor}>
      {props.name}
      {props.required ? <span className="required-form-field"> *</span> : null}
      {props.name || props.required ? ":" : null}
    </label>
  );
}

Label.defaultProps = {
  className: undefined,
  required: false,
};
