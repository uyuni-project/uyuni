import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import useLifecyclePaygActionsApi from "manager/admin/payg-shared/api/payg-actions-api";
import { PaygSshDataFormFields } from "manager/admin/payg-shared/sshData/payg-ssh-data-form";

import { AsyncButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { Form, Text } from "components/input";
import { Panel } from "components/panels/Panel";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr } from "components/toastr/toastr";

const CreateProject = () => {
  const [payg, setPayg] = useState({
    properties: {
      description: "",
      host: "",
      port: "",
      username: "",
      password: "",
      key: "",
      key_password: "",
      bastion_host: "",
      bastion_port: "",
      bastion_username: "",
      bastion_password: "",
      bastion_key: "",
      bastion_key_password: "",
    },
    errors: {},
  });
  const { onAction } = useLifecyclePaygActionsApi();

  return (
    <TopPanel
      title={t("Add new pay-as-you-go ssh connection data")}
      icon="fa-plus"
      button={
        <div className="pull-right btn-group">
          <AsyncButton
            id="savebutton"
            className="btn-primary"
            title={t("Add pay-as-you-go ssh data")}
            text={t("Create")}
            icon="fa-plus"
            action={() =>
              onAction(payg.properties, "create")
                .then((data) => {
                  window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/payg/` + data);
                })
                .catch((error) => {
                  setPayg({ ...payg, errors: error.errors });
                  showErrorToastr(error.messages, { autoHide: false });
                })
            }
          />
        </div>
      }
    >
      <Form
        model={payg.properties}
        errors={payg.errors}
        onChange={(newProperties) => setPayg({ ...payg, properties: newProperties })}
      >
        <Panel headingLevel="h2" title={t("Pay-as-you-go connection Description")}>
          <div className="col-md-10">
            <div className="row">
              <Text required name="description" label={t("Description")} labelClass="col-md-2" divClass="col-md-10" />
            </div>
          </div>
        </Panel>
        <Panel headingLevel="h2" title={t("Instance SSH connection data")}>
          <div className="col-md-10">
            <PaygSshDataFormFields paygSshData={payg.properties} editing={false} isInstance={true} />
          </div>
        </Panel>
        <Panel headingLevel="h2" title={t("Bastion SSH connection data")}>
          <div className="col-md-10">
            <PaygSshDataFormFields paygSshData={payg.properties} editing={false} isInstance={false} />
          </div>
        </Panel>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateProject));
