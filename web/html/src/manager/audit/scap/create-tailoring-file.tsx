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

// Extend window to include tailoringFileData
declare global {
  interface Window {
    tailoringFileData?: {
      name: string | null;
      id: number | null;
      description: string | null;
      tailoringFileName: string | null;
      isUpdate: boolean;
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

class TailoringFile extends React.Component<Props, State> {
  form?: HTMLFormElement;

  constructor(props: Props) {
    super(props);
    // Initialize model from backend data if editing
    const data = window.tailoringFileData || { name: null, id: null, description: null, tailoringFileName: null, isUpdate: false };
    this.state = {
      model: {
        name: data.name || "",
        description: data.description || "",
      },
      messages: [],
    };
  }

  isEditMode = (): boolean => {
    return !!window.tailoringFileData?.isUpdate;
  };

  getTailoringFileId = (): number | null | undefined => {
    return window.tailoringFileData?.id;
  };

  getCurrentFileName = (): string | null | undefined => {
    return window.tailoringFileData?.tailoringFileName;
  };

  onUpload = () => {
    const formData = new FormData(this.form);
    const isEdit = this.isEditMode();

    // Add ID to form data if editing
    if (isEdit) {
      const id = this.getTailoringFileId();
      if (id != null) {
        formData.append("id", id.toString());
      }
    }

    const endpoint = isEdit
      ? "/rhn/manager/api/audit/scap/tailoring-file/update"
      : "/rhn/manager/api/audit/scap/tailoring-file/create";

    Network.post(endpoint, formData, "multipart/form-data", false)
      .then((response) => {
        if (response.success) {
          Utils.urlBounce("/rhn/manager/audit/scap/tailoring-files");
        } else {
          // Handle error response from backend
          const errorMessages = response.messages
            ? response.messages.map((msg: string) => MessageUtils.error(msg))
            : [MessageUtils.error("An error occurred while saving the tailoring file.")];
          this.setState({
            messages: <Messages items={errorMessages} />,
          });
        }
      })
      .catch((error) => {
        console.error("Upload failed:", error);
        const errorMessage = MessageUtils.error(
          error.messages?.[0] || "An unexpected error occurred while saving the tailoring file."
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
      />
    );
  };

  bindForm = (form: HTMLFormElement) => {
    this.form = form;
  };

  render() {
    const isEdit = this.isEditMode();
    const currentFileName = this.getCurrentFileName();

    return (
      <TopPanel
        title={t(isEdit ? "Edit Tailoring File" : "Upload Tailoring File")}
        icon="spacewalk-icon-manage-configuration-files"
      >
        {this.state.messages}
        <Form
          model={this.state.model}
          className="tailoring-file-form"
          onChange={this.onFormChange}
          onSubmit={this.onUpload}
          onValidate={this.onValidate}
          formRef={this.bindForm}
        >
          <Text
            name="name"
            label={t("Name")}
            required={true}
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
          {isEdit && currentFileName && (
            <FormGroup>
              <Label name={t("Current File")} className="col-md-3" />
              <div className="col-md-6">
                <p className="form-control-static">{currentFileName}</p>
              </div>
            </FormGroup>
          )}
          <FormGroup>
            <Label
              name={t(isEdit ? "Replace File (optional)" : "Tailoring File")}
              className="col-md-3"
              required={!isEdit}
            />
            <div className="col-md-6">
              <input
                name="tailoring_file"
                type="file"
                className="form-control"
                accept=".xml"
                required={!isEdit}
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

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(
    <TailoringFile />,
    document.getElementById("scap-create-tailoring-file")
  );
};
