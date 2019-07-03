//@flow
// globals moment
import React from 'react';
import _isEmpty from "lodash/isEmpty"

import type {ProjectEnvironmentType} from '../../../type/project.type.js';
import type {ProjectHistoryEntry} from "../../../type/project.type";
import {getVersionMessageByNumber} from "../properties/properties.utils";
import {objectDefaultValueHandler} from "core/utils/objects";

declare var moment: any;

type Props = {
  environment: ProjectEnvironmentType,
  historyEntries: Array<ProjectHistoryEntry>
}

type EnvironmentStatusEnumType = {
  [key:string]: {
    key: string,
    text: string,
    isBuilding: boolean
  }
}

const environmentStatusEnum: EnvironmentStatusEnumType = new Proxy({
    new: {key: "new", text: "New", isBuilding: false},
    building: {key: "building", text: "Cloning channels", isBuilding: true},
    generating_repodata: {key: "generating_repodata", text: "Generating repositories data", isBuilding: true},
    built: {key: "built", text: "Built", isBuilding: false},
    failed: {key: "failed", text: "Failed", isBuilding: false},
  },
  objectDefaultValueHandler({text: '', isBuilding: false})
);

// $FlowFixMe  // upgrade flow
const EnvironmentView = React.memo((props: Props) => {
  let  versionMessage = getVersionMessageByNumber(props.environment.version, props.historyEntries) || "not built";

  return (
    <React.Fragment>
      <dl className="row">
        <dt className="col-xs-3">Description:</dt>
        <dd className="col-xs-9">{props.environment.description}</dd>
      </dl>
      {/*<dl className="row">*/}
      {/*<dt className="col-xs-3">Registered Systems:</dt>*/}
      {/*<dd className="col-xs-9">{0}</dd>*/}
      {/*</dl>*/}
      <dl className="row">
        <dt className="col-xs-3">Version:</dt>
        <dd className="col-xs-9">{versionMessage}</dd>
      </dl>
      {
        props.environment.version > 0 ?
          <dl className="row">
            <dt className="col-xs-3">Status:</dt>
            <dd className="col-xs-9">
              {environmentStatusEnum[props.environment.status].text}
              &nbsp;
              {
                environmentStatusEnum[props.environment.status].isBuilding &&
                <i className="fa fa-spinner fa-spin fa-1-5x" />
              }
            </dd>
          </dl>
          : null
      }
      {
        props.environment.status === environmentStatusEnum.built.key && !_isEmpty(props.environment.builtTime) ?
          <dl className="row">
            <dt className="col-xs-3">Built time:</dt>
            <dd className="col-xs-9">
              {moment(props.environment.builtTime).format("YYYY-MM-DD HH:mm:ss")}
            </dd>
          </dl>
          : null
      }
    </React.Fragment>
  );
});

export default EnvironmentView;
