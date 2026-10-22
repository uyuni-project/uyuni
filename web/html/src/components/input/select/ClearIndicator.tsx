import { ClearIndicatorProps } from "react-select";

import { IconTag } from "components/icontag";

import styles from "./ClearIndicator.module.scss";

export const ClearIndicator = (props: ClearIndicatorProps) => {
  const {
    getStyles,
    innerProps: { ref, ...restInnerProps },
  } = props;
  return (
    <button
      {...restInnerProps}
      className={`is-plain ${styles.button}`}
      ref={ref}
      style={getStyles("clearIndicator", props)}
    >
      <IconTag icon="fa-times" className={styles.icon} ariaLabel={t("Clear")} ariaHidden={false} />
    </button>
  );
};
