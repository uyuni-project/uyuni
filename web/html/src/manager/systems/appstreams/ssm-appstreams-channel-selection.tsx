import * as React from "react";

import { SSMAppStreamChannel } from "manager/appstreams/appstreams.type";

import { Table } from "components/table";
import { Column } from "components/table/Column";

type ChannelSelectionProps = {
  channels: SSMAppStreamChannel[];
};

export const AppStreamsChannelSelection: React.FC<ChannelSelectionProps> = ({ channels }) => {
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
          <Table data={channels} identifier={(row) => row.id}>
            <Column
              header="Channel Name"
              columnKey="channelName"
              columnClass="col"
              cell={(row) => (
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
          </Table>
        </>
      )}
    </>
  );
};
