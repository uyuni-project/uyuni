import { useState } from "react";

import { ActionChain, ActionSchedule } from "components/action-schedule";
import { Button } from "components/buttons";
import { ActionChainLink, ActionLink } from "components/links";

import { localizedMoment } from "utils";
import Network from "utils/network";

export const AppStreamsChangesConfirm = ({ toEnable, toDisable, onConfirm, onCancelClick }) => {
  const [earliest, setEarliest] = useState(localizedMoment);
  const [actionChain, setActionChain] = useState<ActionChain | null>(null);

  const applyChanges = () => {
    const request = Network.post("/rhn/manager/api/appstreams/save", {
      sid: window.serverId,
      toEnable: toEnable,
      toDisable: toDisable,
    }).then((data) => {
      const id = data.data;
      const msg = actionChain ? (
        <span>
          {t('Action has been successfully added to the action chain <link>"{name}"</link>.', {
            name: actionChain.text,
            link: (str) => <ActionChainLink id={id}>{str}</ActionChainLink>,
          })}
        </span>
      ) : (
        <span>
          {t("Changing AppStreams has been <link>scheduled</link>.", {
            link: (str) => <ActionLink id={id}>{str}</ActionLink>,
          })}
        </span>
      );
      onConfirm(msg);
    });

    return request;
  };

  return (
    <>
      <p>{t("Please review the changes bellow.")}</p>
      <div className="text-right margin-bottom-sm">
        <Button id="cancelAppStreamChanges" className="btn btn-default" text={t("Cancel")} handler={onCancelClick} />
        <Button id="confirmAppStreamChanges" className="btn btn-success" text={t("Confirm")} handler={applyChanges} />
      </div>

      <div className="panel panel-default">
        <div className="panel-body">
          <h6>Changes Summary:</h6>
          <div>
            <p>
              {t("Streams to be enabled:")} {toEnable.length === 0 && t("No changes.")}
            </p>
            <ul>
              {toEnable.map((it) => (
                <li key={it}>{it}</li>
              ))}
            </ul>
          </div>

          <div>
            <p>
              {t("Streams to be disabled:")} {toDisable.length === 0 && t("No changes.")}
            </p>
            <ul>
              {toDisable.map((it) => (
                <li key={it}>{it}</li>
              ))}
            </ul>
          </div>
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
