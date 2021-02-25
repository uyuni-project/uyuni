import * as React from "react";
import _isEmpty from "lodash/isEmpty";

import { ProjectEnvironmentType } from "../../../type/project.type";
import { ProjectHistoryEntry } from "../../../type/project.type";
import { getVersionMessageByNumber } from "../properties/properties.utils";
import { objectDefaultValueHandler } from "core/utils/objects";
import BuildVersion from "../build/build-version";

type Props = {
  environment: ProjectEnvironmentType;
  historyEntries: Array<ProjectHistoryEntry>;
};

type EnvironmentStatusEnumType = {
  [key: string]: {
    key: string;
    text: string;
    isBuilding: boolean;
  };
};

const environmentStatusEnum: EnvironmentStatusEnumType = new Proxy(
  {
    new: { key: "new", text: t("New"), isBuilding: false },
    building: { key: "building", text: t("Cloning channels"), isBuilding: true },
    generating_repodata: { key: "generating_repodata", text: t("Generating repositories data"), isBuilding: true },
    built: { key: "built", text: t("Built"), isBuilding: false },
    failed: { key: "failed", text: t("Failed"), isBuilding: false },
  },
  objectDefaultValueHandler({ text: "", isBuilding: false })
);

const EnvironmentView = React.memo((props: Props) => {
  return (
    <React.Fragment>
      <dl className="row">
        <dt className="col-xs-3">{t("Label")}:</dt>
        <dd className="col-xs-9">{props.environment.label}</dd>
      </dl>
      <dl className="row">
        <dt className="col-xs-3">{t("Description")}:</dt>
        <dd className="col-xs-9">{props.environment.description}</dd>
      </dl>
      {/*<dl className="row">*/}
      {/*<dt className="col-xs-3">Registered Systems:</dt>*/}
      {/*<dd className="col-xs-9">{0}</dd>*/}
      {/*</dl>*/}
      <dl className="row">
        <dt className="col-xs-3">{t("Version")}:</dt>
        <dd className="col-xs-9">
          <BuildVersion
            id={`${props.environment.version}_${props.environment.label}`}
            text={getVersionMessageByNumber(props.environment.version, props.historyEntries) || t("not built")}
            collapsed={true}
          />
        </dd>
      </dl>
      {props.environment.version > 0 ? (
        <dl className="row">
          <dt className="col-xs-3">{t("Status")}:</dt>
          <dd className="col-xs-9">
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
          <dt className="col-xs-3">{t("Built time")}:</dt>
          <dd className="col-xs-9">{props.environment.builtTime}</dd>
        </dl>
      ) : null}
    </React.Fragment>
  );
});

export default EnvironmentView;
