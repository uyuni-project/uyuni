import type { ReactNode } from "react";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { IconTag } from "./icontag";

export type SystemOverview = {
  id: number;
  serverName: string;
  isVirtualGuest: boolean;
  isVirtualHost: boolean;
  entitlement?: string[];
  proxy: boolean;
  mgrServer: boolean;
};

export function iconAndName(system: SystemOverview) {
  const iconMapping = [
    {
      iconType: "system-bare-metal",
      iconTitle: t("Unprovisioned System"),
      condition: (sys: SystemOverview) => sys.entitlement?.includes("bootstrap_entitled"),
    },
    {
      iconType: "system-virt-guest",
      iconTitle: t("Virtual Guest"),
      condition: (sys: SystemOverview) => sys.isVirtualGuest,
    },
    {
      iconType: "system-virt-host",
      iconTitle: t("Virtual Host"),
      condition: (sys: SystemOverview) => sys.isVirtualHost,
    },
    {
      iconType: "system-physical",
      iconTitle: t("Non-Virtual System"),
      condition: () => true,
    },
  ] as const;
  const systemIcon = iconMapping
    .filter((item) => item.condition(system))
    .map((item) => (
      <IconTag
        type={item.iconType}
        size="lg"
        title={item.iconTitle}
        key={item.iconTitle || item.iconType}
      />
    ))[0];

  const proxyIcon = system.proxy ? <IconTag type="header-proxy" title={t("Proxy")} /> : "";
  const mgrServerIcon = system.mgrServer ? (
    <IconTag type="header-mgr-server" title={t("Peripheral Server")} />
  ) : (
    ""
  );

  const content = [systemIcon, proxyIcon, mgrServerIcon, system.serverName];

  if (!DEPRECATED_unsafeEquals(system.id, null)) {
    return (
      <a href={`/rhn/systems/details/Overview.do?sid=${system.id}`} className="js-spa">
        {content}
      </a>
    );
  }
  return content;
}

function statusDisplay(system: any, isAdmin: boolean) {
  const sid = system["systemId"] || system["id"];
  const type = system["statusType"];

  const systems = {
    unentitled: {
      iconType: "system-unknown",
      iconTitle: "System not entitled",
      url: isAdmin && "/rhn/systems/details/Edit.do?sid=" + sid,
    },
    awol: {
      iconType: "system-unknown",
      iconTitle: "System not checking in",
      url: null,
    },
    kickstarting: {
      iconType: "system-kickstarting",
      iconTitle: "Kickstart in progress",
      url: "/rhn/systems/details/kickstart/SessionStatus.do?sid=" + sid,
    },
    "reboot needed": {
      iconType: "system-reboot",
      iconTitle: "System requires reboot",
      url: "/rhn/systems/details/RebootSystem.do?sid=" + sid,
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
      iconStatus: "success",
      iconTitle: "System is up to date",
      url: null,
    },
    critical: {
      iconType: "system-crit",
      iconStatus: "danger",
      iconTitle: "Critical updates available",
      url: "/rhn/systems/details/ErrataList.do?sid=" + sid + "&type=" + t("Security Advisory"),
    },
    updates: {
      iconType: "system-warn",
      iconStatus: "warning",
      iconTitle: "Updates available",
      url: "/rhn/systems/details/packages/UpgradableList.do?sid=" + sid,
    },
  };

  const { iconType, iconStatus, iconTitle, url } = systems[type];

  let locked: ReactNode = "";
  if (DEPRECATED_unsafeEquals(system["locked"], 1)) {
    locked = <IconTag type="system-locked" size="lg" title={t("System Locked")} />;
  }
  return (
    <div>
      <a href={url}>
        <IconTag type={iconType} size="lg" status={iconStatus ? iconStatus : undefined} title={t(iconTitle)} />
      </a>
      {locked}
    </div>
  );
}

export { statusDisplay };
