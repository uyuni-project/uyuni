/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Panel } from "components/panels/Panel";

function ImageViewBuildLog(props) {
  return (
    <Panel headingLevel="h2" title={t("Build Log")}>
      <div className="auto-overflow">
        {props.data.buildlog ? (
          <textarea className="form-control" name="buildlog" rows={30} value={props.data.buildlog} readOnly />
        ) : (
          t("Build log is not available.")
        )}
      </div>
    </Panel>
  );
}

export { ImageViewBuildLog };
