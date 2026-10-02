import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import CreatorPanel from "./CreatorPanel";

type CreatorPanelProps = React.ComponentProps<typeof CreatorPanel>;

const renderContent: CreatorPanelProps["renderContent"] = () => (
  <div>
    <p>Create and maintain reusable credentials from this panel.</p>
    <ul>
      <li>Production credential</li>
      <li>Staging credential</li>
    </ul>
  </div>
);

const renderCreationContent: CreatorPanelProps["renderCreationContent"] = ({ item, setItem, errors }) => (
  <div className="form-group">
    <label htmlFor="creator-panel-name">Credential name</label>
    <input
      id="creator-panel-name"
      className="form-control"
      value={item.name ?? ""}
      onChange={(event) => setItem({ ...item, name: event.target.value })}
    />
    {errors ? <div className="help-block">{String(errors)}</div> : null}
  </div>
);

const meta = {
  title: "Components/Panels/CreatorPanel",
  component: CreatorPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Panel pattern for listing content and opening a controlled creation or editing dialog with save, cancel, and optional delete actions.",
      },
    },
  },
  args: {
    id: "credential-creator",
    title: "Credentials",
    creatingText: "Create credential",
    panelLevel: "2",
    icon: "fa-plus",
    renderContent,
    renderCreationContent,
    onOpen: ({ setItem }) => setItem({ name: "" }),
    onSave: ({ item, closeDialog }) => {
      action("saved")(item);
      closeDialog();
    },
    onCancel: action("cancelled"),
    onDelete: ({ item, closeDialog }) => {
      action("deleted")(item);
      closeDialog();
    },
    disableEditing: false,
    disableDelete: false,
    disableOperations: false,
    collapsible: false,
  },
  argTypes: {
    id: { control: "text", description: "Stable prefix for the panel, trigger, dialog, and action IDs." },
    title: { control: "text", description: "Panel and dialog title." },
    creatingText: { control: "text", description: "Text displayed by the creation trigger." },
    panelLevel: { control: "select", options: ["1", "2", "3", "4"], description: "Panel heading level." },
    icon: { control: "text", description: "Font Awesome icon used by the creation trigger." },
    renderContent: { control: false, description: "Renders the panel body." },
    renderCreationContent: { control: false, description: "Renders the dialog body and receives editing state." },
    onOpen: { control: false, description: "Initializes item and error state before opening." },
    onSave: { control: false, description: "Saves the current item and may close the dialog." },
    onCancel: { action: "cancelled", description: "Called before the dialog closes without saving." },
    onDelete: { control: false, description: "Deletes the current item and may close the dialog." },
    disableEditing: { control: "boolean", description: "Hides all creation and editing controls." },
    disableDelete: { control: "boolean", description: "Disables the delete action." },
    disableOperations: { control: "boolean", description: "Disables save and delete actions." },
    collapsible: { control: "boolean", description: "Makes the panel body collapsible." },
  },
} satisfies Meta<typeof CreatorPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const ReadOnly: Story = {
  args: {
    disableEditing: true,
  },
};
