import { useEffect, useState } from "react";

import { AsyncButton, Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { Dialog } from "components/dialog/Dialog";
import { ModalButton } from "components/dialog/ModalButton";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";

import useLdapActionsApi from "../ldap-shared/api/ldap-actions-api";
import LdapForm from "../ldap-shared/ldap-form";
import { emptyLdapProperties, LdapLookupResult, LdapServerFull, OrgOption } from "../ldap-shared/ldap-types";

type Props = {
  ldap: LdapServerFull | null;
  orgs: OrgOption[];
  wasFreshlyCreatedMessage?: string;
};

type TestDialogKind = "userLookup" | "groupResolution" | null;

const toFormModel = (ldap: LdapServerFull): LdapServerFull => ({
  ...emptyLdapProperties(),
  ...ldap,
  bindPassword: "",
  priority: ldap.priority ?? 0,
  port: ldap.port ?? "",
  connectTimeout: ldap.connectTimeout ?? "",
  responseTimeout: ldap.responseTimeout ?? "",
  defaultOrgId: ldap.defaultOrgId ?? null,
  useMemberOf: Boolean(ldap.useMemberOf),
});

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
  useMemberOf: Boolean(model.useMemberOf),
  // Empty password means keep existing; do not send a dummy value
  bindPassword: model.bindPassword || "",
});

const summarizeLookup = (result: LdapLookupResult, includeGroups: boolean): string => {
  const groups = result.groupLabels || [];
  const parts = [`DN: ${result.dn}`];
  if (includeGroups) {
    parts.push(`Groups (${groups.length}): ${groups.length ? groups.join(", ") : "(none)"}`);
  }
  return parts.join(" — ");
};

const Ldap = (props: Props) => {
  const [ldap, setLdap] = useState(props.ldap ? toFormModel(props.ldap) : null);
  const [errors, setErrors] = useState({});
  const [testDialog, setTestDialog] = useState<TestDialogKind>(null);
  const [testLogin, setTestLogin] = useState("");
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

  const closeTestDialog = () => {
    setTestDialog(null);
    setTestLogin("");
  };

  const runTestAction = () => {
    const login = testLogin.trim();
    if (!login) {
      showErrorToastr(t("Login is required"), { autoHide: false });
      return Promise.resolve();
    }
    const action = testDialog === "groupResolution" ? "testGroupResolution" : "testUserLookup";
    const includeGroups = testDialog === "groupResolution";
    return onAction({ login }, action, ldap.id)
      .then((data: LdapLookupResult) => {
        const summary = summarizeLookup(data, includeGroups);
        showSuccessToastr(
          includeGroups
            ? t("LDAP group resolution succeeded") + ": " + summary
            : t("LDAP user lookup succeeded") + ": " + summary
        );
        closeTestDialog();
      })
      .catch((error) => {
        showErrorToastr(error.messages || error, { autoHide: false });
      });
  };

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
          <Button
            id="test-user-lookup"
            className="btn-default"
            text={t("Test user lookup")}
            handler={() => {
              setTestLogin("");
              setTestDialog("userLookup");
            }}
          />
          <Button
            id="test-group-resolution"
            className="btn-default"
            text={t("Test group resolution")}
            handler={() => {
              setTestLogin("");
              setTestDialog("groupResolution");
            }}
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
      <Dialog
        id="ldap-test-login-modal"
        isOpen={testDialog !== null}
        onClose={closeTestDialog}
        title={testDialog === "groupResolution" ? t("Test group resolution") : t("Test user lookup")}
        content={
          <div className="form-group">
            <label className="control-label" htmlFor="ldap-test-login">
              {t("Login")}
            </label>
            <input
              id="ldap-test-login"
              className="form-control"
              type="text"
              value={testLogin}
              autoFocus
              onChange={(event) => setTestLogin(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  event.preventDefault();
                  runTestAction();
                }
              }}
            />
          </div>
        }
        footer={
          <div className="btn-group">
            <Button className="btn-default" text={t("Cancel")} handler={closeTestDialog} />
            <AsyncButton
              id="ldap-test-login-submit"
              className="btn-primary"
              text={t("Run test")}
              action={runTestAction}
            />
          </div>
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
