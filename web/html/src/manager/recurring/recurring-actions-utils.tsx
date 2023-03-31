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

const targetNameLink = (targetName?: string, targetType?: string, targetId?: number) => {
  switch (targetType) {
    case "MINION":
      return <a href={"/rhn/systems/details/Overview.do?sid=" + targetId}>{targetName}</a>;
    case "GROUP":
      return <a href={"/rhn/groups/GroupDetail.do?sgid=" + targetId}>{targetName}</a>;
    case "ORG":
      return <a href={"/rhn/systems/details/Overview.do?sid=" + targetId}>{targetName}</a>;
  }
  return null;
};

export { targetTypeToString, targetNameLink };
