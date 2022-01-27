import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { BaseRowDefinition } from "./channels-selection-rows";

import styles from "./channels-selection.css";

type Props = {
  rowDefinition: BaseRowDefinition;
  search: string;
  isOpen: boolean;
  isSelected: boolean;
  onToggleChannelSelect: (id: number) => void;
  onToggleChannelOpen: () => void;
};

const BaseChannel = (props: Props) => {
  // TODO: Implement
  const selectedChildrenCount = 0;

  const { id, channelName, isSelectedBaseChannel } = props.rowDefinition;
  const identifier = "base_" + id;
  const totalSelectedCount = Number(props.isSelected) + selectedChildrenCount;

  return (
    <h4
      className={styles.row}
      style={{
        color: isSelectedBaseChannel ? "#02A49C" : "",
        cursor: "pointer",
      }}
      {...(isSelectedBaseChannel ? { title: "New base channel" } : {})}
      onClick={() => props.onToggleChannelOpen()}
    >
      <input
        type="checkbox"
        id={identifier}
        name={identifier}
        className={styles.toggle}
        readOnly
        checked={props.isSelected}
        value={id}
        onClick={(event) => {
          // Since this element is in another clickable element, don't propagate the event
          event.stopPropagation();
          props.onToggleChannelSelect(id);
        }}
        disabled={isSelectedBaseChannel}
      />
      <i className={`${styles.arrow} fa ${props.isOpen ? "fa-angle-down" : "fa-angle-right"}`} />
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
