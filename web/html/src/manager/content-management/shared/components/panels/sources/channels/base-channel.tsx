import * as React from "react";

import { ChannelTreeType } from "core/channels/type/channels.type";

import { Highlight } from "components/table/Highlight";

import { ChannelProcessor } from "./channel-processor";
import styles from "./channels-selection.module.scss";
import ChildChannel from "./child-channel";
import EmptyChild from "./empty-child";
import RecommendedToggle from "./recommended-toggle";

type Props = {
  /** The base channel with all its children */
  channelTree: ChannelTreeType;
  /** The text currently used to filter the list of channels */
  search?: string;
  /** true if the base channel is shown and selectable */
  showBase?: boolean;
  /** true if the component should be shown with all the children channels expanded */
  isOpen?: boolean;
  /** true to show the recommended toggle, if the base recommends any channel */
  recommendedToggle?: boolean;
  /** The ids of the currently selected channels */
  selectedChannelIds: Set<number>;
  /** The channel processor used to retrieve the information about the channels */
  channelProcessor: Readonly<ChannelProcessor>;
  /** Callback to invoke when a channel is selected/deselected */
  onToggleChannelSelect: (channelId: number, toState?: boolean) => void;
  /** callback to invoke when a channel is opened/collapsed */
  onToggleChannelOpen?: (channelId: number) => void;
};

const BaseChannel: React.FC<Props> = (props: Props): React.ReactElement => {
  const { base, children } = props.channelTree;
  const { id, name } = base;

  const isSelectedBaseChannel = id === props.channelProcessor.getSelectedBaseChannelId();
  const isSelected = props.selectedChannelIds.has(id);
  const identifier = "base_" + id;

  const selectedChildrenCount = children
    .map((child) => child.id)
    .reduce((total: number, id: number) => total + Number(props.selectedChannelIds.has(id)), 0);
  const totalSelectedCount = Number(isSelected) + selectedChildrenCount;

  function renderBaseChannel(): React.ReactElement {
    return (
      <h4
        className={`${styles.base_channel} ${isSelectedBaseChannel ? styles.initial_selected : ""}`}
        title={isSelectedBaseChannel ? t("New base channel") : undefined}
        onClick={() => props.onToggleChannelOpen?.(id)}
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
        <i className={`${styles.arrow} fa ${props.isOpen ? "fa-angle-down" : "fa-angle-right"}`} />
        <Highlight
          className={styles.collapsible}
          enabled={props.search !== undefined && props.search.length > 0}
          text={name}
          highlight={props.search}
        />
        {totalSelectedCount > 0 && <b className={styles.count}>{`(${totalSelectedCount})`}</b>}
      </h4>
    );
  }

  function renderChildren(): React.ReactElement {
    if (props.channelTree.children.length === 0) {
      return <EmptyChild key={`empty_child_${id}`} />;
    }

    return (
      <>
        {props.recommendedToggle && base.recommendedChildrenIds.length > 0 && (
          <RecommendedToggle
            key={`recommended_toggle_${id}`}
            base={base}
            selectedRows={props.selectedChannelIds}
            onToggleChannelSelect={props.onToggleChannelSelect}
          />
        )}
        {props.channelTree.children.map((child) => (
          <ChildChannel
            key={child.id}
            channel={child}
            search={props.search}
            isSelected={props.selectedChannelIds.has(child.id)}
            channelProcessor={props.channelProcessor}
            onToggleChannelSelect={props.onToggleChannelSelect}
          />
        ))}
      </>
    );
  }

  return (
    <>
      {props.showBase && renderBaseChannel()}
      {(props.isOpen || !props.showBase) && renderChildren()}
    </>
  );
};

BaseChannel.defaultProps = {
  search: "",
  showBase: true,
  isOpen: true,
  recommendedToggle: true,
  onToggleChannelOpen: (_channel) => {},
};

export default BaseChannel;
