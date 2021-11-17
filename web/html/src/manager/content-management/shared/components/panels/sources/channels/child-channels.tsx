import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { ChannelAnchorLink } from "components/links";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import { ChannelType } from "core/channels/type/channels.type";
import { RequiredChannelsResultType } from "core/channels/api/use-mandatory-channels-api";

type PropsType = {
  base?: ChannelType;
  search: string;
  childChannelsId: Array<number>;
  selectedChannelsIdsInGroup: Array<number>;
  onChannelToggle: Function;
  channelsTree: ChannelsTreeType;
  requiredChannelsResult: RequiredChannelsResultType;
};

const ChildChannels = (props: PropsType) => {
  if (props.childChannelsId.length === 0) {
    return <span>&nbsp;{t("no child channels")}</span>;
  }

  const { requiredChannels, dependenciesTooltip } = props.requiredChannelsResult;

  return (
    <>
      {props.childChannelsId.map((channelId: number) => {
        const channel = props.channelsTree.channelsById[channelId];
        const childId = "child_" + channel.id;
        const toolTip = dependenciesTooltip(channel.id, Object.values(props.channelsTree.channelsById));

        const mandatoryChannelsForBaseId = props.base && requiredChannels.get(props.base.id);
        const isMandatory = Boolean(mandatoryChannelsForBaseId?.has(channel.id));

        return (
          <div key={channel.id} className="checkbox">
            <input
              type="checkbox"
              value={channel.id}
              id={childId}
              name="childChannels"
              checked={props.selectedChannelsIdsInGroup.includes(channel.id)}
              onChange={(event) => props.onChannelToggle(parseInt(event.target.value, 10))}
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
      })}
    </>
  );
};

export default ChildChannels;
