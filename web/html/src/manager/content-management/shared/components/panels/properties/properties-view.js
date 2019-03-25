// @flow
import React from 'react';
import produce from "immer";

import type {ProjectPropertiesType} from '../../../type/project.type.js';

type Props = {
  properties: ProjectPropertiesType,
}

const PropertiesView= (props: Props) => {

  let propertiesToShow = produce(props.properties, draftProperties => {
      draftProperties.historyEntries.sort((a, b) => b.version - a.version)
  });

  return (
    <div>
      <React.Fragment>
        <dl className="row">
          <dt className="col-xs-2">Label:</dt>
          <dd className="col-xs-6">{propertiesToShow.label}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Name</dt>
          <dd className="col-xs-10">{propertiesToShow.name}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Description</dt>
          <dd className="col-xs-10">{propertiesToShow.description}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">Versions history:</dt>
          <dd className="col-xs-10">
            <ul className="list-unstyled">
              {
                propertiesToShow.historyEntries.map((history, index) => {
                  const versionMessage = `Version ${history.version}: ${history.message || ""}`
                  return (
                    <li key={`historyentries_${index}`}>
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
