import { Fragment, memo } from "react";

import _isEmpty from "lodash/isEmpty";

import { ProjectEnvironmentType, ProjectHistoryEntry } from "../../../type";
import BuildVersion from "../build/build-version";
import { getVersionMessageByNumber } from "../properties/properties.utils";

type Props = {
  environment: ProjectEnvironmentType;
  historyEntries: ProjectHistoryEntry[];
};

type EnvironmentStatusEnumType = Record<
  string,
  {
    key: string;
    text: string;
    isBuilding: boolean;
  }
>;

const environmentStatusEnum: EnvironmentStatusEnumType = {
  new: { key: "new", text: t("New"), isBuilding: false },
  building: { key: "building", text: t("Cloning channels"), isBuilding: true },
  generating_repodata: {
    key: "generating_repodata",
    text: t("Waiting for repositories data to be generated"),
    isBuilding: true,
  },
  built: { key: "built", text: t("Built"), isBuilding: false },
  failed: { key: "failed", text: t("Failed"), isBuilding: false },
};

const EnvironmentView = memo((props: Props) => {
  return (
    <Fragment>
      <dl className="row">
        <dt className="col-3">{t("Label")}:</dt>
        <dd className="col-9">{props.environment.label}</dd>
      </dl>
      <dl className="row">
        <dt className="col-3">{t("Description")}:</dt>
        <dd className="col-9">{props.environment.description}</dd>
      </dl>
      <dl className="row">
        <dt className="col-3">{t("Version")}:</dt>
        <dd className="col-9">
          <BuildVersion
            id={`${props.environment.version}_${props.environment.id}`}
            text={getVersionMessageByNumber(props.environment.version, props.historyEntries) || t("not built")}
          />
        </dd>
      </dl>
      {props.environment.version > 0 ? (
        <dl className="row">
          <dt className="col-3">{t("Status")}:</dt>
          <dd className="col-9">
            {environmentStatusEnum[props.environment.status].text}
            &nbsp;
            {environmentStatusEnum[props.environment.status].isBuilding && (
              <i className="fa fa-spinner fa-spin fa-1-5x" />
            )}
          </dd>
        </dl>
      ) : null}
      {props.environment.status === environmentStatusEnum.built.key && !_isEmpty(props.environment.builtTime) ? (
        <dl className="row">
          <dt className="col-3">{t("Built time")}:</dt>
          <dd className="col-9">{props.environment.builtTime}</dd>
        </dl>
      ) : null}
    </Fragment>
  );
});

export default EnvironmentView;
