import { useEffect, useState } from "react";

import yaml from "js-yaml";

import { AceEditor } from "components/ace-editor";
import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { Combobox, ComboboxItem } from "components/combobox";
import { DEPRECATED_Check, Form } from "components/input";
import { ActionChainLink, ActionLink } from "components/links";
import { Messages, MessageType, Utils as MsgUtils } from "components/messages/messages";
import { InnerPanel } from "components/panels/InnerPanel";
import { Toggler } from "components/toggler";
import { Loading } from "components/utils/loading/Loading";

import { localizedMoment } from "utils";
import Network, { JsonResult } from "utils/network";

import { PlaybookDetails } from "./accordion-path-content";
import styles from "./Ansible.module.scss";
import { AnsiblePath } from "./ansible-path-type";
import EditAnsibleVarsModal from "./edit-ansible-vars-modal";

type SchedulePlaybookProps = {
  playbook: PlaybookDetails;
  recurringDetails?: any;
  isRecurring?: boolean;
  onBack: () => void;
  onSelectPlaybook?: (playbook: any) => void;
};

type PlaybookArgs = {
  flushCache: boolean;
};

export default function SchedulePlaybook({
  playbook,
  onBack,
  onSelectPlaybook,
  isRecurring,
  recurringDetails,
}: SchedulePlaybookProps) {
  const [loading, setLoading] = useState(true);
  const [playbookContent, setPlaybookContent] = useState("");
  const [isTestMode, setIsTestMode] = useState(false);
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [inventoryPath, setInventoryPath] = useState<ComboboxItem | null>(null);
  const [inventories, setInventories] = useState<string[]>([]);
  const [playbookArgs, setPlaybookArgs] = useState<PlaybookArgs>({ flushCache: false });
  const [variables, setVariables] = useState<string>("");
  const [actionChain, setActionChain] = useState<ActionChain | null>(null);
  const [datetime, setDatetime] = useState(localizedMoment());
  const defaultInventory = "-";

  useEffect(() => {
    const getInventoryPaths = () => {
      return Network.get(`/rhn/manager/api/systems/details/ansible/paths/inventory/${playbook.path.minionServerId}`)
        .then((res: JsonResult<AnsiblePath[]>) => (res.success ? res.data : Promise.reject(res)))
        .then((inv) => inv.map((i) => i.path))
        .then((inv) => {
          if (playbook.customInventory) inv.push(playbook.customInventory);
          setInventories(inv);
        })
        .catch((res) => setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res)));
    };

    const getPlaybookContents = () => {
      return Network.post("/rhn/manager/api/systems/details/ansible/paths/playbook-contents", {
        pathId: playbook.path.id,
        playbookRelPathStr: playbook.name,
      })
        .then((res: JsonResult<string>) => (res.success ? res.data : Promise.reject(res)))
        .then((res) => {
          setPlaybookContent(res);
          if (isRecurring && playbook.fullPath === recurringDetails.fullPath && recurringDetails.variables) {
            mergePlaybookContent(res, "", recurringDetails.variables);
          }
        })
        .catch((res) => setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res)));
    };

    Promise.all([getInventoryPaths(), getPlaybookContents()]).finally(() => setLoading(false));
  }, [playbook]);

  const schedule = () => {
    return Network.post("/rhn/manager/api/systems/details/ansible/schedule-playbook", {
      playbookPath: playbook.fullPath,
      inventoryPath: inventoryPath?.text === defaultInventory ? null : inventoryPath?.text,
      controlNodeId: playbook.path.minionServerId,
      testMode: isTestMode,
      flushCache: playbookArgs.flushCache,
      extraVars: variables,
      actionChainLabel: actionChain?.text || null,
      earliest: datetime,
    })
      .then((res: JsonResult<number>) => (res.success ? res.data : Promise.reject(res)))
      .then((actionId) => setMessages(MsgUtils.info(<ScheduleMessage id={actionId} actionChain={actionChain?.text} />)))
      .catch((res) => setMessages(res.messages?.flatMap(MsgUtils.error) || Network.responseErrorMessage(res)));
  };

  const updatePlaybookContent = (updatedVariables, extraVars) => {
    mergePlaybookContent(playbookContent, updatedVariables, extraVars);
  };

  const mergePlaybookContent = (playbookContent, updatedVariables, extraVars) => {
    let mergedVars = { ...updatedVariables };

    const extraVarsObject = yaml.load(extraVars);

    if (typeof extraVarsObject === "object" && extraVarsObject !== null) {
      mergedVars = { ...updatedVariables, ...extraVarsObject };
    } else {
      mergedVars = { ...updatedVariables };
    }
    const parsed = yaml.load(playbookContent);
    if (Array.isArray(parsed)) {
      parsed[0].vars = mergedVars;

      const updatedYaml = `---\n${yaml.dump(parsed, {
        quotingType: '"',
        forceQuotes: true,
      })}`;

      setPlaybookContent(updatedYaml);
      setVariables(JSON.stringify(mergedVars));
    }
  };

  const selectPlaybook = () => {
    return onSelectPlaybook?.({
      playbookPath: playbook.fullPath,
      inventoryPath: inventoryPath?.text === defaultInventory ? null : inventoryPath?.text,
      flushCache: playbookArgs.flushCache,
      extraVars: variables,
    });
  };

  if (loading) return <Loading text={t("Loading playbook contents..")} />;

  const inventoryOpts: ComboboxItem[] = inventories.map((inv, i) => ({ id: i, text: inv }));

  const buttons = [
    <div className="btn-group pull-right" key="buttons-right">
      <Toggler text={t("Test mode")} value={isTestMode} className="btn" handler={() => setIsTestMode(!isTestMode)} />
      <Button
        icon="fa-angle-left"
        className="btn-default"
        text={t("Back")}
        title={t("Back to playbook list")}
        handler={onBack}
      />
      <AsyncButton
        defaultType="btn-primary"
        action={schedule}
        title={t("Schedule playbook execution")}
        text={t("Schedule")}
      />
    </div>,
  ];

  const buttonsRecurring = [
    <div key="rec-btns" className="btn-group pull-right">
      <Button
        className="btn-default"
        text={t("Change Playbook")}
        title={t("Choose a different Playbook")}
        handler={onBack}
      />
      <Button
        className="btn-primary"
        text={t("Save")}
        title={t("Save the current Playbook")}
        handler={selectPlaybook}
      />
    </div>,
  ];

  return (
    <>
      <Messages items={messages} />
      <InnerPanel
        title={t('Playbook "{name}"', { name: playbook.name })}
        icon="fa-file-text-o"
        buttons={isRecurring ? buttonsRecurring : buttons}
      >
        <div className="panel panel-default">
          {!isRecurring && (
            <>
              <div className="panel-heading">
                <div>
                  <h3>{t("Schedule Playbook Execution")}</h3>
                </div>
              </div>
              <div className="panel-body">
                <ActionSchedule
                  earliest={datetime}
                  actionChains={window.actionChains}
                  onDateTimeChanged={setDatetime}
                  onActionChainChanged={setActionChain}
                  systemIds={[playbook.path.minionServerId]}
                  actionType="ansible.playbook"
                />
              </div>
            </>
          )}
          <div className="panel_body">
            <Form model={playbookArgs} onChange={setPlaybookArgs} formDirection="form-horizontal">
              <div className="form-group">
                <div className="col-sm-3 control-label">
                  <label>{t("Inventory Path")}:</label>
                </div>
                <div className="col-sm-6">
                  <Combobox
                    id="inventory-path-select"
                    name="inventory-path-select"
                    options={inventoryOpts}
                    selectedId={inventoryPath?.id}
                    onSelect={setInventoryPath}
                  />
                </div>
              </div>
              <DEPRECATED_Check
                name="flushCache"
                label={t("Flush Ansible fact cache")}
                title={t("Clear the fact cache for every host in inventory")}
                divClass="col-sm-offset-3 offset-sm-3 col-sm-6"
              />
            </Form>
          </div>
        </div>

        <div>
          <div className="d-flex justify-content-between">
            <h3>{t("Playbook Content")}</h3>
            <div className="py-4">
              <EditAnsibleVarsModal
                id="anisble-var"
                className={styles.anisbleVar}
                renderContent={playbookContent}
                updatePlaybookContent={updatePlaybookContent}
              />
            </div>
          </div>
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

function ScheduleMessage({ id, actionChain }: { id: number; actionChain?: string }) {
  if (actionChain) {
    return (
      <span>
        {t("Action has been successfully added to action chain")}&nbsp;
        <ActionChainLink id={id}>'{actionChain}'</ActionChainLink>
      </span>
    );
  } else {
    return (
      <span>
        {t("Playbook execution has been")}&nbsp;
        <ActionLink id={id}>{t("scheduled")}</ActionLink>
      </span>
    );
  }
}
