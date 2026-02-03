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
  CREATE: "/rhn/manager/api/audit/scap/content/create",
  UPDATE: "/rhn/manager/api/audit/scap/content/update",
  LIST: "/rhn/manager/audit/scap/content",
} as const;

interface ScapContentData {
  name: string | null;
  id: number | null;
  description: string | null;
  dataStreamFileName: string | null;
  xccdfFileName: string | null;
}

declare global {
  interface Window {
    scapContentData?: ScapContentData;
  }
}

interface FormModel {
  name: string;
  description: string;
}

const ScapContentForm = (): JSX.Element => {
  const initialData = window.scapContentData || {
    name: "",
    id: null,
    description: "",
    dataStreamFileName: null,
    xccdfFileName: null,
  };

  const [model, setModel] = useState<FormModel>({
    name: initialData.name || "",
    description: initialData.description || "",
  });
  const [messages, setMessages] = useState<React.ReactNode>([]);
  const [isInvalid, setIsInvalid] = useState(false);
  const formRef = useRef<HTMLFormElement>(null);

  const isEdit = initialData.id != null;

  const handleUpload = async () => {
    if (!formRef.current) return;

    const formData = new FormData(formRef.current);

    if (isEdit) {
      formData.append("id", initialData.id!.toString());
    }

    const endpoint = isEdit ? ENDPOINTS.UPDATE : ENDPOINTS.CREATE;

    try {
      const response = await Network.post(endpoint, formData, "multipart/form-data", false);

      if (response.success) {
        Utils.urlBounce(ENDPOINTS.LIST);
      } else {
        const errorItems = response.messages?.length
          ? MessageUtils.error(response.messages)
          : MessageUtils.error(t("An error occurred while uploading the SCAP content."));
        setMessages(<Messages items={errorItems} />);
      }
    } catch (error: unknown) {
      const errorMessage = (error as any)?.messages?.[0] || t("An unexpected error occurred.");
      setMessages(<Messages items={MessageUtils.error(errorMessage)} />);
    }
  };

  return (
    <TopPanel
      title={t(isEdit ? "Edit SCAP Content" : "Upload SCAP Content")}
      icon="spacewalk-icon-manage-configuration-files"
    >
      {messages}
      <Form
        model={model}
        onChange={setModel}
        onValidate={(valid: boolean) => setIsInvalid(!valid)}
        onSubmit={handleUpload}
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
            {isEdit && initialData.dataStreamFileName && (
              <div className="help-block">
                {t("Current file")}: <strong>{initialData.dataStreamFileName}</strong>
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
            {isEdit && initialData.xccdfFileName && (
              <div className="help-block">
                {t("Current file")}: <strong>{initialData.xccdfFileName}</strong>
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
  const container = document.getElementById("scap-content-form");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScapContentForm />, container);
  }
};
