//@flow
import React from 'react';

import type {projectEnvironmentType} from '../../../type/project.type.js';

type Props = {
  environment: projectEnvironmentType,
}

// $FlowFixMe  // upgrade flow
const EnvironmentView = React.memo((props: Props) => {
  return (
    <React.Fragment>
      <dl className="row">
        <dt className="col-xs-3">Description:</dt>
        <dd className="col-xs-9">{props.environment.description}</dd>
      </dl>
      <dl className="row">
        <dt className="col-xs-3">Registered Systems:</dt>
        <dd className="col-xs-9">{0}</dd>
      </dl>
      <dl className="row">
        <dt className="col-xs-3">Version:</dt>
        <dd className="col-xs-9">{props.environment.version || "not built"}</dd>
      </dl>
      <dl className="row">
        <dt className="col-xs-3">Compilation:</dt>
        <dd className="col-xs-9"><i className="fa fa-times-circle"></i></dd>
      </dl>
    </React.Fragment>
  );
});

export default EnvironmentView;
