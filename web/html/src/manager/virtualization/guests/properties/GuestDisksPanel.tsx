import * as React from "react";
import { FormMultiInput } from "components/input/FormMultiInput";
import { getOrderedItemsFromModel } from "components/input/FormMultiInput";
import { FormContext } from "components/input/Form";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { GuestDiskFields } from "./GuestDiskFields";

type Props = {
  changeModel: Function;
  pools: Array<any>;
  caps: any;
  poolCaps: any;
};

export function GuestDisksPanel(props: Props) {
  const formContext = React.useContext(FormContext);

  const getDiskPanelIcon = (index: number): string => {
    const device = formContext.model[`disk${index}_device`] || "disk";
    const icons = {
      disk: "fa-hdd-o",
      lun: "fa-hdd-o",
      cdrom: "spacewalk-icon-autoinstallations",
      floppy: "fa-floppy-o",
    };
    return icons[device];
  };

  const getDiskPanelTitle = (index: number): string => {
    const device = formContext.model[`disk${index}_device`] || "disk";
    const similarDisks = [...Array(index).keys()].filter(
      (i) =>
        formContext.model[`disk${i}_bus`] === formContext.model[`disk${index}_bus`] &&
        formContext.model[`disk${i}_device`] === formContext.model[`disk${index}_device`]
    );
    return `${formContext.model[`disk${index}_bus`]} ${device} ${similarDisks.length + 1}`;
  };

  function addDisk(index: number) {
    const busTypes = props.caps ? props.caps.devices.disk.bus : [];

    const preferredBusses = ["virtio", "xen"].filter((type) => busTypes.includes(type));
    const first_pool = props.pools.length > 0 ? props.pools[0].name : "";

    props.changeModel(
      Object.assign(formContext.model, {
        [`disk${index}_editable`]: true,
        [`disk${index}_type`]: "volume",
        [`disk${index}_device`]: "disk",
        [`disk${index}_bus`]: preferredBusses.length > 0 ? preferredBusses[0] : busTypes[0],
        [`disk${index}_source_pool`]: props.pools.find((item) => item.name === "default") ? "default" : first_pool,
        [`disk${index}_source_template`]: undefined,
        [`disk${index}_source_size`]: 10,
      })
    );
  }

  const removeDisk = (index: number): void => {
    props.changeModel(
      Object.entries(formContext.model).reduce((res, entry) => {
        const property = !entry[0].startsWith(`disk${index}_`) ? { [entry[0]]: entry[1] } : undefined;
        return Object.assign(res, property);
      }, {})
    );
  };

  const allDisks = getOrderedItemsFromModel(formContext.model, "disk");
  const onlyHandledDisks = allDisks.every((index) =>
    ["file", "volume"].includes(formContext.model[`disk${index}_type`])
  );
  return (
    <>
      {!onlyHandledDisks && (
        <Messages items={MessagesUtils.warning("At least one unsupported disk: disabling editing.")} />
      )}
      <FormMultiInput
        id="disks"
        title={t("Disks")}
        prefix="disk"
        onAdd={(newIndex) => addDisk(newIndex)}
        onRemove={removeDisk}
        disabled={!onlyHandledDisks}
        panelIcon={getDiskPanelIcon}
        panelTitle={getDiskPanelTitle}
      >
        {(index: number) => (
          <GuestDiskFields
            index={index}
            domainCaps={props.caps}
            pools={props.pools}
            poolCaps={props.poolCaps}
            onlyHandledDisks={onlyHandledDisks}
          />
        )}
      </FormMultiInput>
    </>
  );
}
