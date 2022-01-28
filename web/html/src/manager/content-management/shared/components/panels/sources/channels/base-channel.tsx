import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { BaseRowDefinition, ChildRowDefinition, RowType } from "./channels-selection-rows";
import ChildChannel from "./child-channel";
import RecommendedToggle from "./recommended-toggle";
import EmptyChild from "./empty-child";

import styles from "./channels-selection.css";

type Props = {
  rowDefinition: BaseRowDefinition;
  search: string;
  openRows: Set<number>;
  selectedRows: Set<number>;
  onToggleChannelSelect: (channel: BaseRowDefinition | ChildRowDefinition, toState?: boolean) => void;
  onToggleChannelOpen: (id: number) => void;
};

const BaseChannel = (props: Props) => {
  // TODO: Implement
  const selectedChildrenCount = 0;

  const { id, channelName, isSelectedBaseChannel } = props.rowDefinition;
  const isOpen = props.openRows.has(id);
  const isSelected = props.selectedRows.has(id);
  const identifier = "base_" + id;
  const totalSelectedCount = Number(isSelected) + selectedChildrenCount;

  return (
    <React.Fragment>
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
            props.onToggleChannelSelect(props.rowDefinition);
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
      {isOpen
        ? props.rowDefinition.children?.map((child) => {
            switch (child.type) {
              case RowType.Child:
                return (
                  <ChildChannel
                    key={child.id}
                    definition={child}
                    search={props.search}
                    selectedRows={props.selectedRows}
                    onToggleChannelSelect={props.onToggleChannelSelect}
                  />
                );
              case RowType.EmptyChild:
                return <EmptyChild key={child.id} />;
              case RowType.RecommendedToggle:
                return (
                  <RecommendedToggle
                    key={child.id}
                    definition={child}
                    parent={props.rowDefinition}
                    selectedRows={props.selectedRows}
                    onToggleChannelSelect={props.onToggleChannelSelect}
                  />
                );
              default:
                throw new RangeError("Incorrect channel render type in renderer");
            }
          })
        : null}
    </React.Fragment>
  );
};

export default BaseChannel;
