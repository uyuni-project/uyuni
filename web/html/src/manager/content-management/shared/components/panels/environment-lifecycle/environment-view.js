//@flow
import React from 'react';

import type {ProjectEnvironmentType} from '../../../type/project.type.js';
import type {ProjectHistoryEntry} from "../../../type/project.type";

type Props = {
  environment: ProjectEnvironmentType,
  historyEntries: Array<ProjectHistoryEntry>
}

// $FlowFixMe  // upgrade flow
const EnvironmentView = React.memo((props: Props) => {
  let  versionMessage = "not built"; //`Version ${history.version}: ${history.message || ""}`
  if(props.environment.version) {
    const matchedVersion = props.historyEntries.find(
      historyEntry => historyEntry.version === props.environment.version
    );
    if(matchedVersion) {
      versionMessage = `Version ${matchedVersion.version}: ${matchedVersion.message || ""}`
    }
  }
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
      {/*<dl className="row">*/}
        {/*<dt className="col-xs-3">Compilation:</dt>*/}
        {/*<dd className="col-xs-9"><i className="fa fa-times-circle"></i></dd>*/}
      {/*</dl>*/}
    </React.Fragment>
  );
});

export default EnvironmentView;
