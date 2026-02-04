import "./audit-common.css";

import { Component } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AceEditor } from "components/ace-editor";
import { ActionSchedule } from "components/action-schedule";
import { SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { Select } from "components/input/select/Select";
import { ActionLink } from "components/links";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";

import { localizedMoment } from "utils";
import Network from "utils/network";

declare global {
  interface Window {
    minions?: any[];
    remediation?: string;
    identifier?: string;
    result?: string;
    parentScanUrl?: string;
    parentScanProfile?: string;
    benchmarkId?: string;
  }
}

const messagesCounterLimit = 3;

const SCRIPT_TYPE = {
  BASH: "bash" as const,
  SALT: "salt" as const,
};

const TABS = {
  ORIGINAL: "#original",
  CUSTOM: "#custom",
};

const REMEDIATION_STATE_KEYS = {
  [SCRIPT_TYPE.BASH]: "customRemediationBash",
  [SCRIPT_TYPE.SALT]: "customRemediationSalt",
} as const;

const SAVED_REMEDIATION_STATE_KEYS = {
  [SCRIPT_TYPE.BASH]: "savedRemediationBash",
  [SCRIPT_TYPE.SALT]: "savedRemediationSalt",
} as const;

type StateType = {
  model: any;
  messages: any[];
  isInvalid?: boolean;
  originalRemediation: string;
  customRemediation: string;
  customRemediationBash: string;
  customRemediationSalt: string;
  savedRemediationBash: string;
  savedRemediationSalt: string;
  customScriptType: typeof SCRIPT_TYPE.BASH | typeof SCRIPT_TYPE.SALT;
  activeTabHash: string;
  identifier: string;
  benchmarkId: string;
  earliest: any;
  hasCustomRemediation: boolean;
  isSaving: boolean;
};

function htmlDecode(input: string): string {
  const doc = new DOMParser().parseFromString(input, "text/html");
  return doc.documentElement.textContent || "";
}

class RuleResultDetail extends Component<object, StateType> {
  constructor(props: object) {
    super(props);

    this.state = {
      model: {},
      messages: [],
      earliest: localizedMoment(),
      originalRemediation: htmlDecode(window.remediation || ""),
      customRemediation: "",
      customRemediationBash: "",
      customRemediationSalt: "",
      savedRemediationBash: "",
      savedRemediationSalt: "",
      customScriptType: SCRIPT_TYPE.BASH,
      activeTabHash: TABS.ORIGINAL,
      identifier: window.identifier || "",
      benchmarkId: window.benchmarkId || "",
      hasCustomRemediation: false,
      isSaving: false,
    };
  }

  componentDidMount() {
    // Load custom remediation if it exists
    this.loadCustomRemediation();
  }

  setEditorValue = (content: string) => {
    // Set the editor content programmatically using the Ace API
    // We need to wait for the tab to switch and editor to be initialized
    setTimeout(() => {
      const editorElement = document.getElementById("customRemediation");
      if (editorElement && (window as any).ace) {
        try {
          const editor = (window as any).ace.edit(editorElement);
          editor.setValue(content, -1); // -1 moves cursor to start
        } catch (e) {
          // Editor might not be ready yet, try again after a bit
          setTimeout(() => {
            try {
              const editor = (window as any).ace.edit(editorElement);
              editor.setValue(content, -1);
            } catch (e2) {
              // Failed to set editor value
            }
          }, 200);
        }
      }
    }, 50); // Reduced timeout as we just need next tick usually
  };

  loadCustomRemediation = () => {
    const { identifier, benchmarkId } = this.state;
    if (!identifier || !benchmarkId) return;

    Network.get(
      `/rhn/manager/api/audit/scap/custom-remediation/${encodeURIComponent(identifier)}/${encodeURIComponent(benchmarkId)}`
    )
      .then((data) => {
        if (data.success && data.data) {
          const bashContent = data.data.customRemediationBash || "";
          const saltContent = data.data.customRemediationSalt || "";
          const hasAny = bashContent || saltContent;
          const scriptType = bashContent ? "bash" : "salt";
          const customContent = bashContent || saltContent;

          this.setState({
            customRemediation: customContent,
            customRemediationBash: bashContent,
            customRemediationSalt: saltContent,
            savedRemediationBash: bashContent,
            savedRemediationSalt: saltContent,
            customScriptType: scriptType,
            hasCustomRemediation: !!hasAny,
            activeTabHash: hasAny ? TABS.CUSTOM : TABS.ORIGINAL,
          });

          this.setEditorValue(customContent);
        }
      })
      .catch(() => {
        // No custom remediation found, that's okay
      });
  };

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
    const { customScriptType } = this.state;
    const key = REMEDIATION_STATE_KEYS[customScriptType];

    this.setState({
      customRemediation: content,
      [key]: content,
    } as any);
  };

  onTabHashChange = (hash: string) => {
    this.setState({ activeTabHash: hash });
  };

  onSaveCustomRemediation = () => {
    const { identifier, benchmarkId, customRemediation, customScriptType } = this.state;

    if (!customRemediation.trim()) {
      this.setState({
        messages: MessagesUtils.error("Custom remediation cannot be empty"),
      });
      return;
    }

    this.setState({ isSaving: true });

    Network.post("/rhn/manager/api/audit/scap/custom-remediation", {
      identifier,
      benchmarkId,
      scriptType: customScriptType,
      remediation: customRemediation,
    })
      .then(() => {
        const key = REMEDIATION_STATE_KEYS[customScriptType];
        const savedKey = SAVED_REMEDIATION_STATE_KEYS[customScriptType];

        // Update the stored remediation for the current script type
        this.setState({
          messages: MessagesUtils.success("Custom remediation saved successfully"),
          hasCustomRemediation: true,
          isSaving: false,
          [key]: customRemediation,
          [savedKey]: customRemediation,
        } as any);

        // Scroll to top to show success message
        window.scrollTo({ top: 0, behavior: "smooth" });
      })
      .catch((error) => {
        this.setState({ isSaving: false });
        this.handleResponseError(error);
      });
  };

  onDeleteCustomRemediation = () => {
    const { identifier, benchmarkId, customScriptType } = this.state;

    if (!confirm("Are you sure you want to delete this custom remediation?")) {
      return;
    }

    Network.del(
      `/rhn/manager/api/audit/scap/custom-remediation/${encodeURIComponent(identifier)}/${encodeURIComponent(benchmarkId)}/${customScriptType}`
    )
      .then(() => {
        const { customScriptType, customRemediationBash, customRemediationSalt } = this.state;
        const key = REMEDIATION_STATE_KEYS[customScriptType];
        const savedKey = SAVED_REMEDIATION_STATE_KEYS[customScriptType];

        // Check if OTHER script type still has content
        const otherHasContent =
          customScriptType === SCRIPT_TYPE.BASH ? !!customRemediationSalt : !!customRemediationBash;

        const updates: any = {
          messages: MessagesUtils.success("Custom remediation deleted successfully"),
          customRemediation: "",
          [key]: "",
          [savedKey]: "",
          hasCustomRemediation: otherHasContent,
          activeTabHash: otherHasContent ? TABS.CUSTOM : TABS.ORIGINAL,
        };

        this.setState(updates);
        this.setEditorValue("");

        // Scroll to top to show success message
        window.scrollTo({ top: 0, behavior: "smooth" });
      })
      .catch(this.handleResponseError);
  };

  onApplyRemediation = () => {
    const { activeTabHash, customRemediation, originalRemediation, identifier, benchmarkId, customScriptType } =
      this.state;

    // Determine which remediation to apply based on active tab
    const remediationToApply =
      activeTabHash === TABS.CUSTOM && customRemediation ? customRemediation : originalRemediation;

    const scriptType = activeTabHash === TABS.CUSTOM ? customScriptType : SCRIPT_TYPE.BASH;

    if (!remediationToApply.trim()) {
      this.setState({
        messages: [MessagesUtils.error("No remediation content available to apply")],
      });
      return Promise.resolve();
    }

    // Confirmation dialog
    const confirmMessage = `Are you sure you want to apply this ${activeTabHash === TABS.CUSTOM ? "custom" : "original"} remediation?`;
    if (!confirm(confirmMessage)) {
      return Promise.resolve();
    }

    const serverId = window.minions?.[0]?.id;
    if (!serverId) {
      this.setState({
        messages: [MessagesUtils.error("No server ID available")],
      });
      return Promise.resolve();
    }

    return Network.post("/rhn/manager/api/audit/scap/scan/rule-apply-remediation", {
      serverId: serverId,
      ruleIdentifier: identifier,
      benchmarkId: benchmarkId,
      scriptType: scriptType,
      remediationContent: remediationToApply,
      saveAsCustom: false, // Don't auto-save, user must explicitly save
      earliest: this.state.earliest,
    })
      .then((data) => {
        const actionId = data.data?.actionId || data.value;
        const msg = MessagesUtils.info(
          <span>
            {t("Remediation has been ")}
            <ActionLink id={actionId}>{t("scheduled.")}</ActionLink>
          </span>
        );

        this.setState((prevState) => {
          const msgs = prevState.messages.concat(msg);

          // Do not spam UI showing old messages
          while (msgs.length > messagesCounterLimit) {
            msgs.shift();
          }
          return { messages: msgs };
        });
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
        <div className="tab-description">This is the original remediation from the SCAP datastream (read-only)</div>
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
            onChange={(value) => {
              const newScriptType = value as "bash" | "salt";

              this.setState((prevState) => {
                const newContent =
                  newScriptType === "bash" ? prevState.customRemediationBash : prevState.customRemediationSalt;
                this.setEditorValue(newContent);
                return {
                  customScriptType: newScriptType,
                  customRemediation: newContent,
                };
              });
            }}
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
        <div className="custom-remediation-actions" style={{ marginTop: "10px" }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={this.onSaveCustomRemediation}
            disabled={this.state.isSaving || !this.state.customRemediation.trim()}
          >
            {this.state.isSaving ? "Saving..." : "Save Custom Remediation"}
          </button>
          {/* Only show delete button if saved script type has content */}
          {((this.state.customScriptType === "bash" && this.state.savedRemediationBash) ||
            (this.state.customScriptType === "salt" && this.state.savedRemediationSalt)) && (
            <button
              type="button"
              className="btn btn-danger"
              onClick={this.onDeleteCustomRemediation}
              style={{ marginLeft: "10px" }}
            >
              Delete Custom Remediation
            </button>
          )}
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
                      <li className={this.state.activeTabHash === TABS.ORIGINAL ? "active" : ""}>
                        <a
                          href={TABS.ORIGINAL}
                          onClick={(e) => {
                            e.preventDefault();
                            this.onTabHashChange(TABS.ORIGINAL);
                          }}
                        >
                          {t("Original")}
                        </a>
                      </li>
                      <li className={this.state.activeTabHash === TABS.CUSTOM ? "active" : ""}>
                        <a
                          href={TABS.CUSTOM}
                          onClick={(e) => {
                            e.preventDefault();
                            this.onTabHashChange(TABS.CUSTOM);
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
