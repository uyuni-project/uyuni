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

const CreateProjects = () => {

  return (
    <TopPanel
      title={t("Add new PAYG ssh connection data")}
      icon="fa-plus"
      button={
        <div className="pull-right btn-group">
          <AsyncButton
            id="savebutton"
            className="btn-primary"
            title={t("Add PAYG ssh data")}
            text={t("Create")}
            icon="fa-plus"
          />
        </div>
      }
    >

    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateProjects));
