import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { ModalEditButton } from "./ModalEditButton";

const meta = {
  title: "Components/Dialogs/ModalEditButton",
  component: ModalEditButton,
  parameters: {
    docs: {
      description: {
        component:
          "A button that opens a modal dialog for editing a single text value. The modal includes a text field with validation and Save/Cancel buttons. Commonly used for inline editing of names, labels, or other short text values.",
      },
    },
  },
  args: {
    buttonLabel: "Edit",
    modalTitle: "Edit Value",
    fieldLabel: "Value",
    value: "Current Value",
    disabled: false,
  },
  argTypes: {
    buttonLabel: {
      control: "text",
      description: "Text displayed on the trigger button.",
      table: { type: { summary: "string" } },
    },
    buttonIcon: {
      control: "text",
      description: "Optional Font Awesome icon class for the button (e.g., 'fa-edit', 'fa-pencil').",
      table: { type: { summary: "string" } },
    },
    modalTitle: {
      control: "text",
      description: "Title displayed in the modal dialog header.",
      table: { type: { summary: "string" } },
    },
    fieldLabel: {
      control: "text",
      description: "Label for the text input field inside the modal.",
      table: { type: { summary: "string" } },
    },
    placeholder: {
      control: "text",
      description: "Placeholder text for the input field.",
      table: { type: { summary: "string" } },
    },
    value: {
      control: "text",
      description: "Initial value shown in the input field when the modal opens.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the button and prevents opening the modal.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    validators: {
      control: false,
      description:
        "Array of validation functions. Each receives the value and returns true for valid, false for invalid.",
      table: { type: { summary: "Validator | Validator[]" } },
    },
    invalidHint: {
      control: "text",
      description: "Error message displayed when validation fails.",
      table: { type: { summary: "ReactNode" } },
    },
    onSave: {
      action: "value saved",
      description: "Callback invoked when user clicks Save. Receives the new value as parameter.",
      table: { type: { summary: "(value: string) => void | Promise<void>" } },
    },
  },
} satisfies Meta<typeof ModalEditButton>;

export default meta;

type Story = StoryObj<typeof meta>;

const PlaygroundComponent = (args: React.ComponentProps<typeof ModalEditButton>) => {
  const [currentValue, setCurrentValue] = useState(args.value);

  useEffect(() => {
    setCurrentValue(args.value);
  }, [args.value]);

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "15px" }}>
        <strong>Current value:</strong> {currentValue}
      </div>
      <ModalEditButton
        {...args}
        value={currentValue}
        onSave={(newValue) => {
          setCurrentValue(newValue);
          action("value saved")(newValue);
        }}
      />
    </div>
  );
};

export const Playground: Story = {
  render: (args) => <PlaygroundComponent {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive ModalEditButton. Click to open the modal, edit the value, and save to see the change reflected.",
      },
    },
  },
};

const EditNameComponent = () => {
  const [name, setName] = useState("John Doe");

  return (
    <div style={{ padding: "20px" }}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">User Profile</h3>
        </div>
        <div className="panel-body">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <div>
              <strong>Name:</strong> {name}
            </div>
            <ModalEditButton
              buttonLabel="Edit Name"
              buttonIcon="fa-edit"
              modalTitle="Edit User Name"
              fieldLabel="Full Name"
              placeholder="Enter full name"
              value={name}
              onSave={(newValue) => {
                setName(newValue);
                action("name changed")(newValue);
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export const EditName: Story = {
  render: () => <EditNameComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Using ModalEditButton to edit a user's name in a profile card.",
      },
    },
  },
};

const WithValidationComponent = () => {
  const [email, setEmail] = useState("user@example.com");

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "15px" }}>
        <strong>Email:</strong> {email}
      </div>
      <ModalEditButton
        buttonLabel="Change Email"
        buttonIcon="fa-envelope"
        modalTitle="Update Email Address"
        fieldLabel="Email"
        placeholder="user@example.com"
        value={email}
        validators={(value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)}
        invalidHint="Please enter a valid email address"
        onSave={(newValue) => {
          setEmail(newValue);
          action("email updated")(newValue);
        }}
      />
    </div>
  );
};

export const WithValidation: Story = {
  render: () => <WithValidationComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "ModalEditButton with email validation. The Save button is disabled until a valid email is entered.",
      },
    },
  },
};

const MultipleValidatorsComponent = () => {
  const [apiKey, setApiKey] = useState("abc123xyz789");

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "15px" }}>
        <strong>API Key:</strong> <code>{apiKey}</code>
      </div>
      <ModalEditButton
        buttonLabel="Regenerate"
        buttonIcon="fa-key"
        modalTitle="Set New API Key"
        fieldLabel="API Key"
        placeholder="Enter API key"
        value={apiKey}
        validators={[(value: string) => value.length >= 12, (value: string) => /^[a-zA-Z0-9]+$/.test(value)]}
        invalidHint="API key must be at least 12 alphanumeric characters"
        onSave={(newValue) => {
          setApiKey(newValue);
          action("api key updated")(newValue);
        }}
      />
    </div>
  );
};

export const MultipleValidators: Story = {
  render: () => <MultipleValidatorsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "ModalEditButton with multiple validators: minimum length and alphanumeric characters only.",
      },
    },
  },
};

export const Disabled: Story = {
  render: (args) => (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "15px" }}>
        <strong>Locked Value:</strong> Cannot be edited
      </div>
      <ModalEditButton {...args} />
    </div>
  ),
  args: {
    buttonLabel: "Edit (Locked)",
    modalTitle: "Edit Value",
    fieldLabel: "Value",
    value: "Protected Value",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled ModalEditButton. The modal cannot be opened.",
      },
    },
  },
};

const InlineEditingComponent = () => {
  const [items, setItems] = useState([
    { id: 1, name: "Server Alpha", description: "Production server" },
    { id: 2, name: "Server Beta", description: "Development server" },
    { id: 3, name: "Server Gamma", description: "Testing server" },
  ]);

  const updateItemName = (id: number, newName: string) => {
    setItems(items.map((item) => (item.id === id ? { ...item, name: newName } : item)));
    action("item renamed")({ id, newName });
  };

  const updateItemDescription = (id: number, newDescription: string) => {
    setItems(items.map((item) => (item.id === id ? { ...item, description: newDescription } : item)));
    action("description updated")({ id, newDescription });
  };

  return (
    <div style={{ padding: "20px" }}>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.name}</td>
              <td>{item.description}</td>
              <td>
                <div style={{ display: "flex", gap: "5px" }}>
                  <ModalEditButton
                    buttonLabel=""
                    buttonIcon="fa-edit"
                    modalTitle={`Edit ${item.name}`}
                    fieldLabel="Server Name"
                    value={item.name}
                    onSave={(newValue) => updateItemName(item.id, newValue)}
                  />
                  <ModalEditButton
                    buttonLabel=""
                    buttonIcon="fa-pencil"
                    modalTitle="Edit Description"
                    fieldLabel="Description"
                    value={item.description}
                    onSave={(newValue) => updateItemDescription(item.id, newValue)}
                  />
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export const InlineEditing: Story = {
  render: () => <InlineEditingComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Using ModalEditButton for inline editing in a table. Each row has edit buttons for name and description.",
      },
    },
  },
};
