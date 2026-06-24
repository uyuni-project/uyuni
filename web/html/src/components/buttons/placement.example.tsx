import { Button } from "components/buttons";

import { Text } from "../input/text/Text";
import { BootstrapPanel } from "../panels/BootstrapPanel";

export default () => {
  const listButtons = [
    <div className="form-group btn-group" key="modal-buttons-eg1">
      <div className="col-lg-offset-3 offset-lg-3 col-lg-6">
        <Button className="btn-primary bt-sm" text="Submit" />
        <Button className="btn-default bt-sm" text="Clear" />
      </div>
    </div>,
  ];
  const modalButtonsConfirm = [
    <div className="d-flex justify-content-end" key="modal-buttons-eg2">
      <Button className="btn-default me-2" text="Cancel" />
      <Button className="btn-primary" text="Finish" />
    </div>,
  ];
  const modalButtonsDelete = [
    <div className="d-flex justify-content-end" key="modal-buttons-eg3">
      <Button className="btn-default me-2" text="Cancel" />
      <Button className="btn-danger" text="Delete" />
    </div>,
  ];
  return (
    <div>
      <h2>Placement</h2>
      <div className="row">
        <div className="col-md-6">
          <p>
            When grouping buttons aligned to the left, place the primary button at the far left, followed by the button
            for the next most important action.
          </p>
          <p>
            <strong>Example:</strong> Form
          </p>
          <BootstrapPanel title="Forms" footer={listButtons}>
            <Text name="firstname" label={t("First Name")} labelClass="col-md-3 text-right" divClass="col-md-6" />
            <Text name="lastname" label={t("Last Name")} labelClass="col-md-3 text-right" divClass="col-md-6" />
            <div className="form-group">
              <label className="control-label col-md-3 text-right" htmlFor="email-id">
                Email ID:
              </label>
              <div className="col-md-6">
                user@suse.com{" "}
                <Button className="btn-tertiary" title="Edit Email ID" icon="fa-pencil" id="email-id"></Button>
              </div>
            </div>
          </BootstrapPanel>
        </div>
      </div>
      <div className="row mt-5">
        <div className="col-md-6">
          <p>
            When buttons in a group are aligned to the right, position the Primary button at the end, with the Default
            action button placed next to it.
          </p>
          <p>
            <strong>Example:</strong> Modals, Action Bars, Multi-step interfaces
          </p>
          <BootstrapPanel title="Confirmation Modal" footer={modalButtonsConfirm}>
            <p>
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum commodo massa et tellus tempor, at
              faucibus tortor volutpat.
            </p>
          </BootstrapPanel>
        </div>
      </div>
      <div className="row mt-5">
        <div className="col-md-6">
          <BootstrapPanel title="Remove Selected item" footer={modalButtonsDelete}>
            <p>Are you sure you want to remove the selected items? (3 items selected)?</p>
          </BootstrapPanel>
        </div>
      </div>
      <div className="row mt-5">
        <div className="col-md-6">
          <h5>Tertiary buttons Examples:</h5>
          <BootstrapPanel
            title="Forms"
            footer={
              <div className="form-group btn-group">
                <div className="col-lg-offset-3 offset-lg-3 col-lg-6">
                  <Button className="btn-primary bt-sm" text="Submit" />
                </div>
              </div>
            }
          >
            <Text name="firstname" label={t("Name")} labelClass="col-md-3 text-right" divClass="col-md-6" />
            <div className="form-group ">
              <label className="control-label col-md-3 text-right" htmlFor="firstname">
                Name:
              </label>
              <div className="col-md-6">
                <input className="form-control" name="firstname" />
              </div>
              <div className="col-md-3 ps-0">
                <Button className="btn-tertiary" title="Delete" icon="fa-trash"></Button>
              </div>
            </div>
            <div className="form-group ">
              <div className="control-label col-md-3" />
              <div className="col-md-6">
                <Button className="btn-tertiary" icon="fa-plus">
                  Add Name
                </Button>
              </div>
            </div>
          </BootstrapPanel>
        </div>
      </div>
    </div>
  );
};
