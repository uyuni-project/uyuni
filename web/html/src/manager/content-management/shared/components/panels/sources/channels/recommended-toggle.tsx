import * as React from "react";

import { BaseChannelType, ChildChannelType } from "core/channels/type/channels.type";

import { Toggler } from "components/toggler";

import styles from "./channels-selection.module.css";

type Props = {
  base: BaseChannelType;
  selectedRows: Set<number>;
  onToggleChannelSelect: (channel: BaseChannelType | ChildChannelType, toState?: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  const { base, selectedRows } = props;
  const recommendedChildren = base.recommendedChildren;
  const areAllRecommendedChildrenSelected = recommendedChildren.every((child) => selectedRows.has(child.id));
  return (
    <div className={styles.child_channel}>
      <Toggler
        value={areAllRecommendedChildrenSelected}
        handler={() => {
          recommendedChildren.forEach((child) =>
            props.onToggleChannelSelect(child, !areAllRecommendedChildrenSelected)
          );
        }}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
