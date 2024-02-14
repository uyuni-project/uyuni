import * as React from "react";

import { BaseChannelType, ChannelTreeType, ChildChannelType } from "core/channels/type/channels.type";

import { Highlight } from "components/table/Highlight";

import ChannelProcessor from "./channels-processor";
import styles from "./channels-selection.module.css";
import ChildChannel from "./child-channel";
import EmptyChild from "./empty-child";
import RecommendedToggle from "./recommended-toggle";

type Props = {
  rowDefinition: ChannelTreeType;
  search: string;
  openRows: Set<number>;
  selectedRows: Set<number>;
  selectedBaseChannelId: number | undefined;
  channelProcessor: Readonly<ChannelProcessor>;
  onToggleChannelSelect: (channel: BaseChannelType | ChildChannelType, toState?: boolean) => void;
  onToggleChannelOpen: (channel: BaseChannelType) => void;
};

const BaseChannel = (props: Props) => {
  const { base, children } = props.rowDefinition;
  const { id, name } = base;
  const isSelectedBaseChannel = id === props.selectedBaseChannelId;
  const isOpen = props.openRows.has(id);
  const isSelected = props.selectedRows.has(id);
  const identifier = "base_" + id;
  const selectedChildrenCount = children
    .map((child) => child.id)
    .reduce((total: number, id) => {
      return total + Number(props.selectedRows.has(id as number));
    }, 0);
  const totalSelectedCount = Number(isSelected) + selectedChildrenCount;

  return (
    <React.Fragment>
      <h4
        className={`${styles.base_channel} ${isSelectedBaseChannel ? styles.initial_selected : ""}`}
        title={isSelectedBaseChannel ? t("New base channel") : undefined}
        onClick={() => props.onToggleChannelOpen(base)}
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
            props.onToggleChannelSelect(base);
          }}
          disabled={isSelectedBaseChannel}
        />
        <i className={`${styles.arrow} fa ${isOpen ? "fa-angle-down" : "fa-angle-right"}`} />
        <Highlight
          className={styles.collapsible}
          enabled={props.search.length > 0}
          text={name}
          highlight={props.search}
        />
        {totalSelectedCount > 0 && <b className={styles.count}>{`(${totalSelectedCount})`}</b>}
      </h4>
      {isOpen ? (
        <React.Fragment>
          {props.rowDefinition.children.length === 0 ? <EmptyChild key={`empty_child_${id}`} /> : null}
          {base.recommendedChildren.length ? (
            <RecommendedToggle
              key={`recommended_toggle_${id}`}
              base={base}
              selectedRows={props.selectedRows}
              onToggleChannelSelect={props.onToggleChannelSelect}
            />
          ) : null}
          {props.rowDefinition.children.map((child) => (
            <ChildChannel
              key={child.id}
              definition={child}
              search={props.search}
              selectedRows={props.selectedRows}
              channelProcessor={props.channelProcessor}
              onToggleChannelSelect={props.onToggleChannelSelect}
            />
          ))}
        </React.Fragment>
      ) : null}
    </React.Fragment>
  );
};

export default BaseChannel;
