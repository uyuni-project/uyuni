import * as React from "react";
import { PopUp } from "components/popup";
import { Loading } from "components/utils/Loading";
import { Form } from "components/input/Form";
import { Password } from "components/input/Password";
import { Button } from "components/buttons";
import { hideDialog } from "components/dialog/util";

export type PopupState = "wait" | "askPassword" | "errors";

type Props = {
  id: string;
  onSubmit?: () => void;
  popupState: PopupState;
  model: any;
  setModel: (model: any) => void;
  error: React.ReactNode;
};

export function MessagePopUp(props: Props) {
  const buttonValues = {
    askPassword: {
      text: t("Submit"),
    },
    errors: {
      text: t("Retry"),
    },
  }[props.popupState];

  const popupContent = () => {
    if (props.popupState === "wait") {
      return <Loading text={t("Connecting...")} withBorders={false} />;
    }
    if (props.popupState === "askPassword") {
      return (
        <Form model={props.model} className="form-horizontal" onChange={props.setModel}>
          <Password name="password" label={t("Password")} labelClass="col-md-3" divClass="col-md-6" />
        </Form>
      );
    }
    return props.error;
  };

  return (
    <PopUp
      id={props.id}
      hideHeader
      content={popupContent()}
      footer={
        buttonValues && [
          <Button
            key="submit"
            className="btn-primary"
            text={buttonValues.text}
            title={buttonValues.text}
            handler={() => props.onSubmit?.()}
          />,
          <Button
            key="cancel"
            className="btn-default"
            text={t("Cancel")}
            title={t("Cancel")}
            handler={() => hideDialog("popup")}
          />,
        ]
      }
    />
  );
}
