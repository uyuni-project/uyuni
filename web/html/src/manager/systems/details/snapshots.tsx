import { useState } from "react";

import { AsyncButton } from "components/buttons";
import { HumanDateTime } from "components/datetime";
import { Messages, MessageType, Utils as MessagesUtils } from "components/messages/messages";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils as FunctionsUtils } from "utils/functions";
import Network from "utils/network";

declare global {
  interface Window {
    serverId?: any;
  }
}

type Snapshot = {
  number: number;
  active: boolean;
  default: boolean;
  description: string;
  date: string;
};

type Props = {
  snapshots: Snapshot[];
  snapshotUpdated?: string;
};

const Snapshots = ({ snapshots, snapshotUpdated }: Props) => {
  const [messages, setMessages] = useState<MessageType[]>([]);

  const refreshSnapshots = () => {
    return Network.post("/rhn/manager/api/systems/" + window.serverId + "/details/snapshots/refresh")
      .then(Network.unwrap)
      .then((data) => {
        setMessages(
          MessagesUtils.info(
            <span>
              {t("The action has been ")}
              <a href={data.redirectUrl}>{t("scheduled.")}</a>
            </span>
          )
        );
      })
      .catch((error) => {
        setMessages(Network.responseErrorMessage(error));
        return Promise.reject(error);
      });
  };

  return (
    <>
      {messages.length > 0 && <Messages items={messages} />}
      <div className="panel panel-default">
        <div className="panel-heading">
          <div className="pull-right">
            <AsyncButton
              id="refresh-snapshots"
              icon="fa-refresh"
              text={t("Refresh Snapshots")}
              action={refreshSnapshots}
            />
          </div>
          <h4>{t("Btrfs Snapshots")}</h4>
        </div>
        <div className="panel-body">
          {snapshotUpdated && snapshotUpdated !== "null" && (
            <p>
              {t("Updated")} <HumanDateTime value={snapshotUpdated} />
            </p>
          )}
          <Table
            data={snapshots}
            identifier={(row) => String(row.number)}
            initialSortColumnKey="number"
            initialSortDirection={-1}
            emptyText={t("No snapshots available.")}
          >
            <Column
              columnKey="number"
              header={t("#")}
              comparator={FunctionsUtils.sortByNumber}
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
    </>
  );
};

export default Snapshots;
