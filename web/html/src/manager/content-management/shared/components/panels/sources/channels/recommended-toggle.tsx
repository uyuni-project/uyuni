import * as React from "react";
import { Toggler } from "components/toggler";

import { DerivedBaseChannel } from "core/channels/type/channels.type";
import { ChannelsTreeType } from "core/channels/api/use-channels-tree-api";
import { getAllRecommentedIdsByBaseId } from "core/channels/utils/channels-state.utils";

type Props = {
  channel: DerivedBaseChannel;
  selectedChannelIds: Set<number>;
  onToggleRecommended: (areSelected: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  // TODO: Move this to state or memo so it isn't recomputed all the time
  const { recommendedIds, areRecommendedChildrenSelected } = getAllRecommentedIdsByBaseId(
    props.parent.id,
    props.channelsTree,
    props.selectedChannelsIdsInGroup
  );

  if (recommendedIds.filter((id) => id !== props.parent.id).length === 0) {
    return null;
  }
  const toggleRecommended = () =>
    areRecommendedChildrenSelected ? props.onToggleRecommended(false) : props.onToggleRecommended(true);
  return (
    <div style={{ paddingLeft: 15, paddingTop: 5 }}>
      <Toggler
        handler={() => toggleRecommended()}
        value={areRecommendedChildrenSelected}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
