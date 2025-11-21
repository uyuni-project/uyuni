import { type FC, useState } from "react";

import moment from "moment";

import { ChannelTreeType } from "core/channels/type/channels.type";

import { ActionChain } from "components/action-schedule";
import { Messages, MessageType } from "components/messages/messages";
import { TopPanel } from "components/panels";
import {
  MigrationConfirmScheduleForm,
  MigrationProduct,
  MigrationSystemData,
  MigrationUtils,
} from "components/product-migration";
import { MessagesContainer } from "components/toastr";

import Network from "utils/network";

export type Props = {
  targetProduct: MigrationProduct;
  selectedChannels: ChannelTreeType;
  systemsData: MigrationSystemData[];
  allowVendorChange: boolean;
  actionChains?: ActionChain[];
};

export const SSMProductMigrationFromDryRun: FC<Props> = ({
  targetProduct,
  selectedChannels,
  systemsData,
  allowVendorChange,
  actionChains,
}: Props): JSX.Element => {
  const [outcomeMessage, setOutcomeMessage] = useState<MessageType[]>([]);

  async function performMigration(dryRun: boolean, earliest: moment.Moment, actionChain?: ActionChain): Promise<void> {
    try {
      const request = {
        serverIds: systemsData.map((system) => system.id),
        targetProduct: targetProduct,
        targetChannelTree: selectedChannels,
        allowVendorChange: allowVendorChange,
        earliest: earliest,
        actionChain: actionChain ? actionChain.text : null,
        dryRun: dryRun,
      };

      const successMsg = await MigrationUtils.performMigration(request);
      setOutcomeMessage(successMsg);
    } catch (err: any) {
      Network.showResponseErrorToastr(err);
    }
  }

  function getCurrentTitle(): string {
    if (outcomeMessage.length > 0) {
      return t("Product Migration - Schedule outcome");
    }

    return t("Product Migration - Confirm from dry run");
  }

  return (
    <TopPanel title={getCurrentTitle()} icon="fa spacewalk-icon-software-channels">
      <MessagesContainer />

      {outcomeMessage.length === 0 ? (
        <MigrationConfirmScheduleForm
          systemsData={systemsData}
          actionChains={actionChains ?? []}
          migrationTarget={targetProduct}
          migrationChannels={selectedChannels}
          allowVendorChange={allowVendorChange}
          onConfirm={performMigration}
        />
      ) : (
        <Messages items={outcomeMessage} />
      )}
    </TopPanel>
  );
};
