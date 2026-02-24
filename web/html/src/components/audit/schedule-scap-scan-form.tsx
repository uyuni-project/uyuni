import { useState } from "react";

import { ActionSchedule } from "components/action-schedule";
import { LinkButton, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { ActionLink } from "components/links";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";

import { localizedMoment } from "utils";
import Network from "utils/network";

const ENDPOINTS = {
  PROFILES_LIST: "/rhn/manager/api/audit/profiles/list",
  POLICY_VIEW: "/rhn/manager/api/audit/scap/policy/view",
} as const;

export enum ScapContentType {
  DataStream = "dataStream",
  TailoringFile = "tailoringFile",
}

interface Item {
  id: number | string;
  name?: string;
  title?: string;
  policyName?: string;
}

interface ScapContent {
  id: number;
  name: string;
}

export interface ScheduleScapScanFormProps {
  scapContentList: ScapContent[];
  tailoringFiles: Item[];
  scapPolicies: Item[];
  onSubmit: (model: any) => Promise<any>;
  earliest?: any;
  minions?: { id: number }[];
  createRecurringLink?: string;
  checkMemory?: boolean;
}

interface Profile {
  id: string;
  title: string;
}

interface ScapModel {
  dataStreamName?: string | number;
  xccdfProfileId?: string;
  tailoringFile?: string | number;
  tailoringProfileID?: string;
  ovalFiles?: string;
  advancedArgs?: string;
  fetchRemoteResources?: boolean;
}

export const ScheduleScapScanForm = ({
  scapContentList = [],
  tailoringFiles = [],
  scapPolicies = [],
  onSubmit,
  earliest: initialEarliest,
  minions,
  createRecurringLink,
}: ScheduleScapScanFormProps): JSX.Element => {
  const [model, setModel] = useState<ScapModel>({});
  const [xccdfProfiles, setXccdfProfiles] = useState<Profile[]>([]);
  const [tailoringFileProfiles, setTailoringFileProfiles] = useState<Profile[]>([]);
  const [earliest, setEarliest] = useState(initialEarliest || localizedMoment());
  const [selectedScapPolicy, setSelectedScapPolicy] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [isInvalid, setIsInvalid] = useState(false);

  const getProfiles = async (type: ScapContentType, id: string | number) => {
    try {
      const data = await Network.get(`${ENDPOINTS.PROFILES_LIST}/${type}/${id}`);
      if (type === ScapContentType.TailoringFile) {
        setTailoringFileProfiles(data);
      } else {
        setXccdfProfiles(data);
      }
      return data;
    } catch (error: unknown) {
      setMessages(Network.responseErrorMessage(error as any));
    }
  };

  const handleScapPolicyChange = async (value: number | null) => {
    if (!value) {
      setSelectedScapPolicy(null);
      setModel({});
      setXccdfProfiles([]);
      setTailoringFileProfiles([]);
      return;
    }

    setSelectedScapPolicy(value);
    try {
      const data = await Network.get(`${ENDPOINTS.POLICY_VIEW}/${value}`);

      const newXccdfProfiles = data.xccdfProfileId
        ? [{ id: data.xccdfProfileId, title: data.xccdfProfileTitle || data.xccdfProfileId }]
        : [];

      const newTailoringFileProfiles = data.tailoringProfileId
        ? [{ id: data.tailoringProfileId, title: data.tailoringProfileTitle || data.tailoringProfileId }]
        : [];

      setModel((prev) => ({
        ...prev,
        dataStreamName: data.scapContentId,
        xccdfProfileId: data.xccdfProfileId,
        tailoringFile: data.tailoringFileId,
        tailoringProfileID: data.tailoringProfileId,
        ovalFiles: data.ovalFiles || "",
        advancedArgs: data.advancedArgs || "",
        fetchRemoteResources: data.fetchRemoteResources || false,
      }));

      setXccdfProfiles(newXccdfProfiles);
      setTailoringFileProfiles(newTailoringFileProfiles);
    } catch (error: unknown) {
      setMessages(Network.responseErrorMessage(error as any));
    }
  };

  const handleSubmit = async () => {
    const submitModel = {
      ...model,
      earliest,
      selectedScapPolicy,
    };

    try {
      const data = await onSubmit(submitModel);
      const msg = MessagesUtils.info(
        <span>
          {t("SCAP scan has been ")}
          <ActionLink id={data}>{t("scheduled.")}</ActionLink>
        </span>
      );
      setMessages(msg);
    } catch (error: unknown) {
      setMessages(Network.responseErrorMessage(error as any));
    }
  };

  const renderButtons = () => (
    <SubmitButton id="create-btn" className="btn-success" icon="fa-clock-o" text={t("Schedule")} disabled={isInvalid} />
  );

  return (
    <div>
      <Messages items={messages} />

      <Panel headingLevel="h3" title={t("Schedule New XCCDF Scan")}>
        <Form
          model={model}
          className="schedule-scap-scan-form"
          onChange={setModel}
          onSubmit={handleSubmit}
          onValidate={(valid: boolean) => setIsInvalid(!valid)}
        >
          <FormGroup>
            <Label name={t("SCAP Policy")} className="col-md-3" />
            <div className="col-md-6">
              <Select
                name="scapPolicy"
                placeholder={t("No Policy Selected")}
                isClearable
                onChange={(value) => handleScapPolicyChange(value as number)}
                options={scapPolicies.map((k) => ({ value: k.id, label: k.policyName || "" }))}
              />
            </div>
          </FormGroup>

          <FormGroup>
            <Label name={t("SCAP Content")} className="col-md-3" required />
            <div className="col-md-6">
              <Select
                name="dataStreamName"
                isClearable
                value={model.dataStreamName}
                disabled={!!selectedScapPolicy}
                onChange={(value) => {
                  setModel((prev) => ({ ...prev, dataStreamName: value as string }));
                  if (value) {
                    getProfiles(ScapContentType.DataStream, value as string);
                  }
                }}
                options={scapContentList
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
                value={model.xccdfProfileId}
                disabled={!!selectedScapPolicy}
                isClearable
                onChange={(value) => setModel((prev) => ({ ...prev, xccdfProfileId: value as string }))}
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
                  setModel((prev) => ({ ...prev, tailoringFile: value as string }));
                  if (value) {
                    getProfiles(ScapContentType.TailoringFile, value as string);
                  }
                }}
                isClearable
                value={model.tailoringFile}
                disabled={!!selectedScapPolicy}
                options={tailoringFiles.map((k) => ({ value: k.id, label: k.name || "" }))}
              />
            </div>
          </FormGroup>

          <FormGroup>
            <Label name={t("Profile from Tailoring File")} className="col-md-3" />
            <div className="col-md-6">
              <Select
                name="tailoringProfileID"
                placeholder={t("Select profile...")}
                onChange={(value) => setModel((prev) => ({ ...prev, tailoringProfileID: value as string }))}
                isClearable
                value={model.tailoringProfileID}
                disabled={!!selectedScapPolicy}
                options={tailoringFileProfiles.map((k) => ({ value: k.id, label: k.title }))}
              />
            </div>
          </FormGroup>

          <FormGroup>
            <Label name={t("Advanced Arguments")} className="col-md-3" />
            <div className="col-md-6">
              <Text
                name="advancedArgs"
                placeholder={t("e.g: --rule xccdf_org.ssgproject.content_rule_package_screen_installed --remediate")}
                title={t("Additional command-line arguments for oscap")}
              />
            </div>
          </FormGroup>
          <FormGroup>
            <Label name={t("OVAL Files")} className="col-md-3" />
            <div className="col-md-6">
              <Text
                name="ovalFiles"
                placeholder={t("e.g: /usr/share/xml/scap/suse-sles15-cve.xml")}
                title={t("Comma-separated list of OVAL files")}
              />
              <span className="help-block">
                {t("Note: Paths to local OVAL definitions on target system (comma separated)")}
              </span>
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
                    checked={model.fetchRemoteResources || false}
                    disabled={!!selectedScapPolicy}
                    onChange={(e) => {
                      setModel((prev) => ({ ...prev, fetchRemoteResources: e.target.checked }));
                    }}
                  />
                  <span className="fetch-remote-help">
                    {t(
                      "This requires internet and a lot of memory, make sure this minion has enough memory available!"
                    )}
                  </span>
                </label>
              </div>
            </div>
          </FormGroup>

          <div className="panel-body">
            <ActionSchedule
              earliest={earliest}
              onDateTimeChanged={setEarliest}
              systemIds={minions?.map((m) => m.id)}
              actionType="states.apply"
            />
          </div>

          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">
              {renderButtons()}
              {createRecurringLink && (
                <LinkButton
                  icon="fa-plus"
                  href={createRecurringLink}
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
};
