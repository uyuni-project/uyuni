import type { FC, ReactNode } from "react";

import { pageSize } from "core/user-preferences";

import { SystemLink } from "./links";
import { TopPanel } from "./panels/TopPanel";
import { Column } from "./table/Column";
import { Table } from "./table/Table";

export type SystemData = {
  id: number;
  name: string;
};

type Props = {
  systemsData: SystemData[];
  children?: ReactNode;
};

export const TargetSystems: FC<Props> = ({ systemsData, children }: Props): JSX.Element => {
  function renderSystemLink(system: SystemData) {
    return <SystemLink id={system.id}>{system.name}</SystemLink>;
  }

  return (
    <TopPanel title={t("Target Systems")}>
      <Table
        selectable={false}
        data={systemsData}
        identifier={(system: SystemData) => system.id}
        initialItemsPerPage={pageSize}
        emptyText={t("No systems specified.")}
      >
        <Column columnKey="system" header={t("System")} cell={(system: SystemData) => renderSystemLink(system)} />
        {children}
      </Table>
    </TopPanel>
  );
};
