import * as React from "react";

import { BootstrapPanel } from "components/panels/BootstrapPanel";

import { AttestationResult, renderTime } from "./Utils";

type Props = {
  /** The id of the system to be shown */
  result: AttestationResult;
};

class CoCoResult extends React.Component<Props> {
  render = () => {
    const result = this.props.result;
    return (
      <BootstrapPanel key={result.id}>
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <td>{t("Result Type:")}</td>
                <td>{result.resultTypeLabel}</td>
              </tr>
              <tr>
                <td>{t("Description:")}</td>
                <td>{result.description}</td>
              </tr>
              <tr>
                <td>{t("Status:")}</td>
                <td>{result.statusDescription}</td>
              </tr>
              {result.status === "SUCCEEDED" && (
                <tr>
                  <td>{t("Attested on")}:</td>
                  <td>{renderTime(result.attestationTime)}</td>
                </tr>
              )}
              {result.details && (
                <tr>
                  <td>{t("Details")}:</td>
                  <td>
                    <div className="row">
                      <pre>{result.details}</pre>
                    </div>
                  </td>
                </tr>
              )}
              {result.processOutput && (
                <tr>
                  <td>{t("Process Output")}:</td>
                  <td>
                    <div className="row">
                      <pre>{result.processOutput}</pre>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );
  };
}

export default CoCoResult;
