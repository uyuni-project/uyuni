import * as React from "react";

import { paygProperties } from "manager/admin/payg/payg";
import useLifecyclePaygActionsApi from "manager/admin/payg-shared/api/payg-actions-api";
import PaygSshDataForm from "manager/admin/payg-shared/sshData/payg-ssh-data-form";
import PaygSshDataView from "manager/admin/payg-shared/sshData/payg-ssh-data-view";

import CreatorPanel from "components/panels/CreatorPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";
import { Loading } from "components/utils";

type Props = {
  paygSshData: paygProperties;
  paygId: string;
  isInstance: boolean;
  labelPrefix: string;
  onChange: Function;
  editing?: boolean;
};

const PaygSshDataEdit = (props: Props) => {
  const { onAction, cancelAction, isLoading } = useLifecyclePaygActionsApi();

  const saveAction = ({ item, closeDialog, setErrors }) => {
    if (props.isInstance) {
      item.instance_edit = true;
    } else {
      item.bastion_edit = true;
    }
    onAction(item, "update", props.paygId)
      .then((data) => {
        closeDialog();
        showSuccessToastr(t("Pay-as-you-go properties updated successfully"));
        props.onChange(data);
      })
      .catch((error) => {
        setErrors(error.errors);
        if (error.messages) {
          showErrorToastr(error.messages, { autoHide: false });
        } else {
          showErrorToastr(error, { autoHide: false });
        }
      });
  };

  let title = props.labelPrefix + " SSH connection";

  return (
    <div id={props.labelPrefix + "-panel-wrapper"}>
      <CreatorPanel
        id={(props.isInstance ? "bastion_" : "") + "connection"}
        creatingText={t("Edit " + props.labelPrefix)}
        panelLevel="2"
        title={t(title)}
        icon="fa-pencil"
        customIconClass="fa-small"
        onCancel={() => cancelAction()}
        onSave={saveAction}
        onOpen={({ setItem, setErrors }) => {
          setItem(props.paygSshData);
          setErrors(null);
        }}
        renderContent={() => (
          <React.Fragment>
            <PaygSshDataView payg={props.paygSshData} isInstance={props.isInstance} />
          </React.Fragment>
        )}
        renderCreationContent={({ open, item, setItem, errors }) => {
          if (isLoading) {
            return <Loading text={t("Editing ssh connection data..")} />;
          }
          return (
            <PaygSshDataForm
              paygSshData={{ ...item }}
              errors={errors}
              onChange={(editedProperties) => setItem(editedProperties)}
              isInstance={props.isInstance}
              editing={true}
            />
          );
        }}
      />
    </div>
  );
};

export default PaygSshDataEdit;
