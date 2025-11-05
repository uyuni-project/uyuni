import * as React from "react";

import { Column } from "components/table/Column";
import { HierarchicalRow, HierarchicalTable } from "components/table/HierarchicalTable";

type ChannelSelectionProps = {
  channels: HierarchicalRow[];
};

export const AppStreamsChannelSelection: React.FC<ChannelSelectionProps> = ({ channels }) => {
  const getRowClass = (row: HierarchicalRow, index: number): string => {
    return index % 2 === 0 ? "list-row-even" : "list-row-odd";
  };

  return (
    <>
      {!channels || channels.length === 0 ? (
        <p>{t("No modular channels are available for the selected systems in SSM.")}</p>
      ) : (
        <>
          <h2>{t("Select Channel")}</h2>
          <div className="page-summary">
            {t("First, select the channel containing the AppStreams to be configured upon the selected systems.")}
          </div>
          <HierarchicalTable
            data={channels}
            identifier={(row) => row.id}
            expandColumnKey="channelName"
            initiallyExpanded={true}
            cssClassFunction={getRowClass}
            className="table"
          >
            <Column
              header="Channel Name"
              columnKey="channelName"
              columnClass="col"
              cell={(row: HierarchicalRow) => (
                <>
                  {row.parentId && (
                    <img
                      style={{ marginLeft: "4px", marginRight: "8px", verticalAlign: "middle" }}
                      src="/img/channel_child_node.gif"
                      alt="Child Channel"
                    />
                  )}
                  {row.modular ? (
                    <a href={`/rhn/manager/systems/ssm/appstreams/configure/${row.id}`}>{row.name}</a>
                  ) : (
                    row.name
                  )}
                </>
              )}
            />
          </HierarchicalTable>
        </>
      )}
    </>
  );
};
