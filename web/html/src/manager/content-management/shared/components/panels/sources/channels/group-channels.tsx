import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import { RequiredChannelsResultType } from "core/channels/api/use-mandatory-channels-api";
import { ChannelType } from "core/channels/type/channels.type";

type PropsType = {
  channel: ChannelType;
  search: string;
  selectedChannelsIdsInGroup: number[];
  selectedBaseChannelId: number | null | undefined;
  isOpen: boolean;
  onChannelToggle: (id: number) => void;
  onOpenGroup: (isOpen: boolean) => void;
  channelsTree: ChannelsTreeType;
  requiredChannelsResult: RequiredChannelsResultType;
};

// TODO: Rename to ParentChannel or something similar
const GroupChannels = (props: PropsType) => {
  const channel = props.channel;
  const nrOfSelectedChilds = props.selectedChannelsIdsInGroup.length;

  const isNewLeaderChannel = channel.id === props.selectedBaseChannelId;

  return (
    <div className="row" {...(isNewLeaderChannel ? { title: "New base channel" } : {})}>
      <h4
        style={{
          color: isNewLeaderChannel ? "#02A49C" : "",
          marginBottom: "0px",
        }}
        className="pointer"
      >
        <input
          type="checkbox"
          id={"base_" + channel.id}
          name="childChannels"
          checked={props.selectedChannelsIdsInGroup.includes(channel.id)}
          value={channel.id}
          onChange={() => {
            props.onChannelToggle(channel.id);
          }}
          disabled={channel.id === props.selectedBaseChannelId}
        />
        &nbsp; &nbsp;
        <div style={{ display: "inline" }} onClick={() => props.onOpenGroup(!props.isOpen)}>
          <i className={"fa " + (props.isOpen ? "fa-angle-down" : "fa-angle-right")} />
          &nbsp;
          <Highlight enabled={props.search.length > 0} text={channel.name} highlight={props.search}></Highlight>
          {nrOfSelectedChilds > 0 && <b>{` (${nrOfSelectedChilds})`}</b>}
        </div>
      </h4>
    </div>
  );
};

export default GroupChannels;
