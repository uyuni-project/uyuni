/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { AsyncButton } from "components/buttons";

type TopPanelButtonsProps = {
  onCreate: (...args: any[]) => any;
};

const TopPanelButtons = (props: TopPanelButtonsProps) => {
  return (
    <div className="btn-group">
      <AsyncButton
        id="savebutton"
        className="btn-primary"
        title={t("Create project")}
        text={t("Create")}
        action={props.onCreate}
      />
    </div>
  );
};

export default TopPanelButtons;
