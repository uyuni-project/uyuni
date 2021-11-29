import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { ChannelAnchorLink } from "components/links";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import { ChannelType, DerivedChildChannel } from "core/channels/type/channels.type";
import { RequiredChannelsResultType } from "core/channels/api/use-mandatory-channels-api";

type Props = {
  channel: DerivedChildChannel;
  selectedChannelIds: Set<number>;
  search: string;
  onToggleChannelSelect: (channel: DerivedChildChannel) => void;
};

const ChildChannel = (props: Props) => {
  // TODO: Tack on in worker?
  // const { requiredChannels, dependenciesTooltip } = props.requiredChannelsResult;
  const requiredChannels = new Map();

  const channel = props.channel;
  const childId = "child_" + channel.id;
  // TODO: Tack on in worker
  // const toolTip = dependenciesTooltip(channel.id, Object.values(props.channelsTree.channelsById));
  const toolTip = undefined;
  const isMandatory = Boolean(channel.parent.mandatory.includes(channel.id));

  return (
    <div className="checkbox" style={{ paddingLeft: 35 }}>
      <input
        type="checkbox"
        value={channel.id}
        id={childId}
        name="childChannels"
        checked={props.selectedChannelIds.has(channel.id)}
        onChange={() => props.onToggleChannelSelect(channel)}
      />
      <label title={toolTip || undefined} htmlFor={childId}>
        <Highlight enabled={props.search?.length > 0} text={channel.name} highlight={props.search}></Highlight>
      </label>
      &nbsp;
      {toolTip ? ( // eslint-disable-next-line jsx-a11y/anchor-is-valid
        <a href="#">
          <i className="fa fa-info-circle spacewalk-help-link" title={toolTip}></i>
        </a>
      ) : null}
      &nbsp;
      {channel.recommended ? (
        <span className="recommended-tag-base" title={t("This channel is recommended")}>
          {t("recommended")}
        </span>
      ) : null}
      {isMandatory ? (
        <span className="mandatory-tag-base" title={t("This channel is mandatory")}>
          {t("mandatory")}
        </span>
      ) : null}
      <ChannelAnchorLink id={channel.id} newWindow={true} />
    </div>
  );
};

export default ChildChannel;
