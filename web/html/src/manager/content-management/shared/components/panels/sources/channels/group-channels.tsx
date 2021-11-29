import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { DerivedBaseChannel } from "core/channels/type/channels.type";

type PropsType = {
  channel: DerivedBaseChannel;
  isOpen: boolean;
  selectedChannelIds: Set<number>;
  isSelectedBaseChannel: boolean;
  search: string;
  onToggleChannelSelect: (id: number) => void;
  onToggleChannelOpen: (id: number) => void;
};

// TODO: Rename
const ParentChannel = (props: PropsType) => {
  const channel = props.channel;
  const identifier = "base_" + channel.id;
  const selectedChildCount = channel.children.reduce((count, child) => {
    return count + Number(props.selectedChannelIds.has(child.id));
  }, 0);

  return (
    <div className="row" {...(props.isSelectedBaseChannel ? { title: "New base channel" } : {})}>
      <h4
        style={{
          color: props.isSelectedBaseChannel ? "#02A49C" : "",
          marginBottom: "0px",
        }}
        // TODO: Why is this a pointer, it doesn't toggle anything?
        className="pointer"
      >
        <input
          type="checkbox"
          id={identifier}
          name={identifier}
          // TODO: Or with props.isSelectedBaseChannel?
          checked={props.selectedChannelIds.has(channel.id)}
          value={channel.id}
          onChange={() => props.onToggleChannelSelect(channel.id)}
          disabled={props.isSelectedBaseChannel}
        />
        &nbsp; &nbsp;
        <div style={{ display: "inline" }} onClick={() => props.onToggleChannelOpen(channel.id)}>
          <i className={"fa " + (props.isOpen ? "fa-angle-down" : "fa-angle-right")} />
          &nbsp;
          <Highlight enabled={props.search.length > 0} text={channel.name} highlight={props.search}></Highlight>
          {selectedChildCount > 0 && <b>{` (${selectedChildCount})`}</b>}
        </div>
      </h4>
    </div>
  );
};

export default ParentChannel;
