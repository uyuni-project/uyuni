import * as React from "react";
import { useState } from "react";
import { Form } from "components/input";
import { Select } from "components/input";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";
import * as Systems from "components/systems";

const dataTest = {
  "items": [
    {
      "id": 1000010000,
      "channelId": 101,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "mclient.local",
      "groupCount": 0,
      "modified": "Feb 19, 2025, 2:16:53 PM",
      "channelLabels": "SLE-Micro-5.5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 2:31:23 PM",
      "created": "Feb 19, 2025, 2:16:53 PM",
      "locked": 0,
      "name": "mclient.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled"
      ],
      "entitlementLevel": "Salt",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": true,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010002,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010003,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010004,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010005,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010006,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010007,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010008,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010009,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010010,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010011,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010012,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010013,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010014,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
    {
      "id": 1000010015,
      "channelId": 104,
      "securityErrata": 0,
      "bugErrata": 0,
      "enhancementErrata": 0,
      "outdatedPackages": 0,
      "configFilesWithDifferences": 0,
      "serverName": "client-15sp5.local",
      "groupCount": 0,
      "modified": "Feb 20, 2025, 12:37:30 PM",
      "channelLabels": "SLE-Product-SLES15-SP5-Pool for aarch64",
      "creatorName": "admin",
      "lastCheckin": "Feb 28, 2025, 8:00:02 PM",
      "created": "Feb 20, 2025, 12:05:38 PM",
      "locked": 0,
      "name": "client-15sp5.local",
      "mgrServer": false,
      "proxy": false,
      "entitlement": [
        "salt_entitled",
        "ansible_control_node"
      ],
      "entitlementLevel": "Salt, Ansible Control Node",
      "selectable": true,
      "isVirtualHost": false,
      "isVirtualGuest": false,
      "extraPkgCount": 0,
      "statusType": "up2date",
      "requiresReboot": false,
      "kickstarting": false,
      "actionsCount": 0,
      "packageActionsCount": 0,
      "unscheduledErrataCount": 0,
      "selected": false,
      "disabled": false
    },
  ],
  "total": 19,
  "selectedIds": []
}

type Props = {
  state: any,
  onChange: Function,
  errors: any
};

const options = [
  { value: "Activation KeyAdmin", label: "ActivationKeyAdmin" },
  { value: "Image Administrator", label: "ImageAdministrator" },
  { value: "Configuration Administrator", label: "ConfigurationAdministrator" },
  { value: "Channel Administrator", label: "ChannelAdministrator" },
  { value: "System Group Administrator", label: "SystemGroupAdministrator" },
  { value: "KeyAdmin", label: "KeyAdmin" },
  { value: "Image and channel", label: "ImageChannelAdmi" },
  { value: "Configurations", label: "ConfigurationAdmin" },
  { value: "Channel readonly", label: "ChannelAReadOnly" },
  { value: "SystemModify", label: "SystemModify" },
];


const AccessGroupPermissions = (props: Props) => {
  const [systemsData, setSystemsData] = useState(dataTest);

  return (
    <div>
      <div className="row">
        <div className="col-md-4 d-flex">
          <strong className="me-3">Name:</strong>
          <div>{props.state.detailsproperties.name}</div>
        </div>
        <div className="col-md-8 d-flex">
          <strong className="me-3">Description:</strong>
          <div>{props.state.detailsproperties.description}</div>
        </div>
      </div>
      <hr></hr>
      <p>Inherit permissions from predefined groups or customize your access group by adding specific namespaces and permissions to meet your unique requirements.</p>

      <Form
        model={props.state.accessGroupsModel}
        onChange={(modelNew) => {
          props.onChange(modelNew);
        }}
        divClass="col-md-12 mt-3"
        formDirection="form-horizontal"
      >
        <Select
          name="accessGroup"
          label={t("Assign Access Groups")}
          options={options}
          placeholder={t("Search for existing access groups...")}
          emptyText={t("No Access group")}
          labelClass="col-md-12 text-start fw-bold fs-4 mb-3"
          divClass="col-md-6"
          isMulti
        />
      </Form>
      <hr></hr>
      <h3 className="mt-5">Namespaces and Permissions</h3>

      <Table
        data={systemsData.items}
        identifier={(item) => item.id}
        initialSortColumnKey="server_name"
        selectable={(item) => item.hasOwnProperty("id")}
        emptyText={t("No Systems.")}
      >
        <Column
          columnKey="server_name"
          comparator={Utils.sortByText}
          header={t("System")}
          cell={(item) => Systems.iconAndName(item)}
        />
        <Column
          columnKey="status_type"
          comparator={Utils.sortByText}
          header={t("Updates")}
          cell={(item) => {
            if (item.statusType == null) {
              return "";
            }
            return Systems.statusDisplay(item, props.isAdmin);
          }}
        />
        <Column
          columnKey="totalErrataCount"
          comparator={Utils.sortByText}
          header={t("Patches")}
          cell={(item) => {
            let totalErrataCount = item.securityErrata + item.bugErrata + item.enhancementErrata;
            if (totalErrataCount !== 0) {
              return <a href={`/rhn/systems/details/ErrataList.do?sid=${item.id}`}>{totalErrataCount}</a>;
            }
            return totalErrataCount;
          }}
        />

        <Column
          columnKey="outdated_packages"
          comparator={Utils.sortByText}
          header={t("Packages")}
          cell={(item) => {
            if (item.outdatedPackages !== 0) {
              return (
                <a href={`/rhn/systems/details/packages/UpgradableList.do?sid=${item.id}`}>{item.outdatedPackages}</a>
              );
            }
            return item.outdatedPackages;
          }}
        />

        <Column
          columnKey="extra_pkg_count"
          comparator={Utils.sortByText}
          header={t("Extra Packages")}
          cell={(item) => {
            if (item.extraPkgCount !== 0) {
              return (
                <a href={`/rhn/systems/details/packages/ExtraPackagesList.do?sid=${item.id}`}>{item.extraPkgCount}</a>
              );
            }
            return item.extraPkgCount;
          }}
        />

        <Column
          columnKey="config_files_with_differences"
          comparator={Utils.sortByText}
          header={t("Config Diffs")}
          cell={(item) => {
            if (item.configFilesWithDifferences !== 0) {
              return (
                <a href={`/rhn/systems/details/configuration/Overview.do?sid=${item.id}`}>
                  {item.configFilesWithDifferences}
                </a>
              );
            }
            return 0;
          }}
        />
        <Column
          columnKey="channel_labels"
          comparator={Utils.sortByText}
          header={t("Base Channel")}
          cell={(item) => {
            if (item.channelId != null) {
              return <a href={`/rhn/channels/ChannelDetail.do?cid=${item.channelId}`}>{item.channelLabels}</a>;
            }
            return item.channelLabels;
          }}
        />
        <Column
          columnKey="entitlement_level"
          comparator={Utils.sortByText}
          header={t("System Type")}
          cell={(item) => item.entitlementLevel}
        />
      </Table>
    </div >
  );
};

export default AccessGroupPermissions;
