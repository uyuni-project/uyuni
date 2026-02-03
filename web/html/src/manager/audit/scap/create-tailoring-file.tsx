import React, { useState, useRef } from "react";
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

const ENDPOINTS = {
  CREATE: "/rhn/manager/api/audit/scap/tailoring-file/create",
  UPDATE: "/rhn/manager/api/audit/scap/tailoring-file/update",
  LIST: "/rhn/manager/audit/scap/tailoring-files",
} as const;


interface TailoringFileData {
  name: string | null;
  id: number | null;
  description: string | null;
  tailoringFileName: string | null;
  isUpdate: boolean;
}

declare global {
  interface Window {
    tailoringFileData?: TailoringFileData;
  }
}

interface FormModel {
  name: string;
  description: string;
}

const TailoringFile = (): JSX.Element => {
  // 1. Derive initial data and mode once
  const initialData = window.tailoringFileData || {
    name: "",
    id: null,
    description: "",
    tailoringFileName: null,
    isUpdate: false,
  };

  const isEdit = !!initialData.isUpdate;

  // 2. State management
  const [model, setModel] = useState<FormModel>({
    name: initialData.name || "",
    description: initialData.description || "",
  });
  const [messages, setMessages] = useState<React.ReactNode>([]);
  const [isInvalid, setIsInvalid] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);

  // 3. Handlers
  const handleUpload = async () => {
    if (!formRef.current) return;

    const formData = new FormData(formRef.current);
    
    if (isEdit && initialData.id !== null) {
      formData.append("id", initialData.id.toString());
    }

    const endpoint = isEdit ? ENDPOINTS.UPDATE : ENDPOINTS.CREATE;

    try {
      const response = await Network.post(endpoint, formData, "multipart/form-data", false);
      
      if (response.success) {
        Utils.urlBounce(ENDPOINTS.LIST);
      } else {
        const errorItems = response.messages?.length 
          ? response.messages.map((msg: string) => MessageUtils.error(msg))
          : [MessageUtils.error(t("An error occurred while saving the tailoring file."))];
        setMessages(<Messages items={errorItems} />);
      }
    } catch (error: unknown) {
      const errorMessage = (error as any)?.messages?.[0] || t("An unexpected error occurred.");
      setMessages(<Messages items={MessageUtils.error(errorMessage)} />);
    }
  };

  return (
    <TopPanel
      title={t(isEdit ? "Edit Tailoring File" : "Upload Tailoring File")}
      icon="spacewalk-icon-manage-configuration-files"
    >
      {messages}
      <Form
        model={model}
        className="tailoring-file-form"
        onChange={setModel}
        onSubmit={handleUpload}
        onValidate={(valid: boolean) => setIsInvalid(!valid)}
        formRef={formRef}
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

        {isEdit && initialData.tailoringFileName && (
          <FormGroup>
            <Label name={t("Current File")} className="col-md-3" />
            <div className="col-md-6">
              <p className="form-control-static">{initialData.tailoringFileName}</p>
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
          <div className="col-md-offset-3 col-md-6">
            <SubmitButton
              id="upload-btn"
              className="btn-success"
              icon={isEdit ? "fa-edit" : "fa-plus"}
              text={t(isEdit ? "Update" : "Upload")}
              disabled={isInvalid}
            />
          </div>
        </div>
      </Form>
    </TopPanel>
  );
};

export const renderer = () => {
  const container = document.getElementById("scap-create-tailoring-file");
  if (container) {
    SpaRenderer.renderNavigationReact(<TailoringFile />, container);
  }
};
