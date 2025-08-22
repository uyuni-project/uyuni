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
  <Column columnKey="summary" comparator={Utils.sortByText} header={t("Summary")} cell={renderPackageSummary} />
);

export const PTF_COLUMN_ARCH = (
  <Column columnKey="arch" comparator={Utils.sortByText} header={t("Architecture")} cell={renderPackageArch} />
);

export const PTF_COLUMN_INSTALL_DATE = (
  <Column columnKey="installed" comparator={Utils.sortByDate} header={t("Installed")} cell={renderPackageInstallDate} />
);
