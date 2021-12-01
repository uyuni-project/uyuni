import * as React from "react";
import { Toggler } from "components/toggler";

import { RecommendedToggleRowDefinition } from "./channels-selection-rows";

type Props = {
  definition: RecommendedToggleRowDefinition;
  areAllRecommendedChildrenSelected: boolean;
  onSetRecommendedChildrenSelected: (id: number, selected: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  const { channelId } = props.definition;
  return (
    <div style={{ paddingLeft: 15, paddingTop: 5 }}>
      <Toggler
        value={props.areAllRecommendedChildrenSelected}
        handler={() => props.onSetRecommendedChildrenSelected(channelId, !props.areAllRecommendedChildrenSelected)}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
