import * as React from "react";
import _isNil from "lodash/isNil";
import { DangerDialog } from "components/dialog/DangerDialog";
import { Form } from "components/input/Form";
import { Select } from "components/input/Select";

type Props = {
  /** The modal dialog id */
  id: string;
  /** Virtual Machine descriptor as the one used in the VMs list data */
  vm: any;
  onConfirm: (vm: any, target: string) => any;
  onClose?: () => any;
  clusterNodes?: string[];
};

/**
 * A pop-up dialog for virtual machine migration
 * It contains controls to select the target virtualization host,
 * a 'Cancel' button and a 'Migrate' button.
 * Related virtual machine descriptor object may be passed with the 'vm' property.
 * This 'vm' will be passed to the 'onConfirm' and 'onClosePopUp' handlers.
 */
export function MigrateDialog(props: Props) {
  const [model, setModel] = React.useState<{ target: string | undefined }>({ target: undefined });
  const [valid, setValid] = React.useState(false);
  const onSubmit = () => {
    if (!_isNil(model.target)) {
      props.onConfirm(props.vm, model.target);
    }
  };

  const vmName = props.vm != null ? props.vm.name : undefined;
  return (
    <DangerDialog
      isOpen={!_isNil(props.vm)}
      id={props.id}
      title={t(`Migrate Guest ${vmName}`)}
      submitIcon="fa-share-square-o"
      submitText={t("Migrate")}
      onConfirm={valid ? onSubmit : undefined}
      onClose={() => {
        setModel({ target: undefined });
        if (props.onClose) {
          props.onClose();
        }
      }}
      btnClass="btn-success"
      content={
        <Form
          model={model}
          onChange={(newModel) => setModel(newModel)}
          divClass="col-md-12"
          className="col-md-12"
          formDirection="form-horizontal"
          onValidate={(valid) => setValid(valid)}
        >
          <p>{t("Select where to migrate the virtual guest to.")}</p>
          <Select
            name="target"
            label={t("Target Host")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
            options={props.clusterNodes || []}
          />
        </Form>
      }
    />
  );
}
