import * as React from "react";

import { AttestationResult, renderTime } from "./Utils";

type Props = {
  /** The id of the system to be shown */
  result: AttestationResult;
};

class CoCoResult extends React.Component<Props> {
  render = () => {
    const result = this.props.result;
    return (
      <div className="panel panel-default" key={result.id}>
        <ul className="list-group">
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Result Type:")}</strong>
              </div>
              <div className="col-md-10">{result.resultTypeLabel}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Description:")}</strong>
              </div>
              <div className="col-md-10">{result.description}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Status:")}</strong>
              </div>
              <div className="col-md-10">{result.statusDescription}</div>
            </div>
          </li>
          {result.status === "SUCCEEDED" && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Attested on:")}</strong>
                </div>
                <div className="col-md-10">{renderTime(result.attestationTime)}</div>
              </div>
            </li>
          )}
          {result.details && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Details")}:</strong>
                </div>
                <div className="col-md-10">
                  <pre>{result.details}</pre>
                </div>
              </div>
            </li>
          )}
          {result.processOutput && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Process Output")}:</strong>
                </div>
                <div className="col-md-10">
                  <pre>{result.processOutput}</pre>
                </div>
              </div>
            </li>
          )}
        </ul>
      </div>
    );
  };
}

export default CoCoResult;
