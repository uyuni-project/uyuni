import type { ReactNode } from "react";

import styles from "./layout.module.scss";

type Props = {
  children?: ReactNode;
};

export const StorySection = (props: Props) => {
  return (
    <>
      <div className={styles.section}>{props.children}</div>
    </>
  );
};

export const StripedStorySection = (props: Props) => {
  return (
    <>
      <div className={`${styles.section} ${styles.striped}`}>{props.children}</div>
    </>
  );
};

type RowProps = {
  children?: ReactNode;
};

export const StoryRow = (props: RowProps) => {
  return <div className={styles.row}>{props.children}</div>;
};
