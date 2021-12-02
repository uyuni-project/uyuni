import * as React from "react";
import { Toggler } from "components/toggler";

import { RecommendedToggleRowDefinition } from "./channels-selection-rows";

import styles from "./channels-selection.css";

type Props = {
  definition: RecommendedToggleRowDefinition;
  onSetRecommendedChildrenSelected: (id: number, selected: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  const { channelId, areAllRecommendedChildrenSelected } = props.definition;
  return (
    <div className={styles.nested_row}>
      <Toggler
        value={areAllRecommendedChildrenSelected}
        handler={() => props.onSetRecommendedChildrenSelected(channelId, !areAllRecommendedChildrenSelected)}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
