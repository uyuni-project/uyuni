import * as React from "react";

import styles from "./channels-selection.module.css";

const EmptyChild = () => {
  return (
    <div className={styles.child_channel}>
      <span>&nbsp;{t("no child channels")}</span>
    </div>
  );
};

export default EmptyChild;
