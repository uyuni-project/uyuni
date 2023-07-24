import { useState } from "react";

import { Select } from "components/input";
import { Form } from "components/input/Form";
import { SelectSearchField } from "components/table/SelectSearchField";

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
  { value: "server_name", label: t("System") },
  { value: "system_kind", label: t("System Kind"), filterOptions: SYSTEM_KIND_OPTIONS },
  { value: "status_type", label: t("Updates"), filterOptions: STATUS_TYPE_OPTIONS },
  { value: "total_errata_count", label: t("Patches") },
  { value: "outdated_packages", label: t("Packages") },
  { value: "extra_pkg_count", label: t("Extra Packages") },
  { value: "config_files_with_differences", label: t("Config Diffs") },
  { value: "channel_labels", label: t("Base Channel") },
  { value: "entitlement_level", label: t("System Type"), filterOptions: SYSTEM_TYPE_OPTIONS },
  { value: "requires_reboot", label: t("Requires Reboot"), filterOptions: YES_NO_OPTIONS },
  { value: "created_days", label: t("Registered Days") },
  { value: "group_count", label: t("Groups") },
];

const renderSearchField = (props) => {
  const { field } = props;
  const selectedOption = allListOptions.find((it) => it.value === field);
  if (selectedOption?.filterOptions) {
    return <SelectSearchField label={selectedOption.label} options={selectedOption.filterOptions} {...props} />;
  }
  return (
    <div className="form-group">
      <input
        className="form-control"
        value={props.criteria || ""}
        placeholder={props.placeholder}
        type="text"
        onChange={(e) => props.onSearch?.(e.target.value)}
        name={props.name}
      />
    </div>
  );
};

export const SystemsListFilter = (props) => {
  // Dummy model and onChange to reuse the Select component as it requires a Form
  let model = {};
  const onChange = () => {};

  const [filterValue, setFilterValue] = useState(props.field || "");
  const handleChangeSearchField = (value) => {
    setFilterValue(value);
    props.onSearchField?.(value);
  };

  return (
    <Form model={model} onChange={onChange} title={t("Filter")} className="row">
      <div className="col-sm-4">
        <Select
          name="filter"
          placeholder={t("Select a filter")}
          defaultValue={filterValue}
          options={allListOptions}
          onChange={(_name: string | undefined, value: string) => handleChangeSearchField(value)}
        />
      </div>
      <div className="col-sm-6">{props.field && renderSearchField(props)}</div>
    </Form>
  );
};
