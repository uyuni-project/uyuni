import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";
import { Form } from "./input/form/Form";
import { Text } from "./input/text/Text";
import { BootstrapPanel } from "./panels/BootstrapPanel";

export default () => {
  const listButtons = [
    <div className="form-group btn-group">
      <div className="col-lg-offset-3 offset-lg-3 col-lg-6">
        <Button
          className="btn-primary"
          text="Submit"
        />
        <Button
          className="btn-default"
          text="Clear"
        />
      </div>
    </div>
  ];
  const modalButtonsConfirm = [
    <div className="d-flex justify-content-end">
      <Button
        className="btn-default me-2"
        text="Cancel"
      />
      <Button
        className="btn-primary"
        text="Finish"
      />
    </div >
  ];
  const modalButtonsDelete = [
    <div className="d-flex justify-content-end">
      <Button
        className="btn-default me-2"
        text="Cancel"
      />
      <Button
        className="btn-danger"
        text="Delete"
      />
    </div >
  ];
  return (
    <div>
      <h2>Variants</h2>
      <p>Different button styling variants tailored for various scenarios as required.</p>
      <StripedStorySection>
        <div className="row">
          <div className="col-md-1">
            <h5>Primary</h5>
            <Button className="btn-primary">Button</Button>
            <h5 className="mt-5">Small</h5>
            <Button className="btn-primary btn-sm">Button</Button>
          </div>
          <div className="col-md-1">
            <h5>Secondary</h5>
            <Button className="btn-default">Button</Button>
            <h5 className="mt-5">Small</h5>
            <Button className="btn-default btn-sm">Button</Button>
          </div>
          <div className="col-md-1">
            <h5>Danger</h5>
            <Button className="btn-danger">Danger</Button>
            <h5 className="mt-5">Small</h5>
            <Button className="btn-danger btn-sm">Button</Button>
          </div>
        </div>
        <h5 className="mt-5">Only icon button and button with icon and label</h5>
        <StoryRow>
          <Button title="Add" className="btn-default" icon="fa-plus"></Button>
          <Button title="Delete" className="btn-default" icon="fa-trash"></Button>
          <Button className="btn-primary" icon="fa-plus">Primary</Button>
        </StoryRow>
        <div className="row mt-5">
          <div className="col-md-12">
            <h5>Unstyled</h5>
            <Button title="Edit" className="btn-unstyled" icon="fa-edit"></Button>
          </div>
        </div>
      </StripedStorySection>
      <hr></hr>
      <h2>Choosing a button</h2>
      <p>Every button type has a specific role. Select the appropriate button type based on the screen's context to ensure a clear and seamless user experience.</p>
      <table className="table table-striped mt-3">
        <thead>
          <tr>
            <th>Variant</th>
            <th>Use Cases</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td><strong>Primary</strong></td>
            <td>Calls to action (CTAs), key actions, form submissions.
              <p><strong> Example:</strong> “Add”, “Create” or "Submit"</p>
            </td>
          </tr>
          <tr>
            <td><strong>Secondary</strong>  </td>
            <td>Less important actions, alternative options, supporting information and the most commonly used option.
              <p><strong> Example:</strong>"Edit", "Download" or "Cancel"</p>
            </td>
          </tr>
          <tr>
            <td><strong>Danger</strong>  </td>
            <td>Potentially irreversible actions, deletions, cancellations.
              <p><strong> Example:</strong> Deleting any system or data.</p>
            </td>
          </tr>
          <tr>
            <td><strong>Only Icon Button</strong>  </td>
            <td>Only icon buttons are ideal for presenting actions in compact layouts.
              <p><strong>Example:</strong> Table, Cards.</p>
              <p>They can be used individually or grouped together.</p>
              <p>To enhance clarity, always pair icon buttons with tooltips to give users a brief explanation of their function.</p>
            </td>
          </tr>
          <tr>
            <td><strong>Unstyled</strong>  </td>
            <td>
              Unstyled icon buttons should only be used in cards, forms, or components where they remain subtle and don't overshadow other buttons.
              <p><strong>Example:</strong> Inline action buttons for editing an email address in a form.</p>
            </td>
          </tr>
        </tbody>
      </table>
      <hr></hr>
      <h2 className="mt-5">Placement</h2>
      <div className="row">
        <div className="col-md-6">
          <p>When grouping buttons aligned to the left, place the primary button at the far left, followed by the button for the next most important action.</p>
          <p><strong>Example:</strong> Form</p>
          <BootstrapPanel title="Forms" footer={listButtons}>
            <Text
              name="firstname"
              label={t("First Name")}
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name="firstname"
              label={t("Last Name")}
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </BootstrapPanel>
        </div>
      </div>
      <div className="row mt-5">
        <div className="col-md-6">
          <p>When buttons in a group are aligned to the right, position the primary button at the end, with the secondary action button placed next to it.</p>
          <p><strong>Example:</strong> Modals, Action Bars, Multi-step interfaces</p>
          <BootstrapPanel title="Confirmation Modal" footer={modalButtonsConfirm}>
            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum commodo massa et tellus tempor, at faucibus tortor volutpat.</p>
          </BootstrapPanel>
        </div>
      </div>
      <div className="row mt-5">
        <div className="col-md-4">
          <BootstrapPanel title="Remove Selected item" footer={modalButtonsDelete}>
            <p>Are you sure you want to remove the selected items? (3 items selected)?</p>
          </BootstrapPanel>
        </div>
      </div>
    </div >
  );
};
