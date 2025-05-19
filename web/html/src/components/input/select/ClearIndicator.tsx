import { ClearIndicatorProps } from "react-select";

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
      <i className={`fa fa-times ${styles.icon}`} aria-label={t("Clear")} />
    </button>
  );
};
