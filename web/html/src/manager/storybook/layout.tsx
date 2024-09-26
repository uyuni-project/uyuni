import React from "react";

import styles from "./layout.module.less";

type Props = {
  children?: React.ReactNode;
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
  children?: React.ReactNode;
};

export const StoryRow = (props: RowProps) => {
  return <div className={styles.row}>{props.children}</div>;
};
