//@flow
import React from 'react';

import type {ProjectEnvironmentType} from '../../../type/project.type.js';
import type {ProjectHistoryEntry} from "../../../type/project.type";
import {getVersionMessageByNumber} from "../properties/properties.utils";

type Props = {
  environment: ProjectEnvironmentType,
  historyEntries: Array<ProjectHistoryEntry>
}

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
            <dd className="col-xs-9">To be implemented</dd>
          </dl>
          : null
      }
    </React.Fragment>
  );
});

export default EnvironmentView;
