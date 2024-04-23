import * as React from "react";

import { pageSize } from "core/user-preferences";

import { SystemLink } from "./links";
import { TopPanel } from "./panels/TopPanel";
import { Column } from "./table/Column";
import { Table } from "./table/Table";

export type SystemData = {
  id: number;
  name: string;
} & Record<string, any>;

type Props = {
  systemsData: Array<SystemData>;
};

export class TargetSystems extends React.Component<Props> {
  render(): React.ReactNode {
    return (
      <TopPanel title={t("Target Systems")}>
        <Table
          selectable={false}
          data={this.props.systemsData}
          identifier={(system: SystemData) => system.id}
          initialItemsPerPage={pageSize}
          emptyText={t("No systems specified.")}
        >
          <Column
            columnClass="text-center"
            headerClass="text-center"
            columnKey="system"
            header={t("System")}
            cell={(system: SystemData) => <SystemLink id={system.id}>{system.name}</SystemLink>}
          />
          {this.props.children}
        </Table>
      </TopPanel>
    );
  }
}
