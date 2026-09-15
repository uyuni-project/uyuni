/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { BootstrapPanel } from "components/panels/BootstrapPanel";

function ImageViewBuildLog(props) {
  return (
    <BootstrapPanel title={t("Build Log")}>
      <div className="auto-overflow">
        {props.data.buildlog ? (
          <textarea className="form-control" name="buildlog" rows={30} value={props.data.buildlog} readOnly />
        ) : (
          t("Build log is not available.")
        )}
      </div>
    </BootstrapPanel>
  );
}

export { ImageViewBuildLog };
