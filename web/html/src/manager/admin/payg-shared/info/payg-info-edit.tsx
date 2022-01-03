import * as React from "react";

import { PaygFullType } from "manager/admin/payg/payg";
import useLifecyclePaygActionsApi from "manager/admin/payg-shared/api/payg-actions-api";
import PaygInfoForm from "manager/admin/payg-shared/info/payg-info-form";
import PaygInfoView from "manager/admin/payg-shared/info/payg-info-view";

import CreatorPanel from "components/panels/CreatorPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";
import { Loading } from "components/utils";

type Props = {
  payg: PaygFullType;
  onChange: Function;
  editing?: boolean;
};

const PaygInfoEdit = (props: Props) => {
  const { onAction, cancelAction, isLoading } = useLifecyclePaygActionsApi();

  const saveAction = ({ item, closeDialog, setErrors }) => {
    onAction(item.properties, "update", props.payg.id)
      .then((data) => {
        closeDialog();
        showSuccessToastr(t("Pay-as-you-go properties updated successfully"));
        props.onChange(data);
      })
      .catch((error) => {
        setErrors(error.errors);
        showErrorToastr(error.messages, { autoHide: false });
      });
  };

  return (
    <div id={"Info-panel-wrapper"}>
      <CreatorPanel
        id="information"
        creatingText={t("Edit Information")}
        panelLevel="2"
        title={"Information"}
        icon="fa-pencil"
        customIconClass="fa-small"
        onCancel={() => cancelAction()}
        onSave={saveAction}
        onOpen={({ setItem, setErrors }) => {
          setItem(props.payg);
          setErrors(null);
        }}
        renderContent={() => (
          <React.Fragment>
            <PaygInfoView payg={props.payg} />
          </React.Fragment>
        )}
        renderCreationContent={({ open, item, setItem, errors }) => {
          if (isLoading) {
            return <Loading text={t("Editing properties..")} />;
          }
          return (
            <PaygInfoForm
              payg={{ ...item.properties }}
              errors={errors}
              onChange={(editedProperties) => setItem({ ...item, properties: editedProperties })}
              editing={true}
            />
          );
        }}
      />
    </div>
  );
};

export default PaygInfoEdit;
