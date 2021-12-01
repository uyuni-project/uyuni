import * as React from "react";
import { Toggler } from "components/toggler";

import { DerivedBaseChannel } from "core/channels/type/channels.type";

type Props = {
  channel: DerivedBaseChannel;
  areAllRecommendedChildrenSelected: boolean;
  onSetRecommendedChildrenSelected: (id: number, selected: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  return (
    <div style={{ paddingLeft: 15, paddingTop: 5 }}>
      <Toggler
        value={props.areAllRecommendedChildrenSelected}
        handler={() =>
          props.onSetRecommendedChildrenSelected(props.channel.id, !props.areAllRecommendedChildrenSelected)
        }
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
