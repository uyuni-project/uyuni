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
      fileName: string | null;
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

  getCurrentFileName = (): string | null | undefined => {
    return window.scapContentData?.fileName;
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

    return Network.post(endpoint, formData, false)
      .then((response) => {
        if (response.success) {
          Utils.urlBounce("/rhn/manager/audit/scap/content");
        } else {
          const errorMessages = response.messages.map((msg) =>
            MessageUtils.error(msg)
          );
          this.setState({
            messages: <Messages items={errorMessages} />,
            isInvalid: true,
          });
        }
      })
      .catch((error) => {
        const errorMessage = MessageUtils.error(
          t("An unexpected error occurred while uploading the SCAP content.")
        );
        this.setState({
          messages: <Messages items={errorMessage} />,
          isInvalid: true,
        });
      });
  };

  renderButtons = () => {
    const buttons = [
      <SubmitButton
        key="submit"
        id="submit-btn"
        className="btn-success"
        text={this.isEditMode() ? t("Update") : t("Create")}
        disabled={this.state.isInvalid}
      />,
    ];
    return buttons;
  };

  render() {
    const isEdit = this.isEditMode();
    const currentFileName = this.getCurrentFileName();

    return (
      <TopPanel title={isEdit ? t("Edit SCAP Content") : t("Upload SCAP Content")} icon="spacewalk-icon-manage-configuration-files">
        {this.state.messages}
        <Form
          model={this.state.model}
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
            <Label name={t("SCAP Content File")} className="col-md-3" required={!isEdit} />
            <div className="col-md-6">
              <input
                type="file"
                name="scapFile"
                accept=".xml"
                className="form-control"
                required={!isEdit}
              />
              {isEdit && currentFileName && (
                <div className="help-block">
                  {t("Current file")}: <strong>{currentFileName}</strong>
                  <br />
                  {t("Upload a new file to replace the existing one")}
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
