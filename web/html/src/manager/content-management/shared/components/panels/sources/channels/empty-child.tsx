import styles from "./channels-selection.module.scss";

const EmptyChild = () => {
  return (
    <div className={styles.child_channel}>
      <span>&nbsp;{t("no child channels")}</span>
    </div>
  );
};

export default EmptyChild;
