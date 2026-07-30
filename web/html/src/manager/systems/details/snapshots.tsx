import { useEffect, useState } from "react";

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
  type?: string;
  preNumber?: number | null;
  description: string;
  date: string;
  user?: string;
  usedSpace?: number | null;
  cleanup?: string;
  userdata?: string;
};

type Props = {
  serverId: string;
};

type SnapshotInfo = {
  activeSnapshot?: number | null;
  defaultSnapshot?: number | null;
  updated?: string | null;
  snapshots: Snapshot[];
};

type HttpApiResponse<T> = {
  success: boolean;
  message?: string;
  result: T;
};

const Snapshots = ({ serverId }: Props) => {
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [snapshotInfo, setSnapshotInfo] = useState<SnapshotInfo>({
    snapshots: [],
  });

  useEffect(() => {
    Network.get<HttpApiResponse<SnapshotInfo>>("/rhn/manager/api/system/getSnapshotInfo?sid=" + serverId)
      .then((response) => {
        if (response.success) {
          setSnapshotInfo(response.result);
        } else {
          setMessages([MessagesUtils.error(response.message || t("Unable to load snapshot information."))]);
        }
      })
      .catch((error) => {
        setMessages(Network.responseErrorMessage(error));
      });
  }, [serverId]);

  const refreshSnapshots = () => {
    return Network.post<HttpApiResponse<number>>("/rhn/manager/api/system/scheduleSnapshotRefresh", {
      sid: Number(serverId),
    })
      .then((response) => {
        if (!response.success) {
          return Promise.reject(response);
        }
        const redirectUrl = "/rhn/systems/details/history/Event.do?sid=" + serverId + "&aid=" + response.result;
        setMessages([
          MessagesUtils.info(
            <span>
              {t("The action has been ")}
              <a href={redirectUrl}>{t("scheduled.")}</a>
            </span>
          ),
        ]);
      })
      .catch((error) => {
        setMessages(getErrorMessages(error));
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
          {snapshotInfo.updated && (
            <p>
              {t("Updated")} <HumanDateTime value={snapshotInfo.updated} />
            </p>
          )}
          <Table
            data={snapshotInfo.snapshots}
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
            <Column columnKey="type" header={t("Type")} cell={(row: Snapshot) => row.type || ""} />
            <Column
              columnKey="preNumber"
              header={t("Pre #")}
              comparator={FunctionsUtils.sortByNumber}
              cell={(row: Snapshot) => row.preNumber ?? ""}
            />
            <Column columnKey="date" header={t("Date")} cell={(row: Snapshot) => row.date} />
            <Column columnKey="user" header={t("User")} cell={(row: Snapshot) => row.user || ""} />
            <Column
              columnKey="usedSpace"
              header={t("Used Space")}
              comparator={FunctionsUtils.sortByNumber}
              cell={(row: Snapshot) => formatUsedSpace(row.usedSpace)}
            />
            <Column columnKey="cleanup" header={t("Cleanup")} cell={(row: Snapshot) => row.cleanup || ""} />
            <Column columnKey="description" header={t("Description")} cell={(row: Snapshot) => row.description} />
            <Column columnKey="userdata" header={t("Userdata")} cell={(row: Snapshot) => row.userdata || ""} />
          </Table>
        </div>
      </div>
    </>
  );
};

const formatUsedSpace = (bytes?: number | null) => {
  if (bytes === undefined || bytes === null) {
    return "";
  }

  const units = ["B", "KiB", "MiB", "GiB", "TiB"];
  let value = bytes;
  let unit = 0;

  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024;
    unit += 1;
  }

  return unit === 0 ? `${value} ${units[unit]}` : `${value.toFixed(2)} ${units[unit]}`;
};

const getErrorMessages = (error: JQueryXHR | HttpApiResponse<unknown>) => {
  if ("success" in error && error.success === false) {
    return [MessagesUtils.error(error.message || t("Unable to schedule snapshot refresh."))];
  }
  return Network.responseErrorMessage(error);
};

export default Snapshots;
