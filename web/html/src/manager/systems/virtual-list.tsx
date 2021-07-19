import * as React from "react";
import { useState, useEffect } from 'react';
import Network from 'utils/network';
import * as Systems from "components/systems";
import { Utils } from "utils/functions";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import SpaRenderer from "core/spa/spa-renderer";
import { IconTag } from "components/icontag";
import errors from "manager/errors";

// See java/code/src/com/suse/manager/webui/templates/systems/virtual-list.jade
type Props = {
  /** Locale of the help links */
  docsLocale: string;
  isAdmin: boolean;
};

function VirtualSystems(props: Props) {

  const fetchURL = "/rhn/manager/api/systems/list/virtual";
  const [items, setItems] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    Network.get(fetchURL)
      .then(res => {
        setItems(res)
      })
      .catch(err => {
        setError(err);
        console.log(error);
      })
  }, [])

  return (
    <>
      <h1>
        <IconTag type="header-system"/>
        {t("Virtual Systems")}
        <a href={`/docs/${props.docsLocale}/reference/systems/systems-list.html`} target="_blank">
          <IconTag type="header-help"/>
        </a>
      </h1>

      <Table
        data={items}
        identifier={items => items.uuid}
        initialSortColumnKey="vHost"
        initialItemsPerPage={window.userPrefPageSize}
        emptyText={t("No Virtual Systems.")}
      >
        <Column
          columnKey="vHost"
          comparator={Utils.sortByText}
          header={t("Virtual Host")}
          cell={(items) => {
            var sysId = items.hostSystemId;
            return (
              <a href={"/rhn/systems/details/Overview.do?sid=" + sysId}>{items.hostServerName}</a>
            )
          }}
        />
        <Column
          columnKey="vm"
          comparator={Utils.sortByText}
          header={t("Virtual System")}
          cell={(items) => items.name}
        />
        <Column
          columnKey="status"
          comparator={Utils.sortByText}
          header={t("Status")}
          cell={(items) => items.stateName}
        />
        <Column
          columnKey="statusType"
          comparator={Utils.sortByText}
          header={t("Updates")}
          cell={row => {
            if (row.statusType == null) {
              return "";
            }
            return Systems.statusDisplay(row, props.isAdmin);
        }}
        />
        <Column
          columnKey="baseSoftwareChannel"
          comparator={Utils.sortByText}
          header={t("Base Software Channel")}
          cell={(items) => items.channelLabels}
        />
      </Table>

    </>
  );
}

export const renderer = (id: string, docsLocale: string,  isAdmin: boolean) =>
  SpaRenderer.renderNavigationReact(
    <VirtualSystems docsLocale={docsLocale} isAdmin={isAdmin}/>,
    document.getElementById(id)
  );
