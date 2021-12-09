import * as React from "react";
import { AsyncButton, Button } from "../buttons";
import { Dialog, DialogProps } from "./Dialog";

type Props = DialogProps & {
  /** Text of the submit button */
  submitText: string;
  /** Icon of the submit button */
  submitIcon: string;
  /** Submit button class name */
  btnClass?: string;
  /** Data related to the dialog. This is passed as parameter to 'onConfirm*' handlers */
  item?: any;
  /** Function called when the submit button is clicked */
  onConfirm?: (...args: any[]) => any;
  /** Function called asynchronously when the submit button is clicked */
  onConfirmAsync?: (...args: any[]) => Promise<any>;
};

/**
 * A pop-up dialog for dangerous actions confirmation.
 * It contains a 'Cancel' button and a button with text from 'submitText'
 * and icon from 'submitIcon'.
 * Related data may be passed with the 'item' property.
 * This 'item' will be passed to the 'onConfirm*' handlers.
 */
export function DangerDialog(props: Props) {
  const { item, btnClass, submitText, submitIcon, onConfirm, onConfirmAsync, ...otherProps } = props;

  const buttonClass = btnClass || "btn-danger";
  const buttons = (
    <div>
      {props.onConfirmAsync ? (
        <AsyncButton
          text={submitText}
          title={submitText}
          icon={submitIcon}
          defaultType={buttonClass}
          action={() => {
            onConfirmAsync?.(true);
            props.onClose?.();
          }}
        />
      ) : null}
      {props.onConfirm ? (
        <Button
          className={buttonClass}
          text={submitText}
          title={submitText}
          icon={submitIcon}
          handler={() => {
            onConfirm?.(item);
            props.onClose?.();
          }}
        />
      ) : null}

      <Button
        className="btn-default"
        text={t("Cancel")}
        title={t("Cancel")}
        icon="fa-close"
        handler={() => {
          props.onClose?.();
        }}
      />
    </div>
  );

  return <Dialog footer={buttons} {...otherProps} />;
}
