import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";
import { MigrationTargetSelection } from "components/product-migration";

import { SSMProductMigration } from "./ssm-product-migration";

type RendererProps = {
  actionChains: ActionChain[];
  migrationData: MigrationTargetSelection;
};

export const renderer = (id: string, { actionChains, migrationData }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <SSMProductMigration actionChains={actionChains} {...migrationData} />,
    document.getElementById(id)
  );
