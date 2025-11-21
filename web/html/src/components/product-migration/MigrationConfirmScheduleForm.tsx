import { type FC, useState } from "react";

import { ChannelTreeType } from "core/channels/type/channels.type";

import { ActionChain, ActionSchedule } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { FormGroup, Label } from "components/input";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { SystemData } from "components/target-systems";

import { localizedMoment } from "utils/datetime";

import { MigrationProductList } from "./MigrationProductList";
import { MigrationProduct } from "./types";

type Props = {
  systemsData: SystemData[];
  actionChains: ActionChain[];
  migrationTarget: MigrationProduct;
  migrationChannels: ChannelTreeType;
  allowVendorChange: boolean;
  onBack?: () => void;
  onConfirm: (dryRun: boolean, earliest: moment.Moment, actionChain?: ActionChain) => Promise<void>;
};

export const MigrationConfirmScheduleForm: FC<Props> = ({
  systemsData,
  actionChains,
  migrationTarget,
  migrationChannels,
  allowVendorChange,
  onBack,
  onConfirm,
}) => {
  const [selectedEarliest, setSelectedEarliest] = useState(localizedMoment());
  const [selectedActionChain, setSelectedActionChain] = useState<ActionChain | undefined>(undefined);

  return (
    <>
      <p>{t("Please review your selection and confirm below to schedule the migration.")}</p>
      <div className="form-horizontal">
        {systemsData.length > 1 && (
          <FormGroup>
            <Label className="col-md-3" name={t("Systems")} />
            <div className="form-control-static col-md-6">
              <ul>
                {systemsData.map((system) => (
                  <li key={system.id}>
                    <a href={`/rhn/systems/details/Overview.do?sid=${system.id}`} className="js-spa">
                      {system.name}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          </FormGroup>
        )}
        <FormGroup>
          <Label className="col-md-3" name={t("Product after migration")} />
          <div className="form-control-static col-md-6">
            <MigrationProductList product={migrationTarget} />
          </div>
        </FormGroup>
        <FormGroup>
          <Label className="col-md-3" name={t("Channel subscriptions after migration")} />
          <div className="form-control-static col-md-6">
            <ul className="channels-tree">
              <li>
                <a href={`/rhn/channels/ChannelDetails.do?cid=${migrationChannels.base.id}`}>
                  {migrationChannels.base.name}
                </a>
                <ul>
                  {migrationChannels.children.map((child) => (
                    <li key={`child-${child.id}`}>
                      <a href={`/rhn/channels/ChannelDetails.do?cid=${child.id}`}>{child.name}</a>
                    </li>
                  ))}
                </ul>
              </li>
            </ul>
          </div>
        </FormGroup>
        <FormGroup>
          <Label className="col-md-3" name={t("Allow vendor change")} />
          <div className="form-control-static col-md-6">
            <p>{allowVendorChange ? t("Yes") : t("No")}</p>
          </div>
        </FormGroup>
        <FormGroup>
          <Label className="col-md-3" name={t("Schedule action for no sooner than")} />
          <ActionSchedule
            earliest={selectedEarliest}
            actionChains={actionChains}
            onActionChainChanged={(actionChain) => setSelectedActionChain(actionChain ? actionChain : undefined)}
            onDateTimeChanged={setSelectedEarliest}
            systemIds={systemsData.map((system) => system.id)}
            actionType="coco.attestation"
          />
        </FormGroup>
        <Messages
          items={MessagesUtils.warning(
            t(
              "In order to detect any possible problems it is recommended to always do a Dry Run before scheduling the actual Product Migration."
            )
          )}
        />
        <div className="col-md-offset-3 offset-md-3 btn-group">
          {onBack !== undefined && (
            <Button
              id="back-btn"
              icon="fa-chevron-left"
              className="btn-default"
              text={t("Back to channel selection")}
              handler={onBack}
            />
          )}
          <AsyncButton
            id="dry-run-btn"
            className="btn-default"
            text={t("Dry run")}
            action={() => onConfirm(true, selectedEarliest, selectedActionChain)}
          />
          <AsyncButton
            id="migrate-btn"
            className="btn-primary"
            text={t("Migrate")}
            action={() => onConfirm(false, selectedEarliest, selectedActionChain)}
          />
        </div>
      </div>
    </>
  );
};
