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
      instance_edit: false,
      bastion_edit: false,
    },
    errors: {},
  });
  const { onAction } = useLifecyclePaygActionsApi();

  return (
    <TopPanel title={t("Add new PAYG ssh connection data")} icon="fa-plus">
      <Form
        model={payg.properties}
        errors={payg.errors}
        onChange={(newProperties) => setPayg({ ...payg, properties: newProperties })}
      >
        <Panel headingLevel="h2" title={t("PAYG connection Description")}>
          <div className="row">
            <Text required name="description" label={t("Description")} labelClass="col-md-2" divClass="col-md-10" />
          </div>
        </Panel>
        <Panel headingLevel="h2" title={t("Instance SSH connection data")}>
          <PaygSshDataFormFields paygSshData={payg.properties} editing={false} isInstance={true} />
        </Panel>
        <Panel headingLevel="h2" title={t("Bastion SSH connection data")}>
          <PaygSshDataFormFields paygSshData={payg.properties} editing={false} isInstance={false} />
        </Panel>
        <div className="row">
          <div className="col-md-offset-2 offset-md-2 col-md-10">
            <AsyncButton
              id="savebutton"
              className="btn-primary"
              title={t("Add PAYG ssh data")}
              text={t("Create")}
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
        </div>
      </Form>
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateProject));
