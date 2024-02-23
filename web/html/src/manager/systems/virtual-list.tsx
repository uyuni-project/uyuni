import * as React from "react";

import { IconTag } from "components/icontag";
import * as Systems from "components/systems";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { VirtualSystemsListFilter } from "./list-filter";

// See java/code/src/com/suse/manager/webui/templates/systems/virtual-list.jade
type Props = {
  /** Locale of the help links */
  docsLocale: string;
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export function VirtualSystems(props: Props) {
  const [selectedSystems, setSelectedSystems] = React.useState<String[]>([]);

  const handleSelectedSystems = (items: String[]) => {
    const removed = selectedSystems.filter((item) => !items.includes(item)).map((item) => [item, false]);
    const added = items.filter((item) => !selectedSystems.includes(item)).map((item) => [item, true]);
    const data = Object.assign({}, Object.fromEntries(added), Object.fromEntries(removed));
    Network.post("/rhn/manager/api/sets/system_list", data).catch(Network.showResponseErrorToastr);

    setSelectedSystems(items);
  };

  return (
    <>
      <h1>
        <IconTag type="header-system" />
        {t("Virtual Systems")}
        <a
          href={`/docs/${props.docsLocale}/reference/systems/systems-list.html`}
          target="_blank"
          rel="noopener noreferrer"
        >
          <IconTag type="header-help" />
        </a>
      </h1>

      <Table
        data="/rhn/manager/api/systems/list/virtual"
        identifier={(item) => item.virtualSystemId || item.uuid}
        initialSortColumnKey="host_server_name"
        selectable={(item) => item.hasOwnProperty("virtualSystemId")}
        selectedItems={selectedSystems}
        onSelect={handleSelectedSystems}
        searchField={<VirtualSystemsListFilter />}
        defaultSearchField={props.queryColumn || "host_server_name"}
        initialSearch={props.query}
        emptyText={t("No Virtual Systems.")}
      >
        <Column
          columnKey="hostServerName"
          comparator={Utils.sortByText}
          header={t("Virtual Host")}
          cell={(item) => {
            return <a href={`/rhn/systems/details/Overview.do?sid=${item.hostSystemId}`}>{item.hostServerName}</a>;
          }}
        />
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("Virtual System")}
          cell={(item) => {
            if (item.systemId != null) {
              return <a href={`/rhn/systems/details/Overview.do?sid=${item.systemId}`}>{item.name}</a>;
            }
            return item.name;
          }}
        />
        <Column
          columnKey="stateName"
          comparator={Utils.sortByText}
          header={t("Status")}
          cell={(item) => item.stateName}
        />
        <Column
          columnKey="statusType"
          comparator={Utils.sortByText}
          header={t("Updates")}
          cell={(item) => {
            if (item.statusType == null) {
              return "";
            }
            return Systems.statusDisplay(item, props.isAdmin);
          }}
        />
        <Column
          columnKey="channelLabels"
          comparator={Utils.sortByText}
          header={t("Base Software Channel")}
          cell={(item) => {
            if (item.channelId != null) {
              return <a href={`/rhn/channels/ChannelDetail.do?cid=${item.channelId}`}>{item.channelLabels}</a>;
            }
            return item.channelLabels;
          }}
        />
      </Table>

      <div className="spacewalk-csv-download">
        <a href="/rhn/manager/systems/csv/virtualSystems" className="btn btn-link" data-senna-off="true">
          <IconTag type="item-download-csv" />
          Download CSV
        </a>
      </div>
    </>
  );
}
