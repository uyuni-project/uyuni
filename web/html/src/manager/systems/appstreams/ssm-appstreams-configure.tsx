import * as React from "react";

import { AppStreamModule, ChannelAppStream } from "manager/appstreams/appstreams.type";
import { AppStreamsChangesConfirm } from "manager/appstreams/changes-confirm-appstreams";
import { getStreamName } from "manager/appstreams/utils";

import { ActionChain } from "components/action-schedule";
import { Messages, MessageType, Utils as MessageUtils } from "components/messages/messages";

import { DISABLE, NO_CHANGE, SSMAppStreamsList } from "./ssm-appstreams-configure-list";

export type Props = { channelAppStreams: ChannelAppStream };

export const SSMAppStreamsConfigure: React.FC<Props> = ({ channelAppStreams }: Props): JSX.Element => {
  const { appStreams } = channelAppStreams;

  const [toEnable, setToEnable] = React.useState<string[]>([]);
  const [toDisable, setToDisable] = React.useState<string[]>([]);
  const [showConfirm, setShowConfirm] = React.useState<boolean>(false);
  const [scheduledMsg, setScheduledMsg] = React.useState<MessageType[]>([]);

  const handleActionChange = (moduleKey: string, selectedValue: string) => {
    const allAppStreamsOfModule: AppStreamModule[] = appStreams[moduleKey];
    const allStreamNames = new Set(allAppStreamsOfModule.map(getStreamName));
    const nextToEnable = toEnable.filter((name) => !allStreamNames.has(name));
    const nextToDisable = toDisable.filter((key) => key !== moduleKey);

    if (selectedValue === DISABLE) {
      nextToDisable.push(moduleKey);
    } else if (selectedValue !== NO_CHANGE) {
      nextToEnable.push(selectedValue);
    }

    setToEnable(nextToEnable);
    setToDisable(nextToDisable);
  };

  const handleReset = () => {
    setToEnable([]);
    setToDisable([]);
    setScheduledMsg([]);
  };

  const handleSubmitChanges = () => {
    setShowConfirm(true);
  };

  const handleCancelChanges = () => {
    setScheduledMsg([]);
    setShowConfirm(false);
  };

  const handleConfirmChanges = (id: number, actionChain?: ActionChain | null) => {
    setShowConfirm(false);
    handleReset();

    const msg = MessageUtils.info(
      actionChain ? (
        <span>
          {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
            name: actionChain.text,
            link: (str) => <a href={`/rhn/schedule/ActionChain.do?id=${id}`}>{str}</a>,
          })}
        </span>
      ) : (
        <span>
          {t("Updating the selection of AppStream modules has been <link>scheduled</link>.", {
            link: (str) => <a href={`/rhn/schedule/ActionDetails.do?aid=${id}`}>{str}</a>,
          })}
        </span>
      )
    );
    setScheduledMsg([...msg]);
  };

  const handleConfirmError = (msg: MessageType[]) => {
    setScheduledMsg([...msg]);
  };

  const getModuleAction = (moduleKey: string): string => {
    const allModulesInGroup = appStreams[moduleKey];
    const allStreamNamesInGroup = new Set(allModulesInGroup.map(getStreamName));
    const enablingStreamName = toEnable.find((name) => allStreamNamesInGroup.has(name));

    if (enablingStreamName) {
      return enablingStreamName;
    }
    if (toDisable.includes(moduleKey)) {
      return DISABLE;
    }

    return NO_CHANGE;
  };

  const numberOfChanges = toEnable.length + toDisable.length;

  const showContent = () => {
    if (showConfirm) {
      return (
        <AppStreamsChangesConfirm
          channelId={channelAppStreams.channel.id}
          toEnable={toEnable}
          toDisable={toDisable}
          apiURL="/rhn/manager/api/ssm/appstreams/save"
          onCancelClick={handleCancelChanges}
          onConfirm={handleConfirmChanges}
          onError={handleConfirmError}
        />
      );
    } else {
      return (
        <SSMAppStreamsList
          channelAppStreams={channelAppStreams}
          onReset={handleReset}
          onSubmitChanges={handleSubmitChanges}
          numberOfChanges={numberOfChanges}
          onActionChange={handleActionChange}
          getModuleAction={getModuleAction}
        />
      );
    }
  };

  return (
    <>
      <Messages items={scheduledMsg} />
      <h2>
        <i className={"fa spacewalk-icon-salt-add"} />
        {t("AppStreams")}
      </h2>
      {showContent()}
    </>
  );
};
