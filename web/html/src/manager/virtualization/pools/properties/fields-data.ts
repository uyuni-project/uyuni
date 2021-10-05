import * as data from "utils/data";

const mapping = {
  dir: {
    description: "Manages files in a directory on the virtual host",
    panels: ["target"],
    target_path: {
      hint: "Absolute path where the pool will be mounted",
      required: true,
    },
  },
  fs: {
    description: "Manages files in a block device to be mounted",
    panels: ["source", "target"],
    source_devices: {
      show: true,
      label: "Device path",
      required: true,
      list: false,
    },
    target_path: {
      hint: "Absolute path where the pool will be mounted",
      required: true,
    },
  },
  netfs: {
    description: "Manages files in a network file system to be mounted",
    panels: ["source", "target"],
    source_hosts: {
      show: true,
      required: true,
    },
    source_dir: {
      show: true,
      required: true,
      label: {
        auto: "Directory",
        nfs: "Directory",
        glusterfs: "Volume name",
        cifs: "CIFS share",
      },
    },
    target_path: {
      hint: "Absolute path where the pool will be mounted",
      required: true,
    },
  },
  disk: {
    description: "Manages volumes as partitions on a physical disk",
    panels: ["source", "target"],
    source_devices: {
      show: true,
      label: "Device path",
      list: false,
      required: true,
      allow_part: true,
    },
    target_path: {
      hint: "Absolute path to the directory containing the devices nodes",
      required: true,
    },
  },
  iscsi: {
    description: "Manages pre-allocated volumes on a iSCSI server using iscsiadm",
    panels: ["source", "target"],
    source_hosts: {
      show: true,
      required: true,
    },
    source_devices: {
      show: true,
      list: false,
      required: true,
      label: "iSCSI Qualified Name",
    },
    source_initiator: {
      show: true,
    },
    has_auth: true,
    target_path: {
      hint: "Absolute path to the directory containing the devices nodes",
      required: true,
    },
  },
  logical: {
    description: "Manages volumes in a LVM Volume Group.",
    panels: ["source"],
    source_devices: {
      show: true,
      list: true,
      min: 0,
    },
    source_name: {
      show: true,
    },
  },
  scsi: {
    description: "Manages pre-existing LUNs on a SCSI HBA",
    panels: ["source", "target"],
    source_adapter: {
      scsi_host: {
        selection_mode: true,
        selection_title: "Select device by",
        default_selection: "name",
        selection: {
          name: "Name",
          parent_address: "Parent PCI Address",
        },
        name: ["name"],
        parent_address: ["parent_address", "parent_address_uid"],
      },
      fc_host: {
        fields: ["wwnn", "wwpn", "managed"],
        parent_selection: true,
        selection_title: "Parent vHBA selection mode",
        default_selection: "automatic",
        selection: {
          automatic: "Detect parent scsi_host",
          name: "Parent scsi_host name",
          wwnn_wwpn: "Parent WWNN/WWPN",
          fabric_wwn: "Parent Fabric WWN",
        },
        name: ["parent"],
        wwnn_wwpn: ["parent_wwnn", "parent_wwpn"],
        fabric_wwn: ["parent_fabric_wwn"],
      },
    },
    target_path: {
      hint: "Absolute path to the directory containing the devices nodes",
      required: true,
    },
  },
  mpath: {
    description: "Pool containing all the multipath devices on the host",
  },
  rbd: {
    description: "Pool containing all RBD images in a RADOS pool",
    panels: ["source"],
    source_hosts: {
      show: true,
      list: true,
      required: true,
    },
    source_name: {
      show: true,
      required: true,
    },
    has_auth: true,
  },
  sheepdog: {
    description: "Pool based on an already formatted Sheepdog cluster",
    panels: ["source"],
    source_hosts: {
      show: true,
    },
    source_name: {
      show: true,
      required: true,
    },
  },
  gluster: {
    description: "Pool based on a Gluster volume using native access",
    panels: ["source"],
    source_hosts: {
      show: true,
      required: true,
    },
    source_dir: {
      show: true,
      label: {
        auto: "Subdirectory",
      },
    },
    source_name: {
      show: true,
      required: true,
    },
  },
  zfs: {
    description: "Pool based on the ZFS filesystem",
    panels: ["source"],
    source_devices: {
      show: true,
      list: false,
      required: false,
      label: "Device path",
    },
    source_name: {
      show: true,
      required: true,
    },
  },
  "iscsi-direct": {
    description: "Manages pre-allocated volumes on a iSCSI server using libiscsi",
    panels: ["source"],
    source_hosts: {
      show: true,
      required: true,
    },
    source_devices: {
      show: true,
      list: false,
      required: true,
      label: "iSCSI Qualified Name",
    },
    source_initiator: {
      show: true,
      required: true,
    },
    has_auth: true,
  },
};

export function getValue(type, path, defaultValue) {
  return data.getValue(mapping[type], path, defaultValue);
}

export function computeSourceAdapterSelection(model: any) {
  const adapter_type = model.source_adapter_type || "scsi_host";
  const selection_types = Object.keys(getValue("scsi", `source_adapter.${adapter_type}.selection`, {}));
  const possible_selections = selection_types
    .filter((selection) => {
      const fields = getValue("scsi", `source_adapter.${adapter_type}.${selection}`, []);
      return fields.some((field) => {
        // CamelCase to get the property name
        const name = field
          .split("_")
          .map((item, idx) => (idx > 0 ? `${item.substring(0, 1).toUpperCase()}${item.substring(1)}` : item))
          .join("");
        const property_name = `source_adapter_${name}`;
        return model[property_name] != null;
      });
    })
    .filter((selection) => selection !== "automatic");

  return possible_selections.length > 0 ? possible_selections[0] : undefined;
}
