
import * as React from "react";
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

export enum ScapContentType {
  DataStream = "dataStream",
  TailoringFile = "tailoringFile",
}

export type ScheduleScapScanFormProps = {
  scapContentList: any[];
  tailoringFiles: any[];
  scapPolicies: any[];
  onSubmit: (model: any) => Promise<any>;
  earliest?: any;
  minions?: any[];
  createRecurringLink?: string;
  checkMemory?: boolean;
};

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
  scapContentList: any[];
  isInvalid: boolean;
};

export class ScheduleScapScanForm extends React.Component<ScheduleScapScanFormProps, StateType> {
  constructor(props: ScheduleScapScanFormProps) {
    super(props);

    this.state = {
      xccdfProfiles: [],
      model: {},
      messages: [],
      errors: [],
      earliest: props.earliest || localizedMoment(),
      tailoringFiles: props.tailoringFiles || [],
      tailoringFileProfiles: [],
      scapPolicies: props.scapPolicies || [],
      scapContentList: props.scapContentList || [],
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

  getProfiles(type: ScapContentType, id: string | number) {
    return Network.get(`/rhn/manager/api/audit/profiles/list/${type}/${id}`).then((data) => {
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

    this.setState({ selectedScapPolicy: value });
    return Network.get(`/rhn/manager/api/audit/scap/policy/view/${value}`).then((data) => {
      const xccdfProfiles = data.xccdfProfileId
        ? [{ id: data.xccdfProfileId, title: data.xccdfProfileTitle || data.xccdfProfileId }]
        : [];
      const tailoringFileProfiles = data.tailoringProfileId
        ? [{ id: data.tailoringProfileId, title: data.tailoringProfileTitle || data.tailoringProfileId }]
        : [];

      this.setState({
        model: {
          ...this.state.model,
          dataStreamName: data.scapContentId,
          xccdfProfileId: data.xccdfProfileId,
          tailoringFile: data.tailoringFileId,
          tailoringProfileID: data.tailoringProfileId,
          ovalFiles: data.ovalFiles || "",
          advancedArgs: data.advancedArgs || "",
          fetchRemoteResources: data.fetchRemoteResources || false,
        },
        xccdfProfiles,
        tailoringFileProfiles,
      });

      return data;
    });
  };

  handleSubmit = (model) => {
    // Inject earliest state into the model for the parent handler
    const submitModel = {
        ...model,
        earliest: this.state.earliest,
        selectedScapPolicy: this.state.selectedScapPolicy
    };

    return this.props.onSubmit(submitModel)
      .then((data) => {
        const msg = MessagesUtils.info(
          <span>
            {t("SCAP scan has been ")}
            <ActionLink id={data}>{t("scheduled.")}</ActionLink>
          </span>
        );
        this.setState({ messages: msg, errors: [] });
      })
      .catch((jqXHR) => {
        this.setState({
          messages: Network.responseErrorMessage(jqXHR),
        });
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

    return (
      <div>
        {errors.length > 0 && <Messages items={MessagesUtils.error(errors)} />}
        {messages.length > 0 && <Messages items={messages} />}

        <Panel headingLevel="h3" title="Schedule New XCCDF Scan">
          <Form
            model={this.state.model}
            className="schedule-scap-scan-form"
            onChange={this.onFormChange}
            onSubmit={this.handleSubmit}
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
                  options={this.state.scapContentList
                    .sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()))
                    .map((k) => ({ value: k.id, label: k.name }))}
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
                  options={tailoringFiles.map((k) => ({ value: k.id, label: k.name }))}
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
              <Label name={t("OVAL Files")} className="col-md-3" />
              <div className="col-md-6">
                <Text
                  name="ovalFiles"
                  disabled={!!selectedScapPolicy}
                  placeholder={selectedScapPolicy ? "" : t("e.g: file1.xml, file2.xml")}
                  title={t("Comma-separated list of OVAL files")}
                />
              </div>
            </FormGroup>

            <FormGroup>
              <Label name={t("Advanced Arguments")} className="col-md-3" />
              <div className="col-md-6">
                <Text
                  name="advancedArgs"
                  disabled={!!selectedScapPolicy}
                  placeholder={selectedScapPolicy ? "" : t("e.g: --results --report")}
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
                      {t("This requires internet and a lot of memory, make sure this minion has enough memory available!")}
                    </span>
                  </label>
                </div>
              </div>
            </FormGroup>

            <div className="panel-body">
              <ActionSchedule
                earliest={this.state.earliest}
                onDateTimeChanged={this.onDateTimeChanged}
                systemIds={this.props.minions?.map((m) => m.id)}
                actionType="states.apply"
              />
            </div>

            <div className="form-group">
              <div className="col-md-offset-3 col-md-6">
                {this.renderButtons()}
                {this.props.createRecurringLink && (
                  <LinkButton
                    icon="fa-plus"
                    href={this.props.createRecurringLink}
                    className="btn-default"
                    text={t("Create Recurring")}
                  />
                )}
              </div>
            </div>
          </Form>
        </Panel>
      </div>
    );
  }
}
