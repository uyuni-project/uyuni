/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

const targetTypeToString = (targetType?: string) => {
  switch (targetType) {
    case "MINION":
      return t("Minion");
    case "GROUP":
      return t("Group");
    case "ORG":
      return t("Organization");
    case "NO_ENTITY":
    case "NO-ENTITY":
      return t("Content Lifecycle");
  }
  return null;
};

const targetNameLink = (
  targetName?: string,
  targetType?: string,
  targetId?: number,
  targetAccessible?: boolean,
  projectLabel?: string
) => {
  if (!targetAccessible && targetType !== "NO_ENTITY" && targetType !== "NO-ENTITY") {
    return targetName;
  }
  switch (targetType) {
    case "MINION":
      return <a href={"/rhn/manager/systems/details/recurring-actions?sid=" + targetId}>{targetName}</a>;
    case "GROUP":
      return <a href={"/rhn/manager/groups/details/recurring-actions?sgid=" + targetId}>{targetName}</a>;
    case "ORG":
      if (window.JSONMenu.filter((item) => item.label === "Admin").length) {
        return <a href={"/rhn/manager/multiorg/recurring-actions?oid=" + targetId}>{targetName}</a>;
      }
      return <a href={"/rhn/manager/yourorg/recurring-actions"}>{targetName}</a>;
    case "NO_ENTITY":
    case "NO-ENTITY":
      if (projectLabel) {
        return <a href={"/rhn/manager/contentmanagement/project/" + projectLabel}>{targetName}</a>;
      }
      return <a href="/rhn/manager/contentmanagement/projects">{targetName}</a>;
  }
  return targetName ?? null;
};

const inferEntityParams = () => {
  if (window.entityType === "GROUP") {
    return "/GROUP/" + window.groupId;
  } else if (window.entityType === "ORG") {
    return "/ORG/" + window.orgId;
  } else if (window.entityType === "MINION") {
    return "/MINION/" + window.minions?.[0].id;
  } else if (window.entityType === "CLM") {
    return "/CLM/0";
  }
  return "";
};

const isReadOnly = (item) => {
  if (window.entityType === "CLM") {
    return false;
  }
  const params = inferEntityParams().split("/");
  return !(params[1] === item.targetType && params[2] === item.targetId?.toString());
};

export { targetTypeToString, targetNameLink, inferEntityParams, isReadOnly };
