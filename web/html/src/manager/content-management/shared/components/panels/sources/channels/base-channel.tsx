import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { BaseRowDefinition } from "./channels-selection-rows";

type Props = {
  rowDefinition: BaseRowDefinition;
  search: string;
  onToggleChannelSelect: (id: number) => void;
  onToggleChannelOpen: (id: number) => void;
};

const BaseChannel = (props: Props) => {
  const { id, channelName, isSelected, selectedChildrenCount, isSelectedBaseChannel, isOpen } = props.rowDefinition;
  const identifier = "base_" + id;
  const totalSelectedCount = Number(isSelected) + selectedChildrenCount;

  return (
    <div className="row" {...(isSelectedBaseChannel ? { title: "New base channel" } : {})}>
      <h4
        style={{
          color: isSelectedBaseChannel ? "#02A49C" : "",
          marginBottom: "0px",
        }}
      >
        <input
          type="checkbox"
          id={identifier}
          name={identifier}
          checked={isSelected}
          value={id}
          onChange={() => props.onToggleChannelSelect(id)}
          disabled={isSelectedBaseChannel}
        />
        &nbsp; &nbsp;
        <div style={{ display: "inline" }} className="pointer" onClick={() => props.onToggleChannelOpen(id)}>
          <i className={"fa " + (isOpen ? "fa-angle-down" : "fa-angle-right")} />
          &nbsp;
          <Highlight enabled={props.search.length > 0} text={channelName} highlight={props.search}></Highlight>
          {totalSelectedCount > 0 && <b>{` (${totalSelectedCount})`}</b>}
        </div>
      </h4>
    </div>
  );
};

export default BaseChannel;
