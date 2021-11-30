import * as React from "react";
import { Toggler } from "components/toggler";

import { DerivedBaseChannel } from "core/channels/type/channels.type";

type Props = {
  channel: DerivedBaseChannel;
  areAllRecommendedChildrenSelected: boolean;
  onToggleRecommendedChildrenSelected: () => void;
};

const RecommendedToggle = (props: Props) => {
  return (
    <div style={{ paddingLeft: 15, paddingTop: 5 }}>
      <Toggler
        value={props.areAllRecommendedChildrenSelected}
        handler={() => props.onToggleRecommendedChildrenSelected()}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
