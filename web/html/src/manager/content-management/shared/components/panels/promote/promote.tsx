import * as React from "react";
import { useEffect, useState } from "react";
import { ModalButton } from "components/dialog/ModalButton";
import DownArrow from "../../down-arrow/down-arrow";
import { closeDialog, Dialog } from "components/dialog/Dialog";
import { Button } from "components/buttons";
import { ProjectEnvironmentType, ProjectHistoryEntry } from "../../../type/project.type";
import { getVersionMessageByNumber } from "../properties/properties.utils";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";
import { Loading } from "components/utils/Loading";
import useRoles from "core/auth/use-roles";
import { isOrgAdmin } from "core/auth/auth.utils";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import BuildVersion from "../build/build-version";

type Props = {
  projectId: string;
  environmentPromote: ProjectEnvironmentType;
  environmentTarget: ProjectEnvironmentType;
  environmentNextTarget: ProjectEnvironmentType;
  versionToPromote: number;
  historyEntries: Array<ProjectHistoryEntry>;
  onChange: Function;
};

const Promote = (props: Props) => {
  const [open, setOpen] = useState(false);
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({
    resource: "projects",
    nestedResource: "promote",
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useEffect(() => {
    if (!open) {
      cancelAction();
    }
  }, [open]);

  const modalNameId = `${props.environmentPromote.label}-cm-promote-env-modal`;

  const disabled =
    !hasEditingPermissions ||
    !props.environmentPromote.version ||
    props.environmentPromote.status === "building" || // the source env is already building
    props.environmentTarget.status === "building" || // the target environment is already building
    (props.environmentNextTarget || {}).status === "building"; // the "target+1" environment is already building - we can't promote as it would affect this build

  return (
    <div {...(disabled ? { title: t("No version to promote or colliding environment build in progress") } : {})}>
      <DownArrow />
      <div className="text-center">
        <ModalButton
          id={`${props.environmentPromote.label}-promote-modal-link`}
          className="btn-default"
          text={t("Promote")}
          disabled={disabled}
          target={modalNameId}
          onClick={() => {
            setOpen(true);
          }}
        />
      </div>

      <Dialog
        id={modalNameId}
        closableModal={false}
        className="modal-lg"
        onClosePopUp={() => setOpen(false)}
        content={
          isLoading ? (
            <Loading text={t("Promoting project..")} />
          ) : (
            <React.Fragment>
              <dl className="row">
                <dt className="col-xs-4">{t("Version")}:</dt>
                <dd className="col-xs-8">
                  <BuildVersion
                    id={`${props.environmentPromote.version}_promote_${props.environmentTarget.label}`}
                    text={
                      getVersionMessageByNumber(props.environmentPromote.version, props.historyEntries) ||
                      t("not built")
                    }
                    collapsed={true}
                  />
                </dd>
              </dl>
              <dl className="row">
                <dt className="col-xs-4">{t("Target environment")}:</dt>
                <dd className="col-xs-8">{props.environmentTarget.name}</dd>
              </dl>
            </React.Fragment>
          )
        }
        title={t("Promote version {0} into {1}", props.environmentPromote.version, props.environmentTarget.name)}
        buttons={
          <div className="col-lg-12">
            <div className="pull-right btn-group">
              <Button
                className="btn-default"
                text={t("Cancel")}
                title={t("Cancel")}
                handler={() => {
                  closeDialog(modalNameId);
                }}
              />
              <Button
                className="btn-success"
                text={t("Promote")}
                title={t("Promote environment")}
                handler={() => {
                  onAction(
                    {
                      projectLabel: props.projectId,
                      environmentPromoteLabel: props.environmentPromote.label,
                    },
                    "action",
                    props.projectId
                  )
                    .then(projectWithUpdatedSources => {
                      closeDialog(modalNameId);
                      showSuccessToastr(
                        t(
                          "Version {0} successfully promoted into {1}",
                          props.versionToPromote,
                          props.environmentTarget.name
                        )
                      );
                      props.onChange(projectWithUpdatedSources);
                    })
                    .catch(error => {
                      showErrorToastr(error.messages, { autoHide: false });
                      closeDialog(modalNameId);
                    });
                }}
              />
            </div>
          </div>
        }
      />
      <DownArrow />
    </div>
  );
};

export default Promote;
