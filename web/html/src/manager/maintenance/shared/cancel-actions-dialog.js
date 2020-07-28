// @flow

import React from "react";

import {DangerDialog} from "components/dialog/DangerDialog";

type Props = {
  id: string,
  onConfirmAsync: () => Promise<any>
};

export default function CancelActionsDialog(props: Props) {
  return (
    <DangerDialog
      id={props.id}
      title={t("Cancel affected actions")}
      onConfirmAsync={props.onConfirmAsync}
      submitText={t("Confirm")}
      submitIcon="fa-check"
      btnClass="btn-success"
      content={
        <>
          <p>{t("Any pending maintenance-only actions for these systems will be cancelled unless they are inside the new maintenance windows.")}</p>
          <p>{t("Are you sure you want to proceed?")}</p>
        </>
      }
    />
  );
}
