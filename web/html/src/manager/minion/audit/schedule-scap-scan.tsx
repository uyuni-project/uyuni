import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { Button, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { TopPanel } from "components/panels/TopPanel";
import { InnerPanel } from "components/panels/InnerPanel";
import { ActionSchedule } from "components/action-schedule";
import { LinkButton } from "components/buttons";
import { localizedMoment } from "utils";
import { ActionLink } from "components/links";
import { Utils as MessagesUtils } from "components/messages/messages";

declare global {
  interface Window {
    profileId?: number;
    activationKeys?: any;
    customDataKeys?: any;
    imageTypesDataFromTheServer?: any;
    scapDataStreams?: any;
    tailoringFiles?: any;
  }
}
const typeMap = {
  dockerfile: { name: "Dockerfile", storeType: "registry" },
  kiwi: { name: "Kiwi", storeType: "os_image" },
};
type PropsType = {
};
enum ScapContentType {
  DataStream = "Data_stream",
  TailoringFile = "tailoringFile",
}
const messagesCounterLimit = 3;

type StateType = {
  imageTypes: any;
  model: any;
  tailoringFiles: any;
  messages: any;
  customData: any;
  initLabel?: any;
  channels?: any;
  storeUri?: any;
  isInvalid?: boolean;
  errors: string[];
  datastreams?: any;
  scapPolicies?: any;
};

class ScheduelAuditScan extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    console.log(props);
    this.state = {
      imageTypes: [],
      datastreams: window.scapDataStreams,
      model: Object.assign({}, this.defaultModel),
      messages: [],
      customData: {},
      errors: [],
      profiles: [],
      earliest: localizedMoment(),
      tailoringFiles: window.tailoringFiles,
      tailoringFileProfiles: [],
      scapPolicies: window.scapPolicies,
      selectedScapPolicy: null, // Track selected scapPolicy
    };

    // Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.minionServerId)
    // .promise.then(data => {
    //   this.setState({  });
    // });

  }
  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };
  onFormChange = (model) => {
    this.setState({
      model: model,
    });
  };
  onValidate = (isValid) => {
    this.setState({
      isInvalid: !isValid,
    });
  };
  handleDataStreamChange = (name, value) => {
    this.getProfiles(ScapContentType.DataStream, value);
  };
  handleTailoringFileChange = (name, value) => {
    console.log(name)
    console.log(value)
    this.getProfiles(ScapContentType.TailoringFile, value);
  };
  getProfiles(type, name) {
    return Network.get("/rhn/manager/api/audit/profiles/list/" + type + "/" + name).then((data) => {
      console.log(type);
      if (type === ScapContentType.TailoringFile) {
        this.setState({
          tailoringFileProfiles: data,
        });
      } else {
        this.setState({
          imageTypes: data,
        });
      }

      return data;
    });
  };

  renderButtons() {
    var buttons = [
      <SubmitButton
        key="create-btn"
        id="create-btn"
        className="btn-success"
        icon="fa-plus"
        text={t("Create")}
        disabled={this.state.isInvalid}
      />,
    ];
    return buttons;
  }

  onCreate = (model) => {

    console.log("2" + model);
    return Network.post("/rhn/manager/api/audit/schedule/create", {
      ids: window.minions?.map((m) => m.id),
      earliest: this.state.earliest,
      xccdfProfileId: model.xccdfProfileId,
      dataStreamName: model.dataStreamName,
      tailoringFile: model.tailoringFile,
      tailoringProfileID: model.tailoringProfileID,


    }).then((data) => {
      const msg = MessagesUtils.info(
        (
          <span>
            {t("SCAP scan has been ")}
            <ActionLink id={data}>{t("scheduled.")}</ActionLink>
          </span>
        )
      );

      const msgs = this.state.messages.concat(msg);

      // Do not spam UI showing old messages
      while (msgs.length > messagesCounterLimit) {
        msgs.shift();
      }

      this.setState({
        messages: msgs,
      });
    }).catch(this.handleResponseError);
    return request;
  };

  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };
  handleScapPolicyChange = (name, value) => {
    console.log(value);
    if (!value) {
      // Reset fields to original state when scapPolicy is cleared
      this.setState({
        selectedScapPolicy: null,
        dataStreamName: null,
        xccdfProfileId: null,
        tailoringFile: null,
        tailoringProfileID: null,
        imageTypes: [], // Reset options if dynamically populated
        tailoringFileProfiles: [],
      });
      return;
    }

    if (value) {
      // Update the selectedScapPolicy state
      this.setState({ selectedScapPolicy: value });
      return Network.get("/rhn/manager/api/audit/scap/policy/view/" + value).then((data) => {
        console.log(data);
        const imageTypes = [{ id: data.xccdfProfileId, title: data.xccdfProfileId }]; // Example predefined profile
        const tailoringFileProfiles = [{ id: data.tailoringProfileId, title: data.tailoringProfileId }]; // Example predefined profile
        console.log(tailoringFileProfiles);
        this.setState({
          dataStreamName: data.dataStreamName,
          xccdfProfileId: data.xccdfProfileId,
          tailoringFile: data.tailoringFile,
          tailoringProfileID: data.tailoringProfileId,

          imageTypes,
          tailoringFileProfiles,
        });

        return data;
      });
    }
  };


  render() {
    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;

    const loc = window.location;
    const createLink = loc.pathname.replace("/highstate", "/recurring-states") + loc.search + "#/create";
    const buttonsLeft = [
      <LinkButton icon="fa-plus" href={createLink} className="btn-default" text={t("Create Recurring")} />,
    ];
    const showHighstate = [
      <InnerPanel
        title={t("Schedule SCAP Audit Scan")}
        icon="fa fa-clock-o"
        buttonsLeft={buttonsLeft}
      >
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>Apply Highstate1</h3>
            </div>
          </div>
          <div className="panel-body">
            <ActionSchedule
              earliest={this.state.earliest}
              actionChains={window.actionChains}
              onActionChainChanged={this.onActionChainChanged}
              onDateTimeChanged={this.onDateTimeChanged}
              systemIds={window.minions?.map((m) => m.id)}
              actionType="states.apply"
            />
          </div>
        </div>
      </InnerPanel>,
    ];

    const css = `
    .my-checkbox-tmp {
           margin-top: 10px !important;
           margin-left: 14px !important;
           margin-right: 12px !important;
         }
         .my-checkbox-tip {
                    color: #425074;
                  }
`
    const { dataStreamName, xccdfProfileId, tailoringFile, tailoringProfileID, selectedScapPolicy, tailoringFileProfiles } = this.state;
    return (
      <div>
        {errors}
        {messages}

        <p>
          {this.state.pathContentType}
        </p>
        <Panel headingLevel="h3" title="Schedule New XCCDF Scan" >
          <Form
            model={this.state.model}
            className="image-profile-form"
            onChange={this.onFormChange}
            onSubmit={(e) => (this.onCreate(e))}
            onValidate={this.onValidate}
          >

            <Select
              key="scapPolicy"
              name="scapPolicy"
              label={t("Scap Policy")}
              placeholder={t("No Policy Selected")}
              labelClass="col-md-3"
              divClass="col-md-6"
              isClearable
              onChange={this.handleScapPolicyChange}
              options={this.state.scapPolicies.map((k) => ({ value: k.id, label: k.policyName }))}
            />

            <Select
              key="dataStreamName"
              name="dataStreamName"
              label={t("Scap content")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
              isClearable
              defaultValue={dataStreamName}
              disabled={!!dataStreamName} // Disable if dataStreamName is set
              onChange={this.handleDataStreamChange}
              options={window.scapDataStreams.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
                .map((k) => ({ value: k, label: k.substring(0, k.indexOf("-xccdf.xml")).toUpperCase() }))}
            />


            <Select
              key="xccdfProfileId"
              name="xccdfProfileId"
              label={t("XCCDF Profile")}
              placeholder={t("Select xccdf profile...")}
              labelClass="col-md-3"
              divClass="col-md-6"
              required
              defaultValue={xccdfProfileId}
              disabled={!!xccdfProfileId}
              isClearable
              onChange={this.handleImageTypeChange}
              options={this.state.imageTypes.map((k) => ({ value: k.id, label: k.title }))}
            />
            <Select
              key="tailoringFile"
              name="tailoringFile"
              label={t("Tailoring File")}
              placeholder={t("Select Tailoring file...")}
              onChange={this.handleTailoringFileChange}
              labelClass="col-md-3"
              divClass="col-md-6"
              isClearable
              defaultValue={tailoringFile}
              disabled={!!tailoringFile}
              options={this.state.tailoringFiles.map((k) => ({ value: k.fileName, label: k.name }))}
            />

            <Select
              key="tailoringProfileID"
              name="tailoringProfileID"
              label={t("Profile from Tailoring File")}
              placeholder={t("Select profile...")}
              onChange={this.handleTokenChange}
              labelClass="col-md-3"
              divClass="col-md-6"
              isClearable
              defaultValue={tailoringProfileID}
              disabled={!!tailoringProfileID}
              options={this.state.tailoringFileProfiles.map((k) => ({ value: k.id, label: k.title }))}
            />
            {/*<div className="form-group">
              <label className="control-label col-md-3" >Fetch Remote Content:</label>
              <style>{css}</style>
              <input
                type="checkbox"
                id={"checkbox-for-"}
                value="billa"
                onChange={this.handleTokenChange}
                title={t("This product is mirrored.")}
                className="my-checkbox-tmp"
              />
              <span className="my-checkbox-tip">This requires a lot of memory, make sure this minion has enough memory available!</span>
            </div>*/}

            <div className="panel-body">
              <ActionSchedule
                earliest={this.state.earliest}
                onDateTimeChanged={this.onDateTimeChanged}
                systemIds={window.minions?.map((m) => m.id)}
                actionType="states.apply"
              />
            </div>
            <div className="form-group">
              <div className="col-md-offset-3 col-md-6">{this.renderButtons()}</div>
            </div>
          </Form>
        </Panel>

      </div>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<ScheduelAuditScan />, document.getElementById("schedule-scap-scan"));
}
