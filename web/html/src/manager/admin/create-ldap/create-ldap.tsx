import { useState } from "react";

import { AsyncButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr } from "components/toastr/toastr";

import useLdapActionsApi from "../ldap-shared/api/ldap-actions-api";
import LdapForm from "../ldap-shared/ldap-form";
import { emptyLdapProperties, OrgOption } from "../ldap-shared/ldap-types";

type Props = {
  orgs: OrgOption[];
};

const toRequestBody = (model) => ({
  ...model,
  priority: model.priority === "" ? 0 : Number(model.priority),
  port: model.port === "" ? null : Number(model.port),
  connectTimeout: model.connectTimeout === "" ? null : Number(model.connectTimeout),
  responseTimeout: model.responseTimeout === "" ? null : Number(model.responseTimeout),
  defaultOrgId:
    model.defaultOrgId === "" || model.defaultOrgId === null || model.defaultOrgId === undefined
      ? null
      : Number(model.defaultOrgId),
  useMemberOf: false,
});

const CreateLdap = (props: Props) => {
  const [state, setState] = useState({
    properties: emptyLdapProperties(),
    errors: {},
  });
  const { onAction } = useLdapActionsApi();

  return (
    <TopPanel title={t("Add LDAP Server")} icon="fa-plus">
      <LdapForm
        model={state.properties}
        errors={state.errors}
        orgs={props.orgs}
        onChange={(properties) => setState((prev) => ({ ...prev, properties }))}
      >
        <div className="row">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <AsyncButton
              id="savebutton"
              className="btn-primary"
              title={t("Create LDAP server")}
              text={t("Create")}
              action={() =>
                onAction(toRequestBody(state.properties), "create")
                  .then((data) => {
                    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/ldap/` + data);
                  })
                  .catch((error) => {
                    setState((prev) => ({ ...prev, errors: error.errors || {} }));
                    showErrorToastr(error.messages || error, { autoHide: false });
                  })
              }
            />
          </div>
        </div>
      </LdapForm>
    </TopPanel>
  );
};

export default withPageWrapper(CreateLdap);
