import * as React from "react";
import { useState } from "react";

import { Button, LinkButton } from "components/buttons";
import { TabContainer } from "components/tab-container";

import styles from "./AccessGroup.module.scss";

type Tab = {
  title: string;
  content: React.ReactNode;
};

type AccessGroupTabContainerProps = {
  tabs: Tab[];
  onUpdate?: Function;
  onCancelRedirectTo?: string;
};

const AccessGroupTabContainer = (props: AccessGroupTabContainerProps) => {
  const [currentTab, setCurrentTab] = useState("#details");

  const onUpdate = () => {
    if (props.onUpdate) {
      props.onUpdate();
    }
  };

  const onTabHashChanged = (hash) => {
    setCurrentTab(hash);
  };

  const hashes = props.tabs.map((tab) => "#" + tab.title.replace(" ", "-").toLowerCase());
  const labels = props.tabs.map((tab) => tab.title);
  const tabs = props.tabs.map((tab) => tab.content);

  return (
    <div className={styles.accessGroupWrapper}>
      <div className={styles.accessGroupContainer}>
        <TabContainer
          labels={labels}
          hashes={hashes}
          tabs={tabs}
          initialActiveTabHash={currentTab}
          onTabHashChange={onTabHashChanged}
        />
      </div>
      <div className="progress-bar-footer">
        <LinkButton className="btn-default btn-sm pull-left" text={t("Cancel")} href={props.onCancelRedirectTo} />
        <Button className="btn-primary pull-right" text={t("Update Access Group")} handler={onUpdate} />
      </div>
    </div>
  );
};

export default AccessGroupTabContainer;
