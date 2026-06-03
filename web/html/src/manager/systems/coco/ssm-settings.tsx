import { useMemo, useState } from "react";

import { CoCoSettingsForm } from "components/coco-attestation/CoCoSettingsForm";
import { Settings } from "components/coco-attestation/Utils";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { TargetSystems } from "components/target-systems";

import Network from "utils/network";

import { CoCoSystemData } from "./types";

interface Props {
  systemSupport: CoCoSystemData[];
  availableEnvironmentTypes: Record<string, string>;
}

export const CoCoSSMSettings: React.FC<Props> = ({ systemSupport, availableEnvironmentTypes }: Props): JSX.Element => {
  const [messages, setMessages] = useState<MessageType[]>([]);

  const emptySettings = useMemo<Settings>(
    () => ({
      enabled: false,
      environmentType: Object.values(availableEnvironmentTypes)[0],
      attestOnBoot: false,
      attestOnSchedule: false,
      inputData: {},
    }),
    [availableEnvironmentTypes]
  );

  function onSave(settingsPromise: Promise<Settings>) {
    settingsPromise
      .then((data) => ({
        serverIds: systemSupport.filter((system) => system.cocoSupport).map((system) => system.id),
        settings: data,
      }))
      .then((request) => Network.post(`/rhn/manager/api/systems/coco/settings`, request))
      .then(
        (response) =>
          setMessages(
            response.success ? MessagesUtils.success(response.messages) : MessagesUtils.error(response.messages)
          ),
        (err) => setMessages(Network.responseErrorMessage(err))
      );
  }

  return (
    <>
      <TopPanel title="Confidential Computing Settings">
        <Messages items={messages} />
        <CoCoSettingsForm
          initialData={emptySettings}
          saveHandler={onSave}
          availableEnvironmentTypes={availableEnvironmentTypes}
          showOnScheduleOption={false}
        />
      </TopPanel>
      <TargetSystems systemsData={systemSupport}>
        <Column
          columnKey="cocoSupport"
          header={t("Confidential Computing Capability")}
          cell={(system: CoCoSystemData) => (system.cocoSupport ? t("Yes") : t("No"))}
        />
      </TargetSystems>
    </>
  );
};
