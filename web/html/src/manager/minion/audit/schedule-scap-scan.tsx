import "./schedule-scap-scan.css";

import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { ActionSchedule } from "components/action-schedule";
import { LinkButton } from "components/buttons";
import { localizedMoment } from "utils";
import { ActionLink } from "components/links";

enum ScapContentType {
  DataStream = "dataStream",
  TailoringFile = "tailoringFile",
}

const MESSAGES_COUNTER_LIMIT = 3;

type StateType = {
  xccdfProfiles: any[];
  model: any;
  tailoringFiles: any[];
  messages: any[];
  errors: string[];
  scapPolicies: any[];
  selectedScapPolicy: number | null;
  tailoringFileProfiles: any[];
  earliest: any;
  isInvalid: boolean;
};

class ScheduleAuditScan extends React.Component<{}, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      xccdfProfiles: [],
      model: {},
      messages: [],
      errors: [],
      earliest: localizedMoment(),
      tailoringFiles: window.tailoringFiles || [],
      tailoringFileProfiles: [],
      scapPolicies: window.scapPolicies || [],
      selectedScapPolicy: null,
      isInvalid: false,
    };
  }

  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };

  onFormChange = (model) => {
    this.setState({ model });
  };

  onValidate = (isValid) => {
    this.setState({ isInvalid: !isValid });
  };

  getProfiles(type: ScapContentType, name: string) {
    return Network.get(`/rhn/manager/api/audit/profiles/list/${type}/${name}`).then((data) => {
      if (type === ScapContentType.TailoringFile) {
        this.setState({ tailoringFileProfiles: data });
      } else {
        this.setState({ xccdfProfiles: data });
      }
      return data;
    });
  }

  handleScapPolicyChange = (name, value) => {
    if (!value) {
      // Reset fields when policy is cleared
      this.setState({
        selectedScapPolicy: null,
        model: {
          ...this.state.model,
          dataStreamName: null,
          xccdfProfileId: null,
          tailoringFile: null,
          tailoringProfileID: null,
          advancedArgs: "",
          fetchRemoteResources: false,
        },
        xccdfProfiles: [],
        tailoringFileProfiles: [],
      });
      return;
    }

    // Fetch and populate policy details
    this.setState({ selectedScapPolicy: value });
    return Network.get(`/rhn/manager/api/audit/scap/policy/view/${value}`).then((data) => {
      const xccdfProfiles = data.xccdfProfileId ? [{ id: data.xccdfProfileId, title: data.xccdfProfileId }] : [];
      const tailoringFileProfiles = data.tailoringProfileId
        ? [{ id: data.tailoringProfileId, title: data.tailoringProfileId }]
        : [];

      this.setState({
        model: {
          ...this.state.model,
          dataStreamName: data.dataStreamName,
          xccdfProfileId: data.xccdfProfileId,
          tailoringFile: data.tailoringFile,
          tailoringProfileID: data.tailoringProfileId,
          advancedArgs: data.advancedArgs || "",
          fetchRemoteResources: data.fetchRemoteResources || false,
        },
        xccdfProfiles,
        tailoringFileProfiles,
      });

      return data;
    });
  };

  onCreate = (model) => {
    return Network.post("/rhn/manager/api/audit/schedule/create", {
      ids: window.minions?.map((m) => m.id),
      earliest: this.state.earliest,
      xccdfProfileId: model.xccdfProfileId,
      dataStreamName: model.dataStreamName,
      tailoringFile: model.tailoringFile,
      tailoringProfileID: model.tailoringProfileID,
      advancedArgs: model.advancedArgs,
      fetchRemoteResources: model.fetchRemoteResources,
    })
      .then((data) => {
        const msg = MessagesUtils.info(
          <span>
            {t("SCAP scan has been ")}
            <ActionLink id={data}>{t("scheduled.")}</ActionLink>
          </span>
        );

        const msgs = this.state.messages.concat(msg);

        // Limit message history
        while (msgs.length > MESSAGES_COUNTER_LIMIT) {
          msgs.shift();
        }

        this.setState({ messages: msgs });
      })
      .catch(this.handleResponseError);
  };

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };

  renderButtons() {
    return [
      <SubmitButton
        key="create-btn"
        id="create-btn"
        className="btn-success"
        icon="fa-clock-o"
        text={t("Schedule")}
        disabled={this.state.isInvalid}
      />,
    ];
  }

  render() {
    const { errors, messages, selectedScapPolicy, xccdfProfiles, tailoringFileProfiles, tailoringFiles, scapPolicies } =
      this.state;

    // Get system ID from URL for recurring actions link
    const urlParams = new URLSearchParams(window.location.search);
    const sid = urlParams.get("sid");
    const createRecurringLink = `/rhn/manager/systems/details/recurring-actions?sid=${sid}#/create`;

    return (
      <div>
        {errors.length > 0 && <Messages items={MessagesUtils.error(errors)} />}
        {messages.length > 0 && <Messages items={messages} />}

        <Panel headingLevel="h3" title="Schedule New XCCDF Scan">
          <Form
            model={this.state.model}
            className="schedule-scap-scan-form"
            onChange={this.onFormChange}
            onSubmit={this.onCreate}
            onValidate={this.onValidate}
          >
            <FormGroup>
              <Label name={t("SCAP Policy")} className="col-md-3" />
              <div className="col-md-6">
                <Select
                  name="scapPolicy"
                  placeholder={t("No Policy Selected")}
                  isClearable
                  onChange={(value) => this.handleScapPolicyChange("scapPolicy", value)}
                  options={scapPolicies.map((k) => ({ value: k.id, label: k.policyName }))}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("SCAP Content")} className="col-md-3" required />
              <div className="col-md-6">
                <Select
                  name="dataStreamName"
                  isClearable
                  value={this.state.model.dataStreamName}
                  disabled={!!selectedScapPolicy}
                  onChange={(value) => {
                    this.setState({ model: { ...this.state.model, dataStreamName: value as string } });
                    if (value) {
                      this.getProfiles(ScapContentType.DataStream, value as string);
                    }
                  }}
                  options={window.scapDataStreams
                    .sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
                    .map((k) => ({ value: k, label: k.substring(0, k.indexOf("-xccdf.xml")).toUpperCase() }))}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("XCCDF Profile")} className="col-md-3" required />
              <div className="col-md-6">
                <Select
                  name="xccdfProfileId"
                  placeholder={t("Select XCCDF profile...")}
                  value={this.state.model.xccdfProfileId}
                  disabled={!!selectedScapPolicy}
                  isClearable
                  onChange={(value) => this.setState({ model: { ...this.state.model, xccdfProfileId: value } })}
                  options={xccdfProfiles.map((k) => ({ value: k.id, label: k.title }))}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("Tailoring File")} className="col-md-3" />
              <div className="col-md-6">
                <Select
                  name="tailoringFile"
                  placeholder={t("Select Tailoring file...")}
                  onChange={(value) => {
                    this.setState({ model: { ...this.state.model, tailoringFile: value as string } });
                    if (value) {
                      this.getProfiles(ScapContentType.TailoringFile, value as string);
                    }
                  }}
                  isClearable
                  value={this.state.model.tailoringFile}
                  disabled={!!selectedScapPolicy}
                  options={tailoringFiles.map((k) => ({ value: k.fileName, label: k.name }))}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("Profile from Tailoring File")} className="col-md-3" />
              <div className="col-md-6">
                <Select
                  name="tailoringProfileID"
                  placeholder={t("Select profile...")}
                  onChange={(value) => this.setState({ model: { ...this.state.model, tailoringProfileID: value } })}
                  isClearable
                  value={this.state.model.tailoringProfileID}
                  disabled={!!selectedScapPolicy}
                  options={tailoringFileProfiles.map((k) => ({ value: k.id, label: k.title }))}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("Advanced Arguments")} className="col-md-3" />
              <div className="col-md-6">
                <Text
                  name="advancedArgs"
                  disabled={!!selectedScapPolicy}
                  placeholder={selectedScapPolicy ? "" : t("e.g: --skip-valid --thin-results")}
                  title={t("Additional command-line arguments for oscap")}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("Fetch Remote Content")} className="col-md-3" />
              <div className="col-md-6">
                <div className="checkbox">
                  <label>
                    <input
                      type="checkbox"
                      name="fetchRemoteResources"
                      className="fetch-remote-checkbox"
                      checked={this.state.model.fetchRemoteResources || false}
                      disabled={!!selectedScapPolicy}
                      onChange={(e) => {
                        this.setState({
                          model: { ...this.state.model, fetchRemoteResources: e.target.checked },
                        });
                      }}
                    />
                    <span className="fetch-remote-help">
                      {t("This requires a lot of memory, make sure this minion has enough memory available!")}
                    </span>
                  </label>
                </div>
              </div>
            </FormGroup>

            <div className="panel-body">
              <ActionSchedule
                earliest={this.state.earliest}
                onDateTimeChanged={this.onDateTimeChanged}
                systemIds={window.minions?.map((m) => m.id)}
                actionType="states.apply"
              />
            </div>

            <div className="form-group">
              <div className="col-md-offset-3 col-md-6">
                {this.renderButtons()}
                <LinkButton
                  icon="fa-plus"
                  href={createRecurringLink}
                  className="btn-default"
                  text={t("Create Recurring")}
                />
              </div>
            </div>
          </Form>
        </Panel>
      </div>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<ScheduleAuditScan />, document.getElementById("schedule-scap-scan"));
};
