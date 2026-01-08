import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { SubmitButton, LinkButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Text } from "components/input/text/Text";
import { TextArea } from "components/input/text-area/TextArea";
import { Select } from "components/input/select/Select";
import { Messages } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Utils } from "utils/functions";
import Network from "utils/network";
import { localizedMoment } from "utils/datetime";
import { RecurringActionsEdit } from "../../recurring/recurring-actions-edit";


type Props = {};
type State = {
  model: any;
  isInvalid?: boolean;
  messages?: Array<{ severity: string; text: string }>;
  errors: string[];
  dataStreams?: any;
  tailoringFiles: any;
  earliest: any;
  tailoringFileProfiles: any;
  xccdfProfiles: [];
  selectedTailoringFile: string;
  isEditMode: boolean;
  isReadOnly: boolean;
};

class ScapPolicy extends React.Component<Props, State> {
  form?: HTMLFormElement;

  constructor(props) {
    super(props);
    // policyData is already a JavaScript object from the template, not a JSON string
    const policyData = window.policyData || null;
    const isEditMode = window.isEditMode || false;
    const isReadOnly = window.isReadOnly || false;
    
    this.state = {
      model: policyData || {},
      isInvalid: true,
      messages: [],
      errors: [],
      isEditMode,
      isReadOnly,
      tailoringFiles: (window.tailoringFiles || []).map((file: any) => ({
        value: file.id,
        label: file.name,
        fileName: file.fileName, // Keep fileName for API calls
      })),
      dataStreams: (window.scapDataStreams || []).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase())).map((stream: string) => ({
        value: stream,
        label: stream.substring(0, stream.indexOf("-xccdf.xml")).toUpperCase(),
      })),
      tailoringFileProfiles: [],
      earliest: localizedMoment(),
      xccdfProfiles: [],
      selectedTailoringFile: "",
    };

  }

  componentDidMount() {
    // In edit/detail mode, load the profiles for the selected data stream and tailoring file
    if (this.state.isEditMode || this.state.isReadOnly) {
      const { model } = this.state;
      
      // Load XCCDF profiles if dataStreamName is set
      if (model.dataStreamName) {
        this.fetchProfiles("dataStream", model.dataStreamName).then(() => {
          // Force a re-render after profiles are loaded to ensure Select shows the value
          this.forceUpdate();
        });
      }
      
      // Load tailoring profiles if tailoringFile is set
      if (model.tailoringFile && model.tailoringFileName) {
        this.fetchProfiles("tailoringFile", model.tailoringFileName).then(() => {
          this.forceUpdate();
        });
      }
    }
  }

  onSubmit = async () => {
    try {
      const formData = new FormData(this.form);
      const jsonPayload = Object.fromEntries(formData.entries());
      
      // Add policy ID for update
      if (this.state.isEditMode && this.state.model.id) {
        jsonPayload.id = this.state.model.id;
      }
      
      // Explicitly add checkbox value since unchecked checkboxes don't submit in FormData
      jsonPayload.fetchRemoteResources = this.state.model.fetchRemoteResources || false;
      
      const endpoint = this.state.isEditMode 
        ? "/rhn/manager/api/audit/scap/policy/update"
        : "/rhn/manager/api/audit/scap/policy/create";
      
      const response = await Network.post(endpoint, jsonPayload);

      if (response.success) {
        window.location.href = "/rhn/manager/audit/scap/policies";
      } else {
        this.setState({ messages: response.messages });
      }
    } catch (error) {
      console.log(error);
      this.setState({
        messages: [{ severity: "error", text: "Unexpected error." }],
      });
    }
  };
  bindForm = (form: HTMLFormElement) => {
    this.form = form;
  };

  fetchProfiles = async (type: string, value: string) => {
    if (!value) return;
    console.log(type);
    try {
      const data = await Network.get(`/rhn/manager/api/audit/profiles/list/${type}/${value}`);
      if (type === "tailoringFile") {
        this.setState({ tailoringFileProfiles: data || [] });
      }
      else {
        this.setState({ xccdfProfiles: data || [] });
      }
    } catch (error) {
      console.log(error);
    }
  };
  handleDataStreamChange = (name, value) => {
    console.log(name)
    console.log(value)

  };
  renderButtons = () => {
    if (this.state.isReadOnly) {
      return (
        <LinkButton
          key="back-btn"
          id="back-btn"
          className="btn-default"
          icon="fa-arrow-left"
          text={t("Back to List")}
          href="/rhn/manager/audit/scap/policies"
        />
      );
    }
    
    return (
      <SubmitButton
        key="submit-btn"
        id="submit-btn"
        className="btn-success"
        icon={this.state.isEditMode ? "fa-save" : "fa-plus"}
        text={this.state.isEditMode ? t("Update") : t("Create")}
      />
    );
  };
  renderSelect = (name: string, label: string, options, onChange, isRequired = false) => (
    <Select
      name={name}
      label={label}
      isClearable
      labelClass="col-md-3"
      divClass="col-md-6"
      options={options}
      onChange={onChange}
      required={isRequired}
    />
  );
  renderMessages = () => {
    const messages = this.state.messages;
    if (!messages || messages.length === 0) {
      return null;
    }
    return <Messages items={messages} />;
  };

  render() {


    const { model, dataStreams, tailoringFiles, tailoringFileProfiles, xccdfProfiles, isReadOnly } = this.state;
    const title = isReadOnly ? t("Policy Details") : (this.state.isEditMode ? t("Edit Compliance Policy") : t("Create Compliance Policy"));

    return (
      <TopPanel title={title} icon="spacewalk-icon-manage-configuration-files">
        {this.renderMessages()}
        <Form
          model={this.state.model}
          className="scap-policy-form"
          onSubmit={isReadOnly ? (e) => e.preventDefault() : this.onSubmit}
          formRef={this.bindForm}
        >
          <Text
            name="policyName"
            label={t("Name")}
            required={!isReadOnly}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={isReadOnly}
          />
          <TextArea
            name="description"
            label={t("Description")}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={isReadOnly}
          />
          <FormGroup>
            <Label name={t("SCAP Content")} className="col-md-3" required />
            <div className="col-md-6">
              <Select
                name="dataStreamName"
                isClearable
                options={dataStreams}
                value={model.dataStreamName}
                onChange={(value) => {
                  this.setState({ model: { ...model, dataStreamName: value as string } });
                  this.fetchProfiles("dataStream", value as string);
                }}
                disabled={isReadOnly}
              />
            </div>
          </FormGroup>
          <FormGroup>
            <Label name={t("XCCDF Profile")} className="col-md-3" required />
            <div className="col-md-6">
              <Select
                name="xccdfProfileId"
                isClearable
                options={xccdfProfiles.map((type) => ({ value: type.id, label: type.title }))}
                value={model.xccdfProfileId}
                onChange={(value) => {
                  this.setState({ model: { ...model, xccdfProfileId: value as string } });
                }}
                disabled={isReadOnly}
              />
            </div>
          </FormGroup>
          <FormGroup>
            <Label name={t("Tailoring File")} className="col-md-3" />
            <div className="col-md-6">
              <Select
                name="tailoringFile"
                isClearable
                options={tailoringFiles}
                value={model.tailoringFile}
                onChange={(value) => {
                  // Find the selected tailoring file to get its fileName
                  const selectedFile = tailoringFiles.find((f: any) => f.value === value);
                  const fileName = selectedFile?.fileName || "";
                  this.setState({ 
                    model: { 
                      ...model, 
                      tailoringFile: value as string,
                      tailoringFileName: fileName
                    } 
                  });
                  if (fileName) {
                    this.fetchProfiles("tailoringFile", fileName);
                  }
                }}
                disabled={isReadOnly}
              />
            </div>
          </FormGroup>
          <FormGroup>
            <Label name={t("Tailoring Profile")} className="col-md-3" />
            <div className="col-md-6">
              <Select
                name="tailoringProfileId"
                isClearable
                options={tailoringFileProfiles.map((type) => ({ value: type.id, label: type.title }))}
                value={model.tailoringProfileId}
                onChange={(value) => {
                  this.setState({ model: { ...model, tailoringProfileId: value as string } });
                }}
                disabled={isReadOnly}
              />
            </div>
          </FormGroup>

          <FormGroup>
            <Label name={t("Advanced Arguments")} className="col-md-3" />
            <div className="col-md-6">
              <Text
                name="advancedArgs"
                placeholder={isReadOnly ? "" : t("e.g: --skip-valid --thin-results")}
                title={t("Additional command-line arguments for oscap")}
                disabled={isReadOnly}
              />
            </div>
          </FormGroup>

          <FormGroup>
              <Label name={t("Fetch Remote Resources")} className="col-md-3" />
              <div className="col-md-6">
                <div className="checkbox">
                  <label>
                    <input
                      type="checkbox"
                      name="fetchRemoteResources"
                      className="fetch-remote-checkbox"
                      value="true"
                      checked={model.fetchRemoteResources || false}
                      disabled={isReadOnly}
                      onChange={(e) => {
                        this.setState({
                          model: { ...model, fetchRemoteResources: e.target.checked }
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
          <hr />
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">{this.renderButtons()}</div>
          </div>
        </Form>
      </TopPanel>
    );
  }
}


export const renderer = () =>
  SpaRenderer.renderNavigationReact(<ScapPolicy />, document.getElementById("scap-create-policy"));
