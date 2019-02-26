// @flow
import React from 'react';

import type {projectPropertiesType} from '../../../type/project.type.js';

type Props = {
  properties: projectPropertiesType,
}

const PropertiesView= (props: Props) => {

  //TODO: Improve data normalization
  const historyEntries = props.properties.historyEntries || [];
  const sortVersionsHistoryBackwards = [...historyEntries.sort((a, b) => b.version - a.version)]

  return (
    <div>
      <React.Fragment>
        <dl className="row">
          <dt className="col-xs-2">Label:</dt>
          <dd className="col-xs-6">{props.properties.label}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Name</dt>
          <dd className="col-xs-10">{props.properties.name}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Description</dt>
          <dd className="col-xs-10">{props.properties.description}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Versions history:</dt>
          <dd className="col-xs-10">
            <ul className="list-unstyled">
              {
                sortVersionsHistoryBackwards.map((history, index) => {
                  const versionMessage = `Version ${history.version}: ${history.message}`
                  return (
                    <li>
                      {
                        index === 0
                          ? <strong>{versionMessage}</strong>
                          : versionMessage
                      }
                    </li>
                  )
                })
              }
            </ul>
          </dd>
        </dl>
      </React.Fragment>
    </div>
  );
};


export default PropertiesView;
