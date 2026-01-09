import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Text } from "components/input/text/Text";
import { TextArea } from "components/input/text-area/TextArea";
import { Messages, Utils as MessageUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";

import { Utils } from "utils/functions";
import Network from "utils/network";

// Extend window to include scapContentData
declare global {
  interface Window {
    scapContentData?: {
      name: string | null;
      id: number | null;
      description: string | null;
      dataStreamFileName: string | null;
      xccdfFileName: string | null;
    };
  }
}

type Props = {};

type State = {
  model: {
    name: string;
    description: string;
  };
  messages: React.ReactNode;
  isInvalid?: boolean;
};

class ScapContentForm extends React.Component<Props, State> {
  form?: HTMLFormElement;

  constructor(props: Props) {
    super(props);
    // Initialize model from backend data if editing
    const data = window.scapContentData || { name: null, id: null, description: null, fileName: null };
    this.state = {
      model: {
        name: data.name || "",
        description: data.description || "",
      },
      messages: [],
    };
  }

  isEditMode = (): boolean => {
    return window.scapContentData?.id != null;
  };

  getScapContentId = (): number | null | undefined => {
    return window.scapContentData?.id;
  };

  getCurrentDataStreamFileName = (): string | null | undefined => {
    return window.scapContentData?.dataStreamFileName;
  };

  onUpload = () => {
    const formData = new FormData(this.form);
    const isEdit = this.isEditMode();

    // Add ID to form data if editing
    if (isEdit) {
      const id = this.getScapContentId();
      if (id != null) {
        formData.append("id", id.toString());
      }
    }

    const endpoint = isEdit
      ? "/rhn/manager/api/audit/scap/content/update"
      : "/rhn/manager/api/audit/scap/content/create";

    Network.post(endpoint, formData, "multipart/form-data", false)
      .then((response) => {
        if (response.success) {
          Utils.urlBounce("/rhn/manager/audit/scap/content");
        } else {
          // Handle error response from backend
          const errorMessages = response.messages && response.messages.length > 0
            ? MessageUtils.error(response.messages)
            : MessageUtils.error("An error occurred while uploading the SCAP content.");
          this.setState({
            messages: <Messages items={errorMessages} />,
          });
        }
      })
      .catch((error) => {
        console.error("Upload failed:", error);
        const errorMessage = MessageUtils.error(
          error.messages?.[0] || "An unexpected error occurred while uploading the SCAP content."
        );
        this.setState({
          messages: <Messages items={errorMessage} />,
        });
      });
  };

  onFormChange = (model: State["model"]) => {
    this.setState({ model });
  };

  onValidate = (isValid: boolean) => {
    this.setState({ isInvalid: !isValid });
  };

  renderButtons = () => {
    const isEdit = this.isEditMode();
    return (
      <SubmitButton
        key="upload-btn"
        id="upload-btn"
        className="btn-success"
        icon={isEdit ? "fa-edit" : "fa-plus"}
        text={t(isEdit ? "Update" : "Upload")}
        disabled={this.state.isInvalid}
      />
    );
  };

  render() {
    const isEdit = this.isEditMode();
    const currentDataStreamFileName = this.getCurrentDataStreamFileName();

    return (
      <TopPanel title={isEdit ? t("Edit SCAP Content") : t("Upload SCAP Content")} icon="spacewalk-icon-manage-configuration-files">
        {this.state.messages}
        <Form
          model={this.state.model}
          onChange={this.onFormChange}
          onValidate={this.onValidate}
          onSubmit={this.onUpload}
          formRef={(form) => (this.form = form)}
        >
          <Text
            name="name"
            label={t("Name")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <TextArea
            name="description"
            label={t("Description")}
            labelClass="col-md-3"
            divClass="col-md-6"
            rows={4}
          />
          <FormGroup>
            <Label name={t("SCAP Datastream File")} className="col-md-3" required={!isEdit} />
            <div className="col-md-6">
              <input
                type="file"
                name="scapFile"
                accept=".xml"
                className="form-control"
                required={!isEdit}
              />
              {isEdit && currentDataStreamFileName && (
                <div className="help-block">
                  {t("Current file")}: <strong>{currentDataStreamFileName}</strong>
                  <br />
                  {t("Upload a new file to replace the existing one")}
                </div>
              )}
              {!isEdit && (
                <div className="help-block">
                  {t("Upload the DataStream file (*-ds.xml)")}
                </div>
              )}
            </div>
          </FormGroup>
          <FormGroup>
            <Label name={t("XCCDF File")} className="col-md-3" required={!isEdit} />
            <div className="col-md-6">
              <input
                type="file"
                name="xccdfFile"
                accept=".xml"
                className="form-control"
                required={!isEdit}
              />
              {isEdit && window.scapContentData?.xccdfFileName && (
                <div className="help-block">
                  {t("Current file")}: <strong>{window.scapContentData.xccdfFileName}</strong>
                  <br />
                  {t("Upload a new file to replace the existing one")}
                </div>
              )}
              {!isEdit && (
                <div className="help-block">
                  {t("Upload the XCCDF file (*-xccdf.xml)")}
                </div>
              )}
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

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(
    <ScapContentForm />,
    document.getElementById("scap-content-form")
  );
};
