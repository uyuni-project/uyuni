import * as React from "react";
import { useState, useEffect } from "react";
import { Messages, MessageType, Utils as MsgUtils } from "components/messages";
import Network, { JsonResult } from "utils/network";
import { Combobox, ComboboxItem } from "components/combobox";
import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { AnsiblePath } from "./ansible-path-type";
import { InnerPanel } from "components/panels/InnerPanel";
import { AceEditor } from "components/ace-editor";
import { PlaybookDetails } from "./accordion-path-content";
import { Formats, Utils } from "utils/functions";
import { ActionChainLink, ActionLink } from "components/links";
import { Loading } from "components/utils/Loading";

interface SchedulePlaybookProps {
  playbook: PlaybookDetails,
  onBack: () => void
}

export default function SchedulePlaybook({ playbook, onBack }: SchedulePlaybookProps) {
  const [loading, setLoading] = useState(true);
  const [playbookContent, setPlaybookContent] = useState("");
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [inventoryPath, setInventoryPath] = useState<ComboboxItem | null>(null);
  const [inventories, setInventories] = useState<string[]>([]);
  const [actionChain, setActionChain] = useState<ActionChain | null>(null);
  const [datetime, setDatetime] = useState(new Date());

  // TODO: Use UserLocalization hook instead
  const timezone = window.timezone;
  const localTime = window.localTime;

  useEffect(() => {
    const getInventoryPaths = () => {
      return Network.get(`/rhn/manager/api/systems/details/ansible/paths/inventory/${playbook.path.minionServerId}`)
        .then((res: JsonResult<AnsiblePath[]>) => res.success ? res.data : Promise.reject(res))
        .then(inv => inv.map(i => i.path))
        .then(inv => {
          if (playbook.customInventory) inv.push(playbook.customInventory);
          setInventories(inv);
        })
        .catch(res =>
          setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res))
        );
    }

    const getPlaybookContents = () => {
      return Network.post("/rhn/manager/api/systems/details/ansible/paths/playbook-contents",
        {
          pathId: playbook.path.id,
          playbookRelPathStr: playbook.name
        },
      )
        .then((res: JsonResult<string>) => res.success ? res.data : Promise.reject(res))
        .then(setPlaybookContent)
        .catch(res =>
          setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res))
        );
    }

    Promise.all([getInventoryPaths(), getPlaybookContents()]).finally(() => setLoading(false));
  }, [playbook, localTime]);

  useEffect(() => {
    setDatetime(Utils.dateWithTimezone(localTime || ""));
  }, [localTime]);

  const schedule = () => {
    return Network.post("/rhn/manager/api/systems/details/ansible/schedule-playbook",
      {
        playbookPath: playbook.fullPath,
        inventoryPath: inventoryPath?.text,
        controlNodeId: playbook.path.minionServerId,
        actionChainLabel: actionChain?.text || null,
        earliest: Formats.LocalDateTime(datetime)
      },
    )
      .then((res: JsonResult<number>) => res.success ? res.data : Promise.reject(res))
      .then(actionId =>
        setMessages(MsgUtils.info(<ScheduleMessage id={actionId} actionChain={actionChain?.text}/>)))
      .catch(res =>
        setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res))
      );
  };

  if (loading)
    return <Loading text={t("Loading playbook contents..")} />;


  const inventoryOpts: ComboboxItem[] = inventories.map((inv, i) => ({ id: i, text: inv }));

  const buttons = [
    <div className="btn-group pull-right">
      <Button icon="fa-angle-left" className="btn-default" text={t("Back")} title={t("Back to playbook list")} handler={onBack} />
      <AsyncButton defaultType="btn-success" action={schedule} title={t("Schedule playbook execution")} text={t("Schedule")} />
    </div>,
  ];

  return (
    <>
      <Messages items={messages} />
      <InnerPanel
        title={t("Playbook '{0}'", playbook.name)}
        icon="fa-file-text-o"
        buttons={buttons}
      >
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>{t("Schedule Playbook Execution")}</h3>
            </div>
          </div>
          <div className="panel-body">
            <ActionSchedule
              timezone={timezone}
              localTime={localTime}
              earliest={datetime}
              actionChains={window.actionChains}
              onDateTimeChanged={setDatetime}
              onActionChainChanged={setActionChain}
              systemIds={[playbook.path.minionServerId]}
              actionType="ansible.playbook"
            />
            <div className="form-horizontal">
              <div className="form-group">
                <div className="col-sm-3 control-label">
                  <label>{t("Inventory Path")}:</label>
                </div>
                <div className="col-sm-6">
                  <Combobox
                    id="inventory-path-select"
                    name="inventory-path-select"
                    data={inventoryOpts}
                    selectedId={inventoryPath?.id}
                    onSelect={setInventoryPath}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <h3>{t("Playbook Content")}</h3>
          <AceEditor
            className="form-control"
            id="playbook-content"
            minLines={20}
            maxLines={40}
            readOnly={true}
            mode="yaml"
            content={playbookContent}
          />
        </div>
      </InnerPanel>
    </>
  );
}

function ScheduleMessage({ id, actionChain }: { id: number, actionChain?: string }) {
  if (actionChain) {
    return (
      <span>
        {t("Action has been successfully added to action chain")}&nbsp;
        <ActionChainLink id={id}>'{actionChain}'</ActionChainLink>
      </span>
    );
  }
  else {
    return (
      <span>
        {t("Playbook execution has been")}&nbsp;
        <ActionLink id={id}>{t("scheduled")}</ActionLink>
      </span>
    );
  }
}
