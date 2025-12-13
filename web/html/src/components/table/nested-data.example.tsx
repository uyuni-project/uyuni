import { Column, Table, useSelected } from "components/table";

import { placeholderData } from "./Table.example.placeholderData";

// This is just a placeholder type
type ChannelWithHierarchy = {
  channelId: number;
  channelLabel: string;
  channelArch: string;
  channelOrg?: {
    orgName: string;
  };
  children: ChannelWithHierarchy[];
};

export default () => {
  const identifier = (row: ChannelWithHierarchy) => row.channelId;
  const getAllIdentifiers = () => {
    return new Promise<number[]>((resolve) =>
      window.setTimeout(
        () => resolve([114, 115, 116, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 101]),
        100
      )
    );
  };

  const viewSelected = useSelected(identifier, getAllIdentifiers);
  const modifySelected = useSelected(identifier, getAllIdentifiers);

  const channels = placeholderData.channelSyncData.channels;

  return (
    <>
      <button
        onClick={() => {
          viewSelected.setSelected([114, 115, 116, 102, 103, 104]);
        }}
        style={{ marginBottom: "10px" }}
      >
        call <code>viewSelected.setSelected(...)</code>
      </button>
      <Table data={channels} identifier={identifier} expandable>
        <Column
          columnKey="channelName"
          header={t("Channel name")}
          cell={(row, criteria, nestingLevel) => {
            if (nestingLevel) {
              return row.channelName;
            }
            return <b>{row.channelName}</b>;
          }}
          width="20%"
        />
        <Column
          columnKey="channelLabel"
          header={t("Channel Label")}
          cell={(row: ChannelWithHierarchy) => row.channelLabel}
          width="20%"
        />
        <Column
          columnKey="channelArch"
          header={t("Architecture")}
          cell={(row: ChannelWithHierarchy) => row.channelArch}
        />
        <Column
          columnKey="hubOrg"
          header={t("Hub Org")}
          cell={(row: ChannelWithHierarchy) => (row.channelOrg ? row.channelOrg.orgName : "SUSE")}
          width="20%"
        />
        <viewSelected.Column columnKey="view" header={<viewSelected.Header>{t("View")}</viewSelected.Header>} />
        <modifySelected.Column columnKey="modify" header={t("Modify")} />
      </Table>
    </>
  );
};
