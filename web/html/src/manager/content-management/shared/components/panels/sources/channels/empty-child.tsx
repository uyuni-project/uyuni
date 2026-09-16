/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import styles from "./channels-selection.module.scss";

const EmptyChild = () => {
  return (
    <div className={styles.child_channel}>
      <span>&nbsp;{t("no child channels")}</span>
    </div>
  );
};

export default EmptyChild;
