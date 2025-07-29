import * as React from "react";
import { useState } from "react";

import { Button, LinkButton } from "components/buttons";
import { TabContainer } from "components/tab-container";

type Tab = {
  title: string;
  content: React.ReactNode;
  validate?: boolean | null;
};

type AccessGroupTabContainerProps = {
  tabs: Tab[];
  onUpdate?: Function;
  onCancel?: string;
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
    <div>
      <div className="main-content">
        <div className="d-block mb-3">
          <Button className="btn-primary pull-right" text={t("Update Access Group")} handler={onUpdate} />
        </div>
        <div className="content-section">
          <TabContainer
            labels={labels}
            hashes={hashes}
            tabs={tabs}
            initialActiveTabHash={currentTab}
            onTabHashChange={onTabHashChanged}
          />
        </div>
      </div>
      <div className="progress-bar-footer">
        <LinkButton className="btn-default btn-sm pull-left" text={t("Cancel")} href={props.onCancel} />
      </div>
    </div>
  );
};

export default AccessGroupTabContainer;
