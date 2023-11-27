import { Column } from "components/table/Column";

import { Utils } from "utils/functions";

function renderPackageArch(item) {
  return item.arch;
}

function renderPackageSummary(item) {
  return item.summary;
}

function renderPackageInstallDate(item) {
  return item.installTime;
}

export const PTF_COLUMN_SUMMARY = (
  <Column
    key="extra-column-summary"
    columnKey="summary"
    comparator={Utils.sortByText}
    header={t("Summary")}
    cell={renderPackageSummary}
  />
);

export const PTF_COLUMN_ARCH = (
  <Column
    key="extra-column-arch"
    columnKey="arch"
    comparator={Utils.sortByText}
    header={t("Architecture")}
    cell={renderPackageArch}
  />
);

export const PTF_COLUMN_INSTALL_DATE = (
  <Column
    key="extra-column-installed"
    columnKey="installed"
    comparator={Utils.sortByDate}
    header={t("Installed")}
    cell={renderPackageInstallDate}
  />
);
