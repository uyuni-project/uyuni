import * as React from "react";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { IconTag } from "./icontag";

function statusDisplay(system: any, isAdmin: boolean) {
  const sid = system["systemId"];
  const type = system["statusType"];

  const systems = {
    untitled: {
      iconTitle: "System not entitled",
      iconType: "system-unknown",
      url: isAdmin && "/rhn/systems/details/Edit.do?sid=" + sid,
    },
    awol: {
      iconTitle: "System not checking in",
      iconType: "system-unknown",
      url: null,
    },
    kickstarting: {
      iconType: "system-kickstarting",
      iconTitle: "Kickstart in progress",
      url: "/rhn/systems/details/kickstart/SessionStatus.do?sid=" + sid,
    },
    "updates scheduled": {
      iconType: "action-pending",
      iconTitle: "All updates scheduled",
      url: "/rhn/systems/details/history/Pending.do?sid=" + sid,
    },
    "actions scheduled": {
      iconType: "action-pending",
      iconTitle: "Actions scheduled",
      url: "/rhn/systems/details/history/Pending.do?sid=" + sid,
    },
    up2date: {
      iconType: "system-ok",
      iconTitle: "System is up to date",
      url: null,
    },
    critical: {
      iconType: "system-crit",
      iconTitle: "Critical updates available",
      url: "/rhn/systems/details/ErrataList.do?sid=" + sid + "&type=" + t("Security Advisory"),
    },
    updates: {
      iconType: "system-warn",
      iconTitle: "Updates available",
      url: "/rhn/systems/details/packages/UpgradableList.do?sid=" + sid,
    },
  };

  const { iconType, iconTitle, url } = systems[type];

  var locked: React.ReactNode = "";
  if (DEPRECATED_unsafeEquals(system["locked"], 1)) {
    locked = <IconTag type="system-locked" title={t("System Locked")} />;
  }
  return (
    <div>
      <a href={url}>
        <IconTag type={iconType} title={t(iconTitle)} />
      </a>
      {locked}
    </div>
  );
}

export { statusDisplay };
