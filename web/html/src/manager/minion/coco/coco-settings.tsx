import { useEffect, useState } from "react";

import { CoCoSettingsForm } from "components/coco-attestation/CoCoSettingsForm";
import { Settings } from "components/coco-attestation/Utils";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Loading } from "components/utils";

import Network from "utils/network";

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
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [supported, setSupported] = useState(false);
  const [loading, setLoading] = useState(true);
  const [settings, setSettings] = useState<Settings>({
    enabled: false,
    environmentType: Object.values(availableEnvironmentTypes)[0],
    attestOnBoot: false,
    attestOnSchedule: false,
  });

  function handleRequestError(err: Error | JQueryXHR): void {
    setMessages(Network.responseErrorMessage(err));
    setSupported(false);
    setLoading(false);
    setSettings({
      enabled: false,
      environmentType: Object.values(availableEnvironmentTypes)[0],
      attestOnBoot: false,
      attestOnSchedule: false,
    });
  }

  function handleResult(result: any): void {
    if (!result.success) {
      setMessages(MessagesUtils.error(result.messages));
      setLoading(false);
    } else if (!result.data.supported) {
      setSupported(false);
      setMessages(MessagesUtils.warning(result.messages));
      setLoading(false);
    } else {
      setMessages(MessagesUtils.success(result.messages));
      setSupported(true);
      setSettings(result.data);
      setLoading(false);
    }
  }

  function onSave(data: Settings): void {
    Network.post(`/rhn/manager/api/systems/${serverId}/details/coco/settings`, data).then(
      handleResult,
      handleRequestError
    );
  }

  useEffect(() => {
    setLoading(true);
    Network.get(`/rhn/manager/api/systems/${serverId}/details/coco/settings`).then(handleResult, handleRequestError);
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
