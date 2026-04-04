import { useEffect, useState } from "react";

import useLifecyclePaygActionsApi from "manager/admin/payg-shared/api/payg-actions-api";
import PaygInfoEdit from "manager/admin/payg-shared/info/payg-info-edit";
import PaygSshDataEdit from "manager/admin/payg-shared/sshData/payg-ssh-data-edit";

import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

export type paygProperties = {
  description: string;
  host: string;
  port: string;
  username: string;
  password: string;
  key: string;
  key_password: string;
  bastion_host: string;
  bastion_port: string;
  bastion_username: string;
  bastion_password: string;
  bastion_key: string;
  bastion_key_password: string;
  instance_edit: boolean;
  bastion_edit: boolean;
};

export type PaygFullType = {
  id: string;
  status: string;
  statusMessage: string;
  lastChange: moment.Moment;
  properties: paygProperties;
};

type Props = {
  payg: PaygFullType;
  wasFreshlyCreatedMessage?: string;
  readOnly?: boolean;
};

const Payg = (props: Props) => {
  const [payg, setPayg] = useState(props.payg);
  const { onAction, cancelAction } = useLifecyclePaygActionsApi();

  useEffect(() => {
    if (props.wasFreshlyCreatedMessage) {
      showSuccessToastr(props.wasFreshlyCreatedMessage);
    }
  }, []);

  if (!props.payg) {
    return (
      <div className="alert alert-danger">
        <span>{t("The instance you are looking for does not exist or has been deleted")}.</span>
      </div>
    );
  }

  return (
    <TopPanel
      title={t("Instance Hostname: {host}", { host: payg.properties.host })}
      button={
        <div className="pull-right btn-group">
          <ModalButton
            className="btn-danger"
            title={t("Delete")}
            text={t("Delete")}
            target="delete-payg-modal"
            disabled={props.readOnly}
          />
        </div>
      }
    >
      <DeleteDialog
        id="delete-payg-modal"
        title={t("Delete PAYG Connection")}
        content={
          <span>
            {t("Are you sure you want to delete connection")} <strong>{payg.properties.host}</strong>?
          </span>
        }
        onConfirm={() =>
          onAction(payg, "delete", payg.id)
            .then(() => {
              window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/payg`);
            })
            .catch((error) => {
              showErrorToastr(error.messages, { autoHide: false });
            })
        }
      />
      <PaygInfoEdit
        payg={payg}
        readOnly={props.readOnly}
        onChange={(projectWithNewProperties) => {
          setPayg(projectWithNewProperties);
          cancelAction();
        }}
      />

      {!props.readOnly && (
        <>
          <PaygSshDataEdit
            paygSshData={payg.properties}
            paygId={payg.id}
            isInstance={true}
            labelPrefix={"Instance"}
            readOnly={props.readOnly}
            onChange={(projectWithNewProperties) => {
              setPayg(projectWithNewProperties);
              cancelAction();
            }}
          />

          <PaygSshDataEdit
            paygSshData={payg.properties}
            paygId={payg.id}
            isInstance={false}
            labelPrefix={"Bastion"}
            readOnly={props.readOnly}
            onChange={(projectWithNewProperties) => {
              setPayg(projectWithNewProperties);
              cancelAction();
            }}
          />
        </>
      )}
    </TopPanel>
  );
};

export default withPageWrapper<Props>(Payg);
