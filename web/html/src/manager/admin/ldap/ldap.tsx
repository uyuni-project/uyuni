import { useEffect, useState } from "react";

import { AsyncButton } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import useLdapActionsApi from "../ldap-shared/api/ldap-actions-api";
import LdapForm from "../ldap-shared/ldap-form";
import { emptyLdapProperties, LdapServerFull, OrgOption } from "../ldap-shared/ldap-types";

type Props = {
  ldap: LdapServerFull | null;
  orgs: OrgOption[];
  wasFreshlyCreatedMessage?: string;
};

const toFormModel = (ldap: LdapServerFull): LdapServerFull => ({
  ...emptyLdapProperties(),
  ...ldap,
  bindPassword: "",
  priority: ldap.priority ?? 0,
  port: ldap.port ?? "",
  connectTimeout: ldap.connectTimeout ?? "",
  responseTimeout: ldap.responseTimeout ?? "",
  defaultOrgId: ldap.defaultOrgId ?? null,
});

const toRequestBody = (model) => ({
  ...model,
  priority: model.priority === "" ? 0 : Number(model.priority),
  port: model.port === "" ? null : Number(model.port),
  connectTimeout: model.connectTimeout === "" ? null : Number(model.connectTimeout),
  responseTimeout: model.responseTimeout === "" ? null : Number(model.responseTimeout),
  defaultOrgId: model.defaultOrgId === "" || model.defaultOrgId == null ? null : Number(model.defaultOrgId),
  // Empty password means keep existing; do not send a dummy value
  bindPassword: model.bindPassword || "",
});

const Ldap = (props: Props) => {
  const [ldap, setLdap] = useState(props.ldap ? toFormModel(props.ldap) : null);
  const [errors, setErrors] = useState({});
  const { onAction } = useLdapActionsApi();

  useEffect(() => {
    if (props.wasFreshlyCreatedMessage) {
      showSuccessToastr(props.wasFreshlyCreatedMessage);
    }
  }, []);

  if (!ldap) {
    return (
      <div className="alert alert-danger">
        <span>{t("The LDAP server you are looking for does not exist or has been deleted")}.</span>
      </div>
    );
  }

  return (
    <TopPanel
      title={t("LDAP Server: {label}", { label: ldap.label })}
      button={
        <div className="pull-right btn-group">
          <AsyncButton
            id="test-connection"
            className="btn-default"
            text={t("Test connection")}
            action={() =>
              onAction({}, "test", ldap.id)
                .then((messages) => {
                  showSuccessToastr(Array.isArray(messages) ? messages.join(" ") : t("LDAP connection test succeeded"));
                })
                .catch((error) => {
                  showErrorToastr(error.messages || error, { autoHide: false });
                })
            }
          />
          <ModalButton className="btn-danger" title={t("Delete")} text={t("Delete")} target="delete-ldap-modal" />
        </div>
      }
    >
      <DeleteDialog
        id="delete-ldap-modal"
        title={t("Delete LDAP Server")}
        content={
          <span>
            {t("Are you sure you want to delete LDAP server")} <strong>{ldap.label}</strong>?
          </span>
        }
        onConfirm={() =>
          onAction(ldap, "delete", ldap.id)
            .then(() => {
              window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/setup/ldap`);
            })
            .catch((error) => {
              showErrorToastr(error.messages || error, { autoHide: false });
            })
        }
      />
      <LdapForm
        model={ldap}
        errors={errors}
        orgs={props.orgs}
        editing
        onChange={(properties) => setLdap({ ...ldap, ...properties })}
      >
        <div className="row">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <AsyncButton
              id="savebutton"
              className="btn-primary"
              title={t("Save LDAP server")}
              text={t("Save")}
              action={() =>
                onAction(toRequestBody(ldap), "update", ldap.id)
                  .then((data) => {
                    setLdap(toFormModel(data));
                    setErrors({});
                    showSuccessToastr(t("LDAP server saved successfully"));
                  })
                  .catch((error) => {
                    setErrors(error.errors || {});
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

export default withPageWrapper<Props>(Ldap);
