import { BaseChannelType } from "core/channels/type/channels.type";

import { Toggler } from "components/toggler";

import styles from "./channels-selection.module.scss";

type Props = {
  base: BaseChannelType;
  selectedRows: Set<number>;
  onToggleChannelSelect: (channelId: number, toState?: boolean) => void;
};

const RecommendedToggle = (props: Props) => {
  const { base, selectedRows } = props;
  const recommendedChildrenIds = base.recommendedChildrenIds;
  const areAllRecommendedChildrenSelected = recommendedChildrenIds.every((childId) => selectedRows.has(childId));
  return (
    <div className={styles.child_channel}>
      <Toggler
        value={areAllRecommendedChildrenSelected}
        handler={() => {
          recommendedChildrenIds.forEach((childId) =>
            props.onToggleChannelSelect(childId, !areAllRecommendedChildrenSelected)
          );
        }}
        text={t("include recommended")}
      />
    </div>
  );
};

export default RecommendedToggle;
