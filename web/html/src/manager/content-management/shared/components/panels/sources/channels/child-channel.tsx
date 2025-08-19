import * as React from "react";

import { BaseChannelType, ChildChannelType } from "core/channels/type/channels.type";

import { ChannelAnchorLink } from "components/links";
import { Highlight } from "components/table/Highlight";

import { ChannelDependencyData, ChannelProcessor } from "./channel-processor";
import styles from "./channels-selection.module.scss";

type Props = {
  definition: ChildChannelType;
  search: string;
  selectedRows: Set<number>;
  channelProcessor: Readonly<ChannelProcessor>;
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

const ChildChannel = (props: Props) => {
  const { id, name, recommended, parent } = props.definition;
  const isSelected = props.selectedRows.has(id);
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
        checked={isSelected}
        onClick={() => props.onToggleChannelSelect(props.definition)}
        disabled={isRequiredBySelectedBaseChannel}
      />
      <label className={`${styles.collapsible} ${styles.child_name}`} title={tooltip || undefined} htmlFor={identifier}>
        <Highlight enabled={props.search?.length > 0} text={name} highlight={props.search}></Highlight>
      </label>
      <span>
        {tooltip ? ( // eslint-disable-next-line jsx-a11y/anchor-is-valid
          <a href="#">
            <i className="fa fa-info-circle spacewalk-help-link" title={tooltip}></i>
          </a>
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
