import { FilterOptionType, TableFilter } from "components/table/TableFilter";

const SYSTEM_KIND_OPTIONS = [
  { value: "mgr_server", label: t("Manager Server") },
  { value: "physical", label: t("Physical") },
  { value: "proxy", label: t("Proxy") },
  { value: "virtual_guest", label: t("Virtual Guest") },
  { value: "virtual_host", label: t("Virtual Host") },
];

const SYSTEM_TYPE_OPTIONS = [
  { value: "ansible_control_node", label: t("Ansible Control Node") },
  { value: "bootstrap_entitled", label: t("Bootstrap") },
  { value: "container_build_host", label: t("Container Build Host") },
  { value: "foreign_entitled", label: t("Foreign") },
  { value: "enterprise_entitled", label: t("Management") },
  { value: "monitoring_entitled", label: t("Monitored Host") },
  { value: "osimage_build_host", label: t("OS Image Build Host") },
  { value: "salt_entitled", label: t("Salt") },
  { value: "virtualization_host", label: t("Virtualization Host") },
];

const STATUS_TYPE_OPTIONS = [
  { value: "actions scheduled", label: t("Actions scheduled") },
  { value: "updates scheduled", label: t("All updates scheduled") },
  { value: "critical", label: t("Critical updates available") },
  { value: "kickstarting", label: t("Kickstart in progress") },
  { value: "up2date", label: t("System is up to date") },
  { value: "awol", label: t("System not checking in") },
  { value: "unentitled", label: t("System not entitled") },
  { value: "reboot needed", label: t("System requires reboot") },
  { value: "updates", label: t("Updates available") },
];

const YES_NO_OPTIONS = [
  { value: "true", label: t("Yes") },
  { value: "false", label: t("No") },
];

const allListOptions = [
  { value: "server_name", label: t("System"), type: FilterOptionType.TEXT },
  { value: "system_kind", label: t("System Kind"), type: FilterOptionType.SELECT, filterOptions: SYSTEM_KIND_OPTIONS },
  { value: "status_type", label: t("Updates"), type: FilterOptionType.SELECT, filterOptions: STATUS_TYPE_OPTIONS },
  { value: "total_errata_count", label: t("Patches"), type: FilterOptionType.NUMERIC },
  { value: "outdated_packages", label: t("Packages"), type: FilterOptionType.NUMERIC },
  { value: "extra_pkg_count", label: t("Extra Packages"), type: FilterOptionType.NUMERIC },
  { value: "config_files_with_differences", label: t("Config Diffs"), type: FilterOptionType.NUMERIC },
  { value: "channel_labels", label: t("Base Channel"), type: FilterOptionType.TEXT },
  {
    value: "entitlement_level",
    label: t("System Type"),
    type: FilterOptionType.SELECT,
    filterOptions: SYSTEM_TYPE_OPTIONS,
  },
  {
    value: "requires_reboot",
    label: t("Requires Reboot"),
    type: FilterOptionType.SELECT,
    filterOptions: YES_NO_OPTIONS,
  },
  { value: "created_days", label: t("Registered Days"), type: FilterOptionType.NUMERIC },
  { value: "group_count", label: t("Groups"), type: FilterOptionType.NUMERIC },
];

const virtualSystemsListOptions = [
  { value: "host_server_name", label: t("Virtual Host") },
  { value: "server_name", label: t("Virtual System") },
];

export const SystemsListFilter = (props) => {
  return <TableFilter filterOptions={allListOptions} name="criteria" {...props} />;
};

export const VirtualSystemsListFilter = (props) => {
  return <TableFilter filterOptions={virtualSystemsListOptions} name="criteria" {...props} />;
};
