import "./audit-common.css";

import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { Select } from "components/input/select/Select";
import { ActionSchedule } from "components/action-schedule";
import { AceEditor } from "components/ace-editor";
import { localizedMoment } from "utils";
import { Utils as MessagesUtils } from "components/messages/messages";
import { ActionLink } from "components/links";

declare global {
  interface Window {
    minions?: Array<{ id: number }>;
    remediation?: string;
    identifier?: string;
    result?: string;
    parentScanUrl?: string;
    parentScanProfile?: string;
  }
}

const messagesCounterLimit = 3;

type StateType = {
  model: any;
  messages: any[];
  isInvalid?: boolean;
  originalRemediation: string;
  customRemediation: string;
  customScriptType: "bash" | "salt";
  activeTabHash: string;
  identifier: string;
  earliest: any;
};

function htmlDecode(input: string): string {
  const doc = new DOMParser().parseFromString(input, "text/html");
  return doc.documentElement.textContent || "";
}

class RuleResultDetail extends React.Component<{}, StateType> {
  constructor(props: {}) {
    super(props);

    this.state = {
      model: {},
      messages: [],
      earliest: localizedMoment(),
      originalRemediation: htmlDecode(window.remediation || ""),
      customRemediation: "",
      customScriptType: "bash",
      activeTabHash: "#original",
      identifier: window.identifier || "",
    };
  }

  onDateTimeChanged = (date: any) => {
    this.setState({ earliest: date });
  };

  onFormChange = (model: any) => {
    this.setState({ model });
  };

  onValidate = (isValid: boolean) => {
    this.setState({ isInvalid: !isValid });
  };

  onCustomRemediationChange = (content: string) => {
    this.setState({ customRemediation: content });
  };

  onTabHashChange = (hash: string) => {
    this.setState({ activeTabHash: hash });
  };

  onApplyRemediation = (model: any) => {
    const remediationToApply = this.state.customRemediation
      ? this.state.customRemediation
      : this.state.originalRemediation;

    return Network.post("/rhn/manager/api/audit/scap/scan/rule-apply-remediation", {
      ids: window.minions?.map((m) => m.id),
      earliest: this.state.earliest,
      remediationContent: remediationToApply,
      ruleIdentifier: this.state.identifier,
    })
      .then((data) => {
        const msg = MessagesUtils.info(
          <span>
            {t("Applying the remediation has been ")}
            <ActionLink id={data.value}>{t("scheduled.")}</ActionLink>
          </span>
        );

        const msgs = this.state.messages.concat(msg);

        // Do not spam UI showing old messages
        while (msgs.length > messagesCounterLimit) {
          msgs.shift();
        }

        this.setState({ messages: msgs });
      })
      .catch(this.handleResponseError);
  };

  handleResponseError = (jqXHR: any) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  renderOriginalTab() {
    return (
      <div className={`tab-content ${this.state.activeTabHash === "#original" ? "active" : ""}`}>
        <div className="tab-description">
          This is the original remediation from the SCAP datastream (read-only)
        </div>
        <div className="editor-container">
          <AceEditor
            id="originalRemediation"
            minLines={20}
            maxLines={40}
            mode="sh"
            content={this.state.originalRemediation}
            readOnly={true}
          />
        </div>
      </div>
    );
  }

  renderCustomTab() {
    return (
      <div className={`tab-content ${this.state.activeTabHash === "#custom" ? "active" : ""}`}>
        <div className="script-type-container">
          <label className="script-type-label">Script Type:</label>
          <Select
            name="customScriptType"
            value={this.state.customScriptType}
            onChange={(value) => this.setState({ customScriptType: value as "bash" | "salt" })}
            options={[
              { value: "bash", label: "Bash Script" },
              { value: "salt", label: "Salt State" },
            ]}
          />
        </div>
        <div className="editor-container">
          <AceEditor
            id="customRemediation"
            minLines={20}
            maxLines={40}
            mode={this.state.customScriptType === "bash" ? "sh" : "yaml"}
            onChange={this.onCustomRemediationChange}
            readOnly={false}
          />
        </div>
        <div className="editor-help-text">
          {this.state.customScriptType === "bash"
            ? "Write a bash script to remediate this rule"
            : "Write a Salt state (YAML format) to remediate this rule"}
        </div>
      </div>
    );
  }

  render() {
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;

    return (
      <div>
        {messages}
        <Panel headingLevel="h3" title="Details of Rule Result">
          <Form
            model={this.state.model}
            className="image-profile-form"
            onChange={this.onFormChange}
            onSubmit={this.onApplyRemediation}
            onValidate={this.onValidate}
          >
            <div className="rule-result-detail">
              <dl className="row">
                <dt className="col-md-3">Reference within Document:</dt>
                <dd className="col-md-9">{window.identifier}</dd>

                <dt className="col-md-3">Evaluation Result:</dt>
                <dd className="col-md-9">{window.result}</dd>

                <dt className="col-md-3">Parent Scan:</dt>
                <dd className="col-md-9">
                  <a href={window.parentScanUrl}>{window.parentScanProfile}</a>
                </dd>
              </dl>

              <div className="remediation-panel">
                <div className="remediation-panel-header">
                  <h4>Remediation</h4>
                </div>
                <div className="remediation-panel-body">
                  {/* Tab Navigation */}
                  <div className="spacewalk-content-nav">
                    <ul className="nav nav-tabs">
                      <li className={this.state.activeTabHash === "#original" ? "active" : ""}>
                        <a
                          href="#original"
                          onClick={(e) => {
                            e.preventDefault();
                            this.onTabHashChange("#original");
                          }}
                        >
                          {t("Original")}
                        </a>
                      </li>
                      <li className={this.state.activeTabHash === "#custom" ? "active" : ""}>
                        <a
                          href="#custom"
                          onClick={(e) => {
                            e.preventDefault();
                            this.onTabHashChange("#custom");
                          }}
                        >
                          {t("Custom")}
                        </a>
                      </li>
                    </ul>
                  </div>

                  {this.renderOriginalTab()}
                  {this.renderCustomTab()}
                </div>
              </div>
              <div className="panel-body">
                <ActionSchedule
                  earliest={this.state.earliest}
                  onDateTimeChanged={this.onDateTimeChanged}
                  systemIds={window.minions?.map((m) => m.id)}
                  actionType="states.apply"
                />
              </div>
              <div className="form-group">
                <div>
                  <SubmitButton
                    id="apply-btn"
                    className="btn-success"
                    text={t("Apply Remediation")}
                    disabled={this.state.isInvalid}
                  />
                </div>
              </div>
            </div>
          </Form>
        </Panel>
      </div>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<RuleResultDetail />, document.getElementById("rule-result-detail"));
};
