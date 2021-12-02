import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { BaseRowDefinition } from "./channels-selection-rows";

import styles from "./channels-selection.css";

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
    <h4
      className={styles.row}
      style={{
        color: isSelectedBaseChannel ? "#02A49C" : "",
        cursor: "pointer",
      }}
      {...(isSelectedBaseChannel ? { title: "New base channel" } : {})}
      onClick={() => props.onToggleChannelOpen(id)}
    >
      <input
        type="checkbox"
        id={identifier}
        name={identifier}
        className={styles.toggle}
        readOnly
        checked={isSelected}
        value={id}
        onClick={(event) => {
          // Since this element is in another clickable element, don't propagate the event
          event.stopPropagation();
          props.onToggleChannelSelect(id);
        }}
        disabled={isSelectedBaseChannel}
      />
      <i className={`${styles.arrow} fa ${isOpen ? "fa-angle-down" : "fa-angle-right"}`} />
      <Highlight
        className={styles.collapsible}
        enabled={props.search.length > 0}
        text={channelName}
        highlight={props.search}
      />
      {totalSelectedCount > 0 && <b className={styles.count}>{`(${totalSelectedCount})`}</b>}
    </h4>
  );
};

export default BaseChannel;
