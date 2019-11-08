// @flow
import React from 'react';
import produce from "immer";

import type {ProjectPropertiesType} from '../../../type/project.type.js';
import {getVersionMessage} from "./properties.utils";
import {ModalLink} from "components/dialog/ModalLink";
import {Dialog} from "components/dialog/Dialog";
import {Panel} from "../../../../../../components/panels/Panel";
import styles from "./properties.css";

type Props = {
  properties: ProjectPropertiesType,
}

const NUMBER_HISTORY_ENTRIES = 5;

const PropertiesHistoryEntries = (props) =>
  <ul className="list-unstyled">
    {
      props.entries.map((history, index) => {
        const versionMessage = getVersionMessage(history)
        return (
          <li key={`historyentries_${props.id}_${index}`}>
            {
              index === 0
                ? versionMessage
                : <div>
                    <div className={`${styles.version_collapse_line} pointer`} data-toggle="collapse"
                        data-target={`#historyentries_${props.id}_${index}`} role="button"
                        aria-expanded="false" aria-controls="collapseExample">
                      {t('Version {0}: {1}', history.version, history.message.split('\n')[0])}
                    </div>
                    <div class="collapse" id={`historyentries_${props.id}_${index}`}>
                      <pre>
                        {
                          index === 0
                            ? <strong>{versionMessage}</strong>
                            : versionMessage
                        }
                      </pre>
                    </div>
                  </div>
            }
          </li>
        )
      })
    }
  </ul>

const PropertiesView = (props: Props) => {

  let propertiesToShow = produce(props.properties, draftProperties => {
    draftProperties.historyEntries.sort((a, b) => b.version - a.version)
  });

  return (
    <div>
      <React.Fragment>
        <dl className="row">
          <dt className="col-xs-2">{t("Label:")}</dt>
          <dd className="col-xs-6">{propertiesToShow.label}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t("Name")}</dt>
          <dd className="col-xs-10">{propertiesToShow.name}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t("Description")}</dt>
          <dd className="col-xs-10">{propertiesToShow.description}</dd>
        </dl>
        <dl className="row">
          <dt className="col-xs-2">{t("Versions history:")}</dt>
          <dd className="col-xs-10">
            <PropertiesHistoryEntries
              id="resume"
              entries={propertiesToShow.historyEntries.slice(0,NUMBER_HISTORY_ENTRIES)} />

            {
              propertiesToShow.historyEntries.length > NUMBER_HISTORY_ENTRIES &&
              <>
                <ModalLink
                  id={`properties-longlist-modal-button`}
                  text={t("show more")}
                  target="properties-longlist-modal-content"
                />
                <Dialog
                  id="properties-longlist-modal-content"
                  content={
                    <PropertiesHistoryEntries
                      id="longlist"
                      entries={propertiesToShow.historyEntries} />
                  }
                  title={t("Versions history")}
                />
              </>
            }
          </dd>
        </dl>
      </React.Fragment>
    </div>
  );
};


export default PropertiesView;
