export const targetTypeToString = (targetType?: string) => {
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
