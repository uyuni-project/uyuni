import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { ChannelAnchorLink } from "components/links";
import { ChildRowDefinition } from "./channels-selection-rows";

type Props = {
  definition: ChildRowDefinition;
  search: string;
  onToggleChannelSelect: (id: number) => void;
};

const ChildChannel = (props: Props) => {
  const { id, channelName, isSelected, isRequired, isRecommended } = props.definition;
  const identifier = "child_" + id;
  // TODO: Tack on in worker
  // const toolTip = dependenciesTooltip(channel.id, Object.values(props.channelsTree.channelsById));
  const toolTip = undefined;

  return (
    <div className="checkbox" style={{ paddingLeft: 35 }}>
      <input
        type="checkbox"
        value={id}
        id={identifier}
        name="childChannels"
        checked={isSelected}
        onChange={() => props.onToggleChannelSelect(id)}
      />
      <label title={toolTip || undefined} htmlFor={identifier}>
        <Highlight enabled={props.search?.length > 0} text={channelName} highlight={props.search}></Highlight>
      </label>
      &nbsp;
      {toolTip ? ( // eslint-disable-next-line jsx-a11y/anchor-is-valid
        <a href="#">
          <i className="fa fa-info-circle spacewalk-help-link" title={toolTip}></i>
        </a>
      ) : null}
      &nbsp;
      {isRecommended ? (
        <span className="recommended-tag-base" title={t("This channel is recommended")}>
          {t("recommended")}
        </span>
      ) : null}
      {isRequired ? (
        <span className="mandatory-tag-base" title={t("This channel is mandatory")}>
          {t("mandatory")}
        </span>
      ) : null}
      <ChannelAnchorLink id={id} newWindow={true} />
    </div>
  );
};

export default ChildChannel;
