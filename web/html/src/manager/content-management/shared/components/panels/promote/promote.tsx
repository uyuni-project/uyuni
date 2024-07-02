import * as React from "react";
import { useEffect, useState } from "react";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import { Button } from "components/buttons";
import { closeDialog, Dialog } from "components/dialog/LegacyDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";
import { Loading } from "components/utils/Loading";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import { ProjectEnvironmentType, ProjectHistoryEntry } from "../../../type/project.type";
import DownArrow from "../../down-arrow/down-arrow";
import BuildVersion from "../build/build-version";
import { getVersionMessageByNumber } from "../properties/properties.utils";

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

  const modalNameId = `cm-promote-env-modal-${props.environmentPromote.id}`;

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
          id={`promote-modal-link-${props.environmentPromote.id}`}
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
                <dt className="col-4 col-xs-4">{t("Version")}:</dt>
                <dd className="col-8 col-xs-8">
                  <BuildVersion
                    id={`${props.environmentPromote.version}_promote_${props.environmentTarget.id}`}
                    text={
                      getVersionMessageByNumber(props.environmentPromote.version, props.historyEntries) ||
                      t("not built")
                    }
                    collapsed={true}
                  />
                </dd>
              </dl>
              <dl className="row">
                <dt className="col-4 col-xs-4">{t("Target environment")}:</dt>
                <dd className="col-8 col-xs-8">{props.environmentTarget.name}</dd>
              </dl>
            </React.Fragment>
          )
        }
        title={t("Promote version {version} into {environmentName}", {
          version: props.environmentPromote.version,
          environmentName: props.environmentTarget.name,
        })}
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
                    .then((projectWithUpdatedSources) => {
                      closeDialog(modalNameId);
                      showSuccessToastr(
                        t("Version {version} successfully promoted into {environmentName}", {
                          version: props.versionToPromote,
                          environmentName: props.environmentTarget.name,
                        })
                      );
                      props.onChange(projectWithUpdatedSources);
                    })
                    .catch((error) => {
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
