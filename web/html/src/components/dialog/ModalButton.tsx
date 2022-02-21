import * as React from "react";

import { Button, ButtonProps } from "../buttons";
import { showDialog } from "./util";

type Props = ButtonProps & {
  target: string;
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
