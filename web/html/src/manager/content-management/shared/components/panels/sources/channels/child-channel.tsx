import * as React from "react";
import { Highlight } from "components/table/Highlight";
import { ChannelAnchorLink } from "components/links";
import { BaseRowDefinition, ChildRowDefinition } from "./channels-selection-rows";

import styles from "./channels-selection.css";

type Props = {
  definition: ChildRowDefinition;
  search: string;
  selectedRows: Set<number>;
  onToggleChannelSelect: (channel: BaseRowDefinition | ChildRowDefinition, toState?: boolean) => void;
};

const getTooltip = (tooltipData: ChildRowDefinition["tooltipData"]) => {
  let tooltip = "";
  if (tooltipData.requiresNames.length) {
    tooltip += `${t("This channel requires:")}\n\n${tooltipData.requiresNames.join("\n")}`;
  }
  if (tooltip) {
    tooltip += "\n\n";
  }
  if (tooltipData.requiredByNames.length) {
    tooltip += `${t("This channel is required by:")}\n\n${tooltipData.requiredByNames.join("\n")}`;
  }
  return tooltip;
};

const ChildChannel = (props: Props) => {
  const { id, channelName, isRecommended, isRequired, isRequiredBySelectedBaseChannel, tooltipData } = props.definition;
  const isSelected = props.selectedRows.has(id);
  const identifier = "child_" + id;
  const tooltip = getTooltip(tooltipData);

  return (
    <div className={styles.nested_row}>
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
        <Highlight enabled={props.search?.length > 0} text={channelName} highlight={props.search}></Highlight>
      </label>
      <span>
        {tooltip ? ( // eslint-disable-next-line jsx-a11y/anchor-is-valid
          <a href="#">
            <i className="fa fa-info-circle spacewalk-help-link" title={tooltip}></i>
          </a>
        ) : null}
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
      </span>
    </div>
  );
};

export default ChildChannel;
