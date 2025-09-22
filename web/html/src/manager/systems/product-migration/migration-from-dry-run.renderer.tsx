import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";
import { MigrationDryRunConfirmation } from "components/product-migration";

import { SSMProductMigrationFromDryRun } from "./migration-from-dry-run";

type RendererProps = {
  actionChains: ActionChain[];
  dryRunData: MigrationDryRunConfirmation;
};

export const renderer = (id: string, { actionChains, dryRunData }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <SSMProductMigrationFromDryRun actionChains={actionChains} {...dryRunData} />,
    document.getElementById(id)
  );
