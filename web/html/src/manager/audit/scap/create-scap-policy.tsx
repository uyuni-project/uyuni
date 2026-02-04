import { useEffect, useRef, useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { LinkButton, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { TextArea } from "components/input/text-area/TextArea";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

const ENDPOINTS = {
  CREATE: "/rhn/manager/api/audit/scap/policy/create",
  UPDATE: "/rhn/manager/api/audit/scap/policy/update",
  LIST: "/rhn/manager/audit/scap/policies",
  PROFILES: "/rhn/manager/api/audit/profiles/list",
} as const;

interface ScapContent {
  id: number;
  dataStreamFileName: string;
}

interface TailoringFile {
  id: number;
  name: string;
  fileName: string;
}

interface Profile {
  id: string;
  title: string;
}

interface PolicyModel {
  id?: number;
  policyName?: string;
  description?: string;
  scapContentId?: number;
  xccdfProfileId?: string;
  tailoringFile?: number;
  tailoringProfileId?: string;
  ovalFiles?: string;
  advancedArgs?: string;
  fetchRemoteResources?: boolean;
}

interface ScapPolicyPageData {
  policyData: PolicyModel | null;
  isEditMode: boolean;
  isReadOnly: boolean;
  scapContentList: ScapContent[];
  tailoringFiles: TailoringFile[];
}

declare global {
  interface Window {
    scapPolicyPageData?: ScapPolicyPageData;
  }
}

interface SelectOption extends Record<string, unknown> {
  value: number | string;
  label: string;
}

const ScapPolicy = (): JSX.Element => {
  const pageData = window.scapPolicyPageData;
  const policyData = pageData?.policyData || null;
  const isEditMode = pageData?.isEditMode || false;

  const [model, setModel] = useState<PolicyModel>(policyData || {});
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [xccdfProfiles, setXccdfProfiles] = useState<Profile[]>([]);
  const [tailoringFileProfiles, setTailoringFileProfiles] = useState<Profile[]>([]);
  const formRef = useRef<HTMLFormElement>(null);

  const tailoringFiles: SelectOption[] = (pageData?.tailoringFiles || []).map((file) => ({
    value: file.id,
    label: file.name,
  }));

  const dataStreams: SelectOption[] = (pageData?.scapContentList || []).map((content) => ({
    value: content.id,
    label: content.dataStreamFileName.replace("-ds.xml", "").toUpperCase(),
  }));

  // Load profiles on mount in edit mode
  useEffect(() => {
    if (isEditMode && model.scapContentId) {
      fetchProfiles("dataStream", model.scapContentId);
    }
    if (isEditMode && model.tailoringFile) {
      fetchProfiles("tailoringFile", model.tailoringFile);
    }
  }, []); // Only run on mount

  const fetchProfiles = async (type: string, value: string | number) => {
    if (!value) return;

    try {
      const data = await Network.get(`${ENDPOINTS.PROFILES}/${type}/${value}`);
      if (type === "tailoringFile") {
        setTailoringFileProfiles(data || []);
      } else {
        setXccdfProfiles(data || []);
      }
    } catch (error: unknown) {
      const errorMessages = Network.responseErrorMessage(error as any);
      setMessages(errorMessages);
    }
  };

  const onSubmit = async () => {
    try {
      // Validate required Select fields
      if (!model.scapContentId) {
        setMessages([{ severity: "error", text: t("SCAP Content is required") }]);
        return;
      }
      if (!model.xccdfProfileId) {
        setMessages([{ severity: "error", text: t("XCCDF Profile is required") }]);
        return;
      }

      if (!formRef.current) return;

      const formData = new FormData(formRef.current);
      const jsonPayload: any = Object.fromEntries(formData.entries());

      // Add policy ID for update
      if (isEditMode && model.id) {
        jsonPayload.id = model.id;
      }

      // Explicitly add checkbox value since unchecked checkboxes don't submit in FormData
      jsonPayload.fetchRemoteResources = model.fetchRemoteResources || false;

      const endpoint = isEditMode ? ENDPOINTS.UPDATE : ENDPOINTS.CREATE;

      const response = await Network.post(endpoint, jsonPayload);

      if (response.success) {
        window.location.href = ENDPOINTS.LIST;
      } else {
        setMessages(MessageUtils.error(response.messages));
      }
    } catch (error: unknown) {
      setMessages([{ severity: "error", text: t("Unexpected error.") }]);
    }
  };

  const title = isEditMode ? t("Edit Compliance Policy") : t("Create Compliance Policy");

  return (
    <TopPanel title={title} icon="spacewalk-icon-manage-configuration-files">
      <Messages items={messages} />
      <Form model={model} className="scap-policy-form" onChange={setModel} onSubmit={onSubmit} formRef={formRef}>
        <Text name="policyName" label={t("Name")} required labelClass="col-md-3" divClass="col-md-6" />

        <TextArea name="description" label={t("Description")} labelClass="col-md-3" divClass="col-md-6" />

        <FormGroup>
          <Label name={t("SCAP Content")} className="col-md-3" required />
          <div className="col-md-6">
            <Select
              name="scapContentId"
              isClearable
              options={dataStreams}
              value={model.scapContentId}
              onChange={(value) => {
                setModel({ ...model, scapContentId: value as number });
                fetchProfiles("dataStream", value as number);
              }}
            />
          </div>
        </FormGroup>

        <FormGroup>
          <Label name={t("XCCDF Profile")} className="col-md-3" required />
          <div className="col-md-6">
            <Select
              name="xccdfProfileId"
              isClearable
              options={xccdfProfiles.map((profile) => ({ value: profile.id, label: profile.title }))}
              value={model.xccdfProfileId}
              onChange={(value) => {
                setModel({ ...model, xccdfProfileId: value as string });
              }}
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
                setModel({ ...model, tailoringFile: value as number });
                fetchProfiles("tailoringFile", value as number);
              }}
            />
          </div>
        </FormGroup>

        <FormGroup>
          <Label name={t("Tailoring Profile")} className="col-md-3" />
          <div className="col-md-6">
            <Select
              name="tailoringProfileId"
              isClearable
              options={tailoringFileProfiles.map((profile) => ({ value: profile.id, label: profile.title }))}
              value={model.tailoringProfileId}
              onChange={(value) => {
                setModel({ ...model, tailoringProfileId: value as string });
              }}
            />
          </div>
        </FormGroup>

        <FormGroup>
          <Label name={t("OVAL Files")} className="col-md-3" />
          <div className="col-md-6">
            <Text
              name="ovalFiles"
              placeholder={t("e.g: file1.xml, file2.xml")}
              title={t("Comma-separated list of OVAL files")}
            />
          </div>
        </FormGroup>

        <FormGroup>
          <Label name={t("Advanced Arguments")} className="col-md-3" />
          <div className="col-md-6">
            <Text
              name="advancedArgs"
              placeholder={t("e.g: --results --report")}
              title={t("Additional command-line arguments for oscap")}
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
                  onChange={(e) => {
                    setModel({ ...model, fetchRemoteResources: e.target.checked });
                  }}
                />
                <span className="fetch-remote-help">
                  {t("This requires internet and a lot of memory, make sure this minion has enough memory available!")}
                </span>
              </label>
            </div>
          </div>
        </FormGroup>

        <hr />

        <div className="form-group">
          <div className="col-md-offset-3 col-md-6">
            {isEditMode && (
              <LinkButton
                id="back-btn"
                className="btn-default"
                icon="fa-arrow-left"
                text={t("Back to List")}
                href={ENDPOINTS.LIST}
              />
            )}
            <SubmitButton
              id="submit-btn"
              className="btn-success"
              icon={isEditMode ? "fa-save" : "fa-plus"}
              text={isEditMode ? t("Update") : t("Create")}
            />
          </div>
        </div>
      </Form>
    </TopPanel>
  );
};

export const renderer = () => {
  const container = document.getElementById("scap-create-policy");
  if (container) {
    SpaRenderer.renderNavigationReact(<ScapPolicy />, container);
  }
};
