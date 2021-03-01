import * as React from "react";
import { Button } from "../buttons";
import { showDialog } from "./util";

// TODO: Extend Button's props here once that branch gets merged
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
 * Button to launch a modal dialog
 */
export function ModalButton(props: Props) {
  return (
    <Button
      id={props.id}
      className={props.className}
      title={props.title}
      text={props.text}
      icon={props.icon}
      disabled={props.disabled}
      handler={() => {
        props.onClick?.(props.item);
        showDialog(props.target);
      }}
    />
  );
}
