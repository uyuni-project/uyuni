import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { DerivedBaseChannel } from "core/channels/type/channels.type";

type PropsType = {
  channel: DerivedBaseChannel;
  isOpen: boolean;
  isSelected: boolean;
  isSelectedBaseChannel: boolean;
  selectedChildrenCount: number;
  search: string;
  onToggleChannelSelect: (id: number) => void;
  onToggleChannelOpen: (id: number) => void;
};

// TODO: Rename to BaseChannel
const ParentChannel = (props: PropsType) => {
  const channel = props.channel;
  const identifier = "base_" + channel.id;
  const totalSelectedCount = Number(props.isSelected) + props.selectedChildrenCount;

  return (
    <div className="row" {...(props.isSelectedBaseChannel ? { title: "New base channel" } : {})}>
      <h4
        style={{
          color: props.isSelectedBaseChannel ? "#02A49C" : "",
          marginBottom: "0px",
        }}
      >
        <input
          type="checkbox"
          id={identifier}
          name={identifier}
          checked={props.isSelected}
          value={channel.id}
          onChange={() => props.onToggleChannelSelect(channel.id)}
          disabled={props.isSelectedBaseChannel}
        />
        &nbsp; &nbsp;
        <div style={{ display: "inline" }} className="pointer" onClick={() => props.onToggleChannelOpen(channel.id)}>
          <i className={"fa " + (props.isOpen ? "fa-angle-down" : "fa-angle-right")} />
          &nbsp;
          <Highlight enabled={props.search.length > 0} text={channel.name} highlight={props.search}></Highlight>
          {totalSelectedCount > 0 && <b>{` (${totalSelectedCount})`}</b>}
        </div>
      </h4>
    </div>
  );
};

export default ParentChannel;
