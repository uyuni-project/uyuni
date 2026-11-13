/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { ReactNode } from "react";

import styles from "./example-layout.module.scss";

type Props = {
  children?: ReactNode;
};

export const ExampleSection = ({ children }: Props) => <div className={styles.section}>{children}</div>;

export const StripedExampleSection = ({ children }: Props) => (
  <div className={`${styles.section} ${styles.striped}`}>{children}</div>
);

export const ExampleRow = ({ children }: Props) => <div className={styles.row}>{children}</div>;
