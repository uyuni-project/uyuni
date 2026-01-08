import { useState } from "react";

import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { MessageType } from "components/messages/messages";

import { localizedMoment } from "utils";
import Network from "utils/network";

type AppStreamsChangesConfirmProps = {
  sid?: string;
  channelId?: number;
  toEnable: string[];
  toDisable: string[];
  apiURL: string;
  onConfirm: (responseData: number, actionChain: ActionChain | null) => void;
  onError: (errorMessages: MessageType[]) => void;
  onCancelClick: () => void;
};

export const AppStreamsChangesConfirm = ({
  sid,
  channelId,
  toEnable,
  toDisable,
  apiURL,
  onConfirm,
  onError,
  onCancelClick,
}: AppStreamsChangesConfirmProps) => {
  const [earliest, setEarliest] = useState(localizedMoment());
  const [actionChain, setActionChain] = useState<ActionChain | null>(null);
  const payload = {
    toEnable: toEnable,
    toDisable: toDisable,
    actionChainLabel: actionChain?.text,
    earliest: earliest,
  };

  if (sid) {
    payload["sid"] = sid;
  }
  if (channelId) {
    payload["channelId"] = channelId;
  }

  const applyChanges = () => {
    const request = Network.post(apiURL, payload)
      .then((data) => {
        onConfirm(data.data, actionChain);
      })
      .catch((jqXHR) => onError(Network.responseErrorMessage(jqXHR)));

    return request;
  };

  return (
    <>
      <p>{t("Please review the changes below before scheduling an action to apply the changes.")}</p>
      <div className="text-right margin-bottom-sm">
        <div className="btn-group">
          <Button id="cancelAppStreamChanges" className="btn btn-default" text={t("Cancel")} handler={onCancelClick} />
          <AsyncButton
            id="confirmAppStreamChanges"
            defaultType="btn-primary"
            text={t("Confirm")}
            action={applyChanges}
          />
        </div>
      </div>

      <div className="panel panel-default">
        <div className="panel-body">
          <h5>Changes Summary:</h5>
          {toEnable.length === 0 || (
            <div>
              <p>{t("Streams to be enabled:")}</p>
              <ul>
                {toEnable.map((it) => (
                  <li key={it}>{it}</li>
                ))}
              </ul>
            </div>
          )}

          {toDisable.length === 0 || (
            <div>
              <p>{t("Streams to be disabled:")}</p>
              <ul>
                {toDisable.map((it) => (
                  <li key={it}>{it}</li>
                ))}
              </ul>
            </div>
          )}
          <ActionSchedule
            earliest={earliest}
            actionChains={window.actionChains}
            onActionChainChanged={(ac) => setActionChain(ac)}
            onDateTimeChanged={(date) => setEarliest(date)}
            systemIds={window.minions?.map((m) => m.id)}
            actionType="states.apply"
          />
        </div>
      </div>
    </>
  );
};
