import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Text } from "components/input/text/Text";
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
  xccdfProfiles: [],
  selectedTailoringFile: string
};

class ScapPolicy extends React.Component<Props, State> {
  form?: HTMLFormElement;

  constructor(props) {
    super(props);
    this.state = {
      model: {},
      isInvalid: true,
      messages: [],
      errors: [],
      tailoringFiles: (window.tailoringFiles || []).map((file: any) => ({
        value: file.fileName,
        label: file.name,
      })),
      dataStreams: (window.scapDataStreams || []).sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase())).map((stream: string) => ({
        value: stream,
        label: stream.substring(0, stream.indexOf("-xccdf.xml")).toUpperCase(),
      })),
      tailoringFileProfiles: [],
      earliest: localizedMoment(),
      xccdfProfiles: [],
    };

  }

  onCreate = async () => {
    try {
      const formData = new FormData(this.form);
      const jsonPayload = Object.fromEntries(formData.entries());
      const response = await Network.post("/rhn/manager/api/audit/scap/policy/create", jsonPayload);

      if (response.success) {
        window.location.href = "/rhn/manager/audit/scap/policies";
      } else {
        this.setState({ messages: response.messages });
      }
    } catch (error) {
      console.log(error);
      this.setState({
        messages: [{ severity: "error", text: "Unexpected error during upload." }],
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
      this.setState({
        messages: [{ severity: "error", text: "Error fetching profiles." }],
      });
    }
  };
  handleDataStreamChange = (name, value) => {
    console.log(name)
    console.log(value)

  };
  renderButtons = () => (
    <SubmitButton
      key="upload-btn"
      id="upload-btn"
      className="btn-success"
      icon="fa-plus"
      text={t("Create")}
    />
  );
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
    const { messages } = this.state;
    return messages.length > 0 && <Messages items={messages} />;
  };

  render() {


    const { model, dataStreams, tailoringFiles, tailoringFileProfiles, xccdfProfiles } = this.state;

    return (
      <TopPanel title={t("Create Compliance Policy")} icon="spacewalk-icon-manage-configuration-files">
        {this.renderMessages()}
        <Form
          model={this.state.model}
          className="scap-policy-form"
          onSubmit={this.onCreate}
          formRef={this.bindForm}
        >
          <Text
            name="policyName"
            label={t("Name")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <FormGroup>
            <Label name={t("SCAP Content")} className="col-md-3" required />
            <div className="col-md-6">
              <Select
                name="dataStreamName"
                isClearable
                options={dataStreams}
                onChange={(value) => this.fetchProfiles("dataStream", value as string)}
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
                onChange={(value) => this.fetchProfiles("tailoringFile", value as string)}
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
              />
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
