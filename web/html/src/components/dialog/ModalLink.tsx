import * as React from "react";

import { showDialog } from "./util";
import { Button } from "components/buttons";
type Props = {
  target: string;
  id?: string;
  className?: string;
  title?: string;
  text?: React.ReactNode;
  icon?: string;
  disabled?: boolean;
  item?: any;
  onClick?: (...args: any[]) => any;
};

/**
 * DEPRECATED: Do **NOT** use this component for new code, prefer `ModalButton` instead
 * Link to launch a modal dialog
 */
export function ModalLink(props: Props) {
  const margin = props.text ? "" : " no-margin";
  var icon = props.icon && <i className={"fa " + props.icon + margin} />;

  return (
    <Button
      id={props.id}
      title={props.title}
      className={"btn-tertiary " + (props.className || "")}
      icon={props.icon}
      text={props.text}
      handler={() => {
        if (props.onClick) props.onClick(props.item);
        showDialog(props.target);
      }}
    />
  );
}
