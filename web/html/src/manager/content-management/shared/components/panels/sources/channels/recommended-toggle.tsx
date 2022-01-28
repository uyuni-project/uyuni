import * as React from "react";
import { Toggler } from "components/toggler";

import { BaseRowDefinition, ChildRowDefinition, RecommendedToggleRowDefinition } from "./channels-selection-rows";

import styles from "./channels-selection.css";

type Props = {
  definition: RecommendedToggleRowDefinition;
  parent: BaseRowDefinition;
  selectedRows: Set<number>;
  onToggleChannelSelect: (channel: BaseRowDefinition | ChildRowDefinition, toState?: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  const { parent, selectedRows } = props;
  const recommendedChildren = parent.recommendedChildren;
  const areAllRecommendedChildrenSelected = recommendedChildren.every((child) => selectedRows.has(child.id));
  return (
    <div className={styles.nested_row}>
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
