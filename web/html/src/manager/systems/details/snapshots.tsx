import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

type Snapshot = {
  number: number;
  active: boolean;
  default: boolean;
  description: string;
  date: string;
};

type Props = {
  snapshots: Snapshot[];
};

const Snapshots = ({ snapshots }: Props) => {
  return (
    <div className="panel panel-default">
      <div className="panel-heading">
        <h4>{t("Btrfs Snapshots")}</h4>
      </div>
      <div className="panel-body">
        <Table data={snapshots} identifier={(row) => String(row.number)} emptyText={t("No snapshots available.")}>
          <Column
            columnKey="number"
            header={t("#")}
            cell={(row: Snapshot) => (
              <span>
                {row.number}
                {row.active && (
                  <span className="label label-success" style={{ marginLeft: "6px" }}>
                    {t("active")}
                  </span>
                )}
                {row.default && (
                  <span className="label label-info" style={{ marginLeft: "4px" }}>
                    {t("default")}
                  </span>
                )}
              </span>
            )}
          />
          <Column columnKey="description" header={t("Description")} cell={(row: Snapshot) => row.description} />
          <Column columnKey="date" header={t("Date")} cell={(row: Snapshot) => row.date} />
        </Table>
      </div>
    </div>
  );
};

export default Snapshots;
