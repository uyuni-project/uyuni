import { useEffect, useMemo, useState } from "react";

import {
  type SingleCoCoSettingsResponse,
  getSystemCoCoSettings,
  saveSystemCoCoSettings,
} from "components/coco-attestation/api";
import { CoCoSettingsForm } from "components/coco-attestation/CoCoSettingsForm";
import { Settings } from "components/coco-attestation/Utils";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Loading } from "components/utils";

import Network, { type JsonResult } from "utils/network";

interface Props {
  serverId: number;
  availableEnvironmentTypes: Record<string, string>;
  showOnScheduleOption?: boolean;
}

export const CoCoSettings: React.FC<Props> = ({
  serverId,
  availableEnvironmentTypes,
  showOnScheduleOption = true,
}: Props): JSX.Element => {
  const emptySettings: Settings = useMemo(
    () => ({
      enabled: false,
      environmentType: Object.keys(availableEnvironmentTypes)[0],
      attestOnBoot: false,
      attestOnSchedule: false,
      inputData: {},
    }),
    [availableEnvironmentTypes]
  );
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [supported, setSupported] = useState(false);
  const [loading, setLoading] = useState(true);
  const [settings, setSettings] = useState<Settings>(emptySettings);

  function handleRequestError(err: Error | JQueryXHR): void {
    setMessages(Network.responseErrorMessage(err));
    setSupported(false);
    setLoading(false);
    setSettings(emptySettings);
  }

  function handleResult(result: JsonResult<SingleCoCoSettingsResponse>): void {
    if (!result.success) {
      setMessages(MessagesUtils.error(result.messages));
      setLoading(false);
    } else if (!result.data.supported) {
      setSupported(false);
      setMessages(MessagesUtils.warning(result.messages));
      setSettings(emptySettings);
      setLoading(false);
    } else {
      setMessages(MessagesUtils.success(result.messages));
      setSupported(true);
      setSettings(result.data.settings);
      setLoading(false);
    }
  }

  function onSave(data: Settings): void {
    saveSystemCoCoSettings(serverId, data).then(handleResult, handleRequestError);
  }

  useEffect(() => {
    setLoading(true);
    getSystemCoCoSettings(serverId).then(handleResult, handleRequestError);
  }, [serverId]);

  if (loading) {
    return (
      <div className="panel panel-default">
        <Loading />
      </div>
    );
  }

  return (
    <TopPanel title={t("Settings")} icon="fa fa-pencil-square-o">
      <Messages items={messages} />
      {supported && (
        <CoCoSettingsForm
          initialData={settings}
          saveHandler={onSave}
          availableEnvironmentTypes={availableEnvironmentTypes}
          showOnScheduleOption={showOnScheduleOption}
        />
      )}
    </TopPanel>
  );
};
