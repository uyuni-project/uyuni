import { type FC, type ReactNode, useState } from "react";

import { ChannelTreeType } from "core/channels/type/channels.type";

import { ActionChain } from "components/action-schedule";
import { LinkButton } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { TopPanel } from "components/panels";
import {
  MigrationChannelsSelection,
  MigrationChannelsSelectorForm,
  MigrationConfirmScheduleForm,
  MigrationProduct,
  MigrationProductList,
  MigrationSystemData,
  MigrationTarget,
  MigrationTargetSelectorForm,
  MigrationUtils,
} from "components/product-migration";
import { Column } from "components/table/Column";
import { TargetSystems } from "components/target-systems";
import { MessagesContainer } from "components/toastr";

import { stringToReact } from "utils";
import Network from "utils/network";

enum MigrationStep {
  TargetSelection,
  ChannelsSelection,
  ScheduleConfirmation,
  Scheduled,
}

export type Props = {
  commonBaseProduct: boolean;
  migrationSource: MigrationProduct | null;
  migrationTargets: MigrationTarget[];
  systemsData: MigrationSystemData[];
  actionChains?: ActionChain[];
};

export const SSMProductMigration: FC<Props> = ({
  commonBaseProduct,
  migrationSource,
  migrationTargets,
  systemsData,
  actionChains,
}: Props): JSX.Element => {
  const [migrationStep, setMigrationStep] = useState(MigrationStep.TargetSelection);
  const [statusDetailsData, setStatusDetailsData] = useState<MigrationSystemData | undefined>(undefined);
  const [installedProductData, setInstalledProductData] = useState<MigrationSystemData | undefined>(undefined);
  const [selectedTarget, setSelectedTarget] = useState<MigrationTarget | undefined>(undefined);
  const [channelSelectionData, setChannelSelectionData] = useState<MigrationChannelsSelection | undefined>(undefined);
  const [selectedChannelTree, setSelectedChannelTree] = useState<ChannelTreeType | undefined>(undefined);
  const [allowVendorChange, setAllowVendorChange] = useState(false);
  const [migrationOutcomeMessage, setMigrationOutcomeMessage] = useState<MessageType[]>([]);

  async function performMigration(dryRun: boolean, earliest: moment.Moment, actionChain?: ActionChain): Promise<void> {
    if (selectedTarget === undefined || selectedChannelTree === undefined) {
      throw new Error("Illegal state: performMigration called with selectedTarget or selectedChannelTree undefined");
    }

    try {
      const request = {
        serverIds: systemsData.map((system) => system.id),
        targetProduct: selectedTarget.targetProduct,
        targetChannelTree: selectedChannelTree,
        allowVendorChange: allowVendorChange,
        earliest: earliest,
        actionChain: actionChain ? actionChain.text : null,
        dryRun: dryRun,
      };

      const outcomeMessage = await MigrationUtils.performMigration(request);

      setMigrationStep(MigrationStep.Scheduled);
      setMigrationOutcomeMessage(outcomeMessage);
    } catch (err: any) {
      Network.showResponseErrorToastr(err);
    }
  }

  function getCurrentTitle(): string {
    switch (migrationStep) {
      case MigrationStep.TargetSelection:
        return t("Product Migration - Choose target product");

      case MigrationStep.ChannelsSelection:
        return t("Product Migration - Select channels");

      case MigrationStep.ScheduleConfirmation:
        return t("Product Migration - Schedule");

      case MigrationStep.Scheduled:
        return t("Product Migration - Schedule outcome");

      default:
        throw new TypeError(`Invalid value ${migrationStep} for a migration step`);
    }
  }

  function renderBaseProduct(system: MigrationSystemData): ReactNode {
    if (system.installedProduct === null) {
      return <span>{t("Unknown base product")}</span>;
    }

    const highlight =
      !commonBaseProduct && migrationSource !== null && system.installedProduct.id === migrationSource.id;

    return <span className={highlight ? "fw-bold" : ""}>{system.installedProduct.name}</span>;
  }

  function renderProductDetails(system: MigrationSystemData): ReactNode {
    return (
      <LinkButton
        className={system.installedProduct !== null ? "btn-link" : "btn-link disabled"}
        icon="fa-1-5x fa-list"
        title={t("Show product details")}
        handler={() => setInstalledProductData(system)}
        disabled={system.installedProduct === null}
      />
    );
  }

  function renderEligible(system: MigrationSystemData): ReactNode {
    return <span>{system.eligible ? t("Yes") : t("No")}</span>;
  }

  function renderReason(system: MigrationSystemData): ReactNode {
    let title: string, className: string;

    if (system.eligible) {
      className = system.reason === null ? "fa-check-circle text-success" : "fa-exclamation-circle text-warning";
      title = t("The products installed on this system can be migrated");
    } else {
      className = "fa-times-circle text-danger";
      title = t("The products installed on this system cannot be migrated");
    }

    return (
      <span>
        <i className={`fa fa-1-5x ${className}`} title={title}></i>
        {system.reason ?? t("The products installed on this system can be migrated.")}
        {system.details !== null && (
          <LinkButton
            className="btn-link px-2 py-0"
            handler={() => setStatusDetailsData(system)}
            text={"[" + t("show details") + "]"}
          />
        )}
      </span>
    );
  }

  function renderTargetSystems(data: MigrationSystemData[]): ReactNode {
    if (migrationStep === MigrationStep.ScheduleConfirmation) {
      return <></>;
    }

    return (
      <TargetSystems systemsData={data}>
        <Column columnKey="baseProduct" header={t("Base Product")} cell={renderBaseProduct} />
        <Column columnKey="productSet" header={t("Details")} columnClass="text-center" cell={renderProductDetails} />
        <Column columnKey="eligible" header={t("Can be migrated?")} columnClass="text-center" cell={renderEligible} />
        <Column columnKey="reason" header={t("Status")} cell={renderReason} />
      </TargetSystems>
    );
  }

  async function onTargetSelection(targetId: string): Promise<void> {
    // If the target id is already the one selected we just move to the next step
    if (selectedTarget !== undefined && selectedTarget.id === targetId) {
      setMigrationStep(MigrationStep.ChannelsSelection);
      return;
    }

    const serverIds = systemsData.map((systemData) => systemData.id);
    const request = { serverIds, targetId };

    try {
      const response = await Network.post("/rhn/manager/api/systems/migration/computeChannels", request);

      setSelectedTarget(migrationTargets.find((target) => target.id === targetId));
      setChannelSelectionData(response.data as MigrationChannelsSelection);

      // Reset the selection
      setSelectedChannelTree(undefined);
      setAllowVendorChange(false);
    } catch (err: any) {
      Network.showResponseErrorToastr(err);
    } finally {
      setMigrationStep(MigrationStep.ChannelsSelection);
    }
  }

  function onChannelsSelection(channelTree: ChannelTreeType, allowVendorChange: boolean): void {
    setSelectedChannelTree(channelTree);
    setAllowVendorChange(allowVendorChange);
    setMigrationStep(MigrationStep.ScheduleConfirmation);
  }

  return (
    <>
      <MessagesContainer />

      {statusDetailsData !== undefined && (
        <Dialog
          id="status-details-popup-dialog"
          isOpen={true}
          onClose={() => setStatusDetailsData(undefined)}
          title={stringToReact(statusDetailsData.reason)}
          content={stringToReact(statusDetailsData.details ?? "")}
        />
      )}
      {installedProductData && installedProductData.installedProduct !== null && (
        <Dialog
          id="migration-product-popup-dialog"
          isOpen={true}
          onClose={() => setInstalledProductData(undefined)}
          title={t("Product details for {system}", { system: installedProductData.name })}
          content={
            <>
              <p>
                {t("These are the products currently installed on {system}:", { system: installedProductData.name })}
              </p>
              <MigrationProductList className="ms-5 mb-3" product={installedProductData.installedProduct} />
            </>
          }
        />
      )}

      <TopPanel title={getCurrentTitle()} icon="fa spacewalk-icon-software-channels">
        {/* If no migration is possible just show an error message */}
        {(!commonBaseProduct || migrationSource === null) && (
          <Messages items={MessagesUtils.error(t("The systems currently selected cannot be migrated in batch."))} />
        )}

        {/* Migration is possible - First step: select the target */}
        {commonBaseProduct && migrationStep === MigrationStep.TargetSelection && (
          <MigrationTargetSelectorForm
            targetId={selectedTarget?.id}
            migrationSource={migrationSource!}
            migrationTargets={migrationTargets}
            onTargetChange={onTargetSelection}
          />
        )}

        {/* Migration is possible - Second step: Select the channels */}
        {commonBaseProduct && migrationStep === MigrationStep.ChannelsSelection && (
          <MigrationChannelsSelectorForm
            migrationSource={migrationSource!}
            migrationTarget={selectedTarget!}
            baseChannelTrees={channelSelectionData!.baseChannelTrees}
            mandatoryMap={channelSelectionData!.mandatoryMap}
            reversedMandatoryMap={channelSelectionData!.reversedMandatoryMap}
            baseChannel={selectedChannelTree?.base}
            childChannels={selectedChannelTree?.children}
            allowVendorChange={allowVendorChange}
            onChannelSelection={onChannelsSelection}
            onBack={() => setMigrationStep(MigrationStep.TargetSelection)}
          />
        )}
      </TopPanel>

      {commonBaseProduct && migrationStep === MigrationStep.ScheduleConfirmation && (
        <MigrationConfirmScheduleForm
          systemsData={systemsData}
          actionChains={actionChains ?? []}
          migrationTarget={selectedTarget!.targetProduct}
          migrationChannels={selectedChannelTree!}
          allowVendorChange={allowVendorChange}
          onBack={() => setMigrationStep(MigrationStep.ChannelsSelection)}
          onConfirm={performMigration}
        />
      )}

      {commonBaseProduct && migrationStep === MigrationStep.Scheduled && <Messages items={migrationOutcomeMessage} />}

      {migrationStep !== MigrationStep.Scheduled &&
        renderTargetSystems(
          migrationStep !== MigrationStep.TargetSelection ? channelSelectionData!.systemsData : systemsData
        )}
    </>
  );
};
