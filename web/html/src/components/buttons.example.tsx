import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

import { Text } from "./input/text/Text";
import { BootstrapPanel } from "./panels/BootstrapPanel";
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
      <h2>Buttons</h2>
      <p>Different button styling variants tailored for various scenarios as required.</p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" text="Primary" />
          <Button className="btn-default" text="Default" />
          <Button className="btn-danger" text="Danger" />
          <Button className="btn-tertiary" text="Tertiary" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" text="Primary" /&gt; <br />
        &lt;Button className="btn-default" text="Default" /&gt;
        <br />
        &lt;Button className="btn-danger" text="Danger" /&gt;
        <br />
        &lt;Button className="btn-tertiary" text="Tertiary" /&gt;
      </StorySection>

      <h2>Variants</h2>
      <p>
        <strong>Note: </strong>Icon-only buttons should always include a descriptive <code>title / Tooltip</code>{" "}
        attribute to ensure clarity and accessibility for all users.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" title="Add" icon="fa-plus" />
          <Button className="btn-default" title="Delete" icon="fa-trash" />
          <Button className="btn-primary" icon="fa-plus" text="Primary" />
          <Button className="btn-default" icon="fa-plus" text="Default" />
          <Button className="btn-tertiary" icon="fa-plus" text="Tertiary" />
          <Button className="btn-tertiary" title="Delete" icon="fa-trash" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" title="Add" icon="fa-plus" /&gt; <br />
        &lt;Button className="btn-default" title="Delete" icon="fa-trash" /&gt;
        <br />
        &lt;Button className="btn-primary" icon="fa-plus" text="Primary" /&gt;
        <br />
        &lt;Button className="btn-default" icon="fa-plus" text="Default" /&gt;
        <br />
        &lt;Button className="btn-tertiary" icon="fa-plus" text="Tertiary" /&gt;
        <br />
        &lt;Button className="btn-tertiary" title="Delete" icon="fa-trash" /&gt;
      </StorySection>

      <h2>Sizes</h2>
      <p>
        We support two button sizes: default and small. Use <code>btn-sm</code> for the small button size.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary btn-sm" text="Small button" />
          <Button className="btn-default btn-sm" text="Small button" />
          <Button className="btn-danger btn-sm" text="Small button" />
          <Button className="btn-default btn-sm" title="Delete" icon="fa-trash" />
          <Button className="btn-primary btn-sm" title="Add" icon="fa-plus" />
          <Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash" />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary btn-sm" text="Small button" /&gt; <br />
        &lt;Button className="btn-default btn-sm" text="Small button" /&gt;
        <br />
        &lt;Button className="btn-danger btn-sm" text="Small button" /&gt;
        <br />
        &lt;Button className="btn-default btn-sm" title="Delete" icon="fa-trash" /&gt;
        <br />
        &lt;Button className="btn-primary btn-sm" title="Add" icon="fa-plus" /&gt;
        <br />
        &lt;Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash" /&gt;
        <br />
      </StorySection>

      <h2>Disabled</h2>
      <p>
        Make buttons look inactive by adding the disabled boolean attribute to any <code>Button</code> element.
      </p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" text="Primary" disabled />
          <Button className="btn-default" text="Default" disabled />
          <Button className="btn-danger" text="Danger" disabled />
          <Button className="btn-tertiary" text="Tertiary" disabled />
        </StoryRow>
      </StripedStorySection>
      <StorySection>
        &lt;Button className="btn-primary" text="Primary" disabled /&gt; <br />
        &lt;Button className="btn-default" text="Default" disabled /&gt;
        <br />
        &lt;Button className="btn-danger" text="Danger" disabled /&gt;
        <br />
        &lt;Button className="btn-tertiary" text="Tertiary" disabled /&gt;
        <br />
      </StorySection>
      <h2>Choosing a button</h2>
      <p>
        Every button type has a specific role. Select the appropriate button type based on the screen's context to
        ensure a clear and seamless user experience.
      </p>
      <table className="table table-striped mt-3">
        <thead>
          <tr>
            <th>Variant</th>
            <th>Use Cases</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <strong>Primary</strong>
            </td>
            <td>
              Calls to action (CTAs), key actions, form submissions.
              <p>
                <strong> Example:</strong> “Add”, “Create” or "Submit"
              </p>
            </td>
          </tr>
          <tr>
            <td>
              <strong>Default</strong>{" "}
            </td>
            <td>
              Less important actions, alternative options, supporting information and the most commonly used option.
              <p>
                <strong> Example:</strong>"Edit", "Download" or "Cancel"
              </p>
            </td>
          </tr>
          <tr>
            <td>
              <strong>Danger</strong>{" "}
            </td>
            <td>
              Potentially irreversible actions, deletions, cancellations.
              <p>
                <strong> Example:</strong> Deleting any system or data.
              </p>
            </td>
          </tr>
          <tr>
            <td>
              <strong>Only Icon Button</strong>{" "}
            </td>
            <td>
              Only icon buttons are ideal for presenting actions in compact layouts.
              <p>
                <strong>Example:</strong> Table, Cards.
              </p>
              <p>They can be used individually or grouped together.</p>
              <p>
                To enhance clarity, always pair icon buttons with title attribute to give users a brief explanation of
                their function.
              </p>
            </td>
          </tr>
          <tr>
            <td>
              <strong>Tertiary</strong>{" "}
            </td>
            <td>
              <p>
                Tertiary buttons are designed for less prominent actions and should not compete visually with Primary or
                Default buttons.{" "}
              </p>
              <p>Use icon-only tertiary buttons in cards, forms, inline action or components where space is limited.</p>
              <p>
                <strong>Example:</strong> Inline action buttons for editing an email address in a form.
              </p>
            </td>
          </tr>
        </tbody>
      </table>
      <hr></hr>
      <h2 className="mt-5">Best Practices to Write Useful Button Labels</h2>
      <ul className="mt-0">
        <li>Button labels should typically range from 1 to 3 words to maintain clarity and make them easy to scan.</li>
        <li>Ensure labels are action-oriented and provide clear guidance on what the button does.</li>
        <li>
          Avoid Repetition in Bulk Actions: In lists or bulk-action scenarios, avoid redundant labels for buttons acting
          on "selected items."
          <br />
        </li>
      </ul>
      <hr></hr>
      <h2 className="mt-5">Placement</h2>
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
              <label className="control-label col-md-3" aria-hidden></label>
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
