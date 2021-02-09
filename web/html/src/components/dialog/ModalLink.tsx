import * as React from "react";
import { showDialog } from "./util";

type Props = {
  target: string;
  id?: string;
  className?: string;
  title?: string;
  text: React.ReactNode;
  icon?: string;
  disabled?: boolean;
  item?: any;
  onClick: (...args: any[]) => any;
};

/**
 * DEPRECATED: Do **NOT** use this component for new code, prefer `ModalButton` instead
 * Link to launch a modal dialog
 */
export function ModalLink(props: Props) {
  const margin = props.text ? "" : " no-margin";
  var icon = props.icon && <i className={"fa " + props.icon + margin} />;

  // TODO: Using `href="#"` is bad for accessibility and semantics, this should be a `<button>` that looks like it needs to
  return (
    <a
      id={props.id}
      href="#"
      title={props.title}
      className={props.className}
      onClick={() => {
        if (props.onClick) props.onClick(props.item);
        showDialog(props.target);
      }}
    >
      {icon}
      {props.text}
    </a>
  );
}
