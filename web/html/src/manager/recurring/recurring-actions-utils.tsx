const targetTypeToString = (targetType?: string) => {
  switch (targetType) {
    case "MINION":
      return t("Minion");
    case "GROUP":
      return t("Group");
    case "ORG":
      return t("Organization");
  }
  return null;
};

const targetNameLink = (targetName?: string, targetType?: string, targetId?: number, targetAccessible?: boolean) => {
  if (!targetAccessible) {
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
  }
  return null;
};

const inferEntityParams = () => {
  if (window.entityType === "GROUP") {
    return "/GROUP/" + window.groupId;
  } else if (window.entityType === "ORG") {
    return "/ORG/" + window.orgId;
  } else if (window.entityType === "MINION") {
    return "/MINION/" + window.minions?.[0].id;
  }
  return "";
};

const isReadOnly = (item) => {
  const params = inferEntityParams().split("/");
  return !(params[1] === item.targetType && params[2] === item.targetId.toString());
};

export { targetTypeToString, targetNameLink, inferEntityParams, isReadOnly };
