import * as React from "react";

import { BaseChannelType, ChildChannelType } from "core/channels/type/channels.type";

import { ChannelAnchorLink } from "components/links";
import { Highlight } from "components/table/Highlight";

import { ChannelDependencyData, ChannelProcessor } from "./channel-processor";
import styles from "./channels-selection.module.scss";

type Props = {
  /** The child channel */
  channel: ChildChannelType;
  /** The text currently used to filter the list of channels */
  search?: string;
  /** true if this channel is selected */
  isSelected: boolean;
  /** The channel processor used to retrieve the information about the channels */
  channelProcessor: Readonly<ChannelProcessor>;
  /** Callback to invoke when a channel is selected/deselected */
  onToggleChannelSelect: (channel: BaseChannelType | ChildChannelType, toState?: boolean) => void;
};

function getTooltip(channelDependencies: ChannelDependencyData): string {
  let tooltip = "";
  if (channelDependencies.requiresNames.length) {
    const namesAsList = channelDependencies.requiresNames.map((name) => "  - " + name).join("\n");
    tooltip += `${t("This channel requires:")}\n${namesAsList}`;
  }
  if (tooltip) {
    tooltip += "\n\n";
  }
  if (channelDependencies.requiredByNames.length) {
    const namesAsList = channelDependencies.requiredByNames.map((name) => "  - " + name).join("\n");
    tooltip += `${t("This channel is required by:")}\n${namesAsList}`;
  }
  return tooltip;
}

const ChildChannel: React.FC<Props> = ({ search = "", ...props }: Props): React.ReactElement => {
  const { id, name, recommended, parent } = props.channel;
  const identifier = "child_" + id;

  const tooltip = getTooltip(props.channelProcessor.getDependencyData(id));
  const requiredBy = props.channelProcessor.getRequiredBy(id);

  const selectedBaseChannelId = props.channelProcessor.getSelectedBaseChannelId();
  const selectedBaseChannel = selectedBaseChannelId
    ? props.channelProcessor.getChannelById(selectedBaseChannelId)
    : undefined;
  const isRequiredBySelectedBaseChannel = Boolean(selectedBaseChannel && requiredBy?.has(selectedBaseChannel));
  const isReqiredByBase = requiredBy?.has(parent);

  return (
    <div className={styles.child_channel}>
      <input
        type="checkbox"
        value={id}
        id={identifier}
        name="childChannels"
        readOnly
        checked={props.isSelected}
        onClick={() => props.onToggleChannelSelect(props.channel)}
        disabled={isRequiredBySelectedBaseChannel}
      />
      <label className={`${styles.collapsible} ${styles.child_name}`} title={tooltip || undefined} htmlFor={identifier}>
        <Highlight enabled={search !== undefined && search.length > 0} text={name} highlight={search}></Highlight>
      </label>
      <span>
        {tooltip ? (
          <span>
            <i className="fa fa-info-circle spacewalk-help-link" title={tooltip}></i>
          </span>
        ) : null}
        {recommended ? (
          <span className="recommended-tag-base" title={t("This channel is recommended")}>
            {t("recommended")}
          </span>
        ) : null}
        {isReqiredByBase ? (
          <span className="mandatory-tag-base" title={t("This channel is mandatory")}>
            {t("mandatory")}
          </span>
        ) : null}
        <ChannelAnchorLink id={id} newWindow={true} />
      </span>
    </div>
  );
};

export default ChildChannel;
