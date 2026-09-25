import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form, Text } from "../index";
import { FormMultiInput } from "./FormMultiInput";

type FormMultiInputProps = React.ComponentProps<typeof FormMultiInput>;

const meta = {
  title: "Components/Inputs/FormMultiInput",
  component: FormMultiInput,
  parameters: {
    docs: {
      description: {
        component:
          "Manages dynamic lists of form fields with add/remove functionality. Uses a prefix-based naming scheme (`prefix{index}_fieldname`) to track items in the form model. Each item can be a simple row or nested in a Panel. Ideal for managing lists like remote hosts, email addresses, or any repeating data structures.",
      },
    },
  },
  args: {
    id: "multi-input",
    title: "Items",
    prefix: "item",
    disabled: false,
  },
  argTypes: {
    id: {
      control: "text",
      description: "Unique identifier for the component.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Title displayed in the panel header.",
      table: { type: { summary: "string" } },
    },
    prefix: {
      control: "text",
      description:
        "Prefix for field names. Fields are named `{prefix}{index}_fieldname`. For example, with prefix 'host', fields become 'host0_name', 'host0_port', etc.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the remove buttons. Add button is always enabled.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    panelIcon: {
      control: false,
      description: "Function returning an icon class for each item's panel. If provided, items are wrapped in panels.",
      table: { type: { summary: "(index: number) => string" } },
    },
    panelTitle: {
      control: false,
      description: "Function returning a title for each item's panel. If provided, items are wrapped in panels.",
      table: { type: { summary: "(index: number) => string" } },
    },
    header: {
      control: false,
      description: "Content displayed between the title and the first item.",
      table: { type: { summary: "ReactNode" } },
    },
    rowClass: {
      control: "text",
      description: "CSS class applied to the row containing each item's fields.",
      table: { type: { summary: "string" } },
    },
    panelHeading: {
      control: false,
      description: "HTML tag name for panel headers (e.g., 'h2', 'h3').",
      table: { type: { summary: "keyof JSX.IntrinsicElements" } },
    },
    panelClassName: {
      control: "text",
      description: "CSS class for the wrapping Panel.",
      table: { type: { summary: "string" } },
    },
    onAdd: {
      action: "item added",
      description: "Callback invoked when adding a new item. Receives the new index as parameter.",
      table: { type: { summary: "(index: number) => void" } },
    },
    onRemove: {
      action: "item removed",
      description: "Callback invoked when removing an item. Receives the item's index as parameter.",
      table: { type: { summary: "(index: number) => void" } },
    },
    children: {
      control: false,
      description: "Function that renders the fields for one item. Receives the item's index as parameter.",
      table: { type: { summary: "(index: number) => ReactNode" } },
    },
  },
} satisfies Meta<FormMultiInputProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const SimpleListExample = (args: Partial<FormMultiInputProps>) => {
  const [model, setModel] = useState<Record<string, string>>({});

  const handleAdd = (index: number) => {
    const newFields = {
      [`email${index}_address`]: "",
    };
    setModel({ ...model, ...newFields });
    action("item added")(index);
  };

  const handleRemove = (index: number) => {
    const newModel = Object.entries(model).reduce(
      (acc, [key, value]) => {
        if (!key.startsWith(`email${index}_`)) {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, string>
    );
    setModel(newModel);
    action("item removed")(index);
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel({ ...newModel })}
      onSubmit={() => action("form submitted")(model)}
    >
      <FormMultiInput
        id="email-list"
        title="Email Addresses"
        prefix="email"
        onAdd={handleAdd}
        onRemove={handleRemove}
        disabled={args.disabled}
      >
        {(index) => (
          <Text
            name={`email${index}_address`}
            label="Email"
            placeholder="user@example.com"
            labelClass="col-md-3"
            divClass="col-md-6"
          />
        )}
      </FormMultiInput>
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" />
        </div>
      </div>
    </Form>
  );
};

export const Playground: Story = {
  render: (args) => <SimpleListExample {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive FormMultiInput for managing a list of email addresses. Click + to add items, - to remove them. Submit to see the form model.",
      },
    },
  },
};

const RemoteHostsComponent = () => {
  const [model, setModel] = useState<Record<string, string>>({
    host0_name: "prod-server-01",
    host0_port: "22",
  });

  const handleAdd = (index: number) => {
    setModel({
      ...model,
      [`host${index}_name`]: "",
      [`host${index}_port`]: "22",
    });
  };

  const handleRemove = (index: number) => {
    const newModel = Object.entries(model).reduce(
      (acc, [key, value]) => {
        if (!key.startsWith(`host${index}_`)) {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, string>
    );
    setModel(newModel);
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel({ ...newModel });
        action("form changed")(newModel);
      }}
      onSubmit={() => action("form submitted")(model)}
    >
      <FormMultiInput id="hosts" title="Remote Hosts" prefix="host" onAdd={handleAdd} onRemove={handleRemove}>
        {(index) => (
          <>
            <Text
              name={`host${index}_name`}
              label="Hostname"
              placeholder="server.example.com"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`host${index}_port`}
              label="Port"
              placeholder="22"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        )}
      </FormMultiInput>
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Save Configuration" />
        </div>
      </div>
    </Form>
  );
};

export const RemoteHosts: Story = {
  render: () => <RemoteHostsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Managing a list of remote hosts with hostname and port fields. Starts with one pre-filled host.",
      },
    },
  },
};

const WithPanelsComponent = () => {
  const [model, setModel] = useState<Record<string, string>>({
    db0_name: "production",
    db0_host: "db.example.com",
    db0_port: "5432",
  });

  const handleAdd = (index: number) => {
    setModel({
      ...model,
      [`db${index}_name`]: "",
      [`db${index}_host`]: "",
      [`db${index}_port`]: "5432",
    });
  };

  const handleRemove = (index: number) => {
    const newModel = Object.entries(model).reduce(
      (acc, [key, value]) => {
        if (!key.startsWith(`db${index}_`)) {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, string>
    );
    setModel(newModel);
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel({ ...newModel })}
      onSubmit={() => action("form submitted")(model)}
    >
      <FormMultiInput
        id="databases"
        title="Database Connections"
        prefix="db"
        onAdd={handleAdd}
        onRemove={handleRemove}
        panelIcon={() => "fa-database"}
        panelTitle={(index) => `Database ${index + 1}`}
      >
        {(index) => (
          <>
            <Text
              name={`db${index}_name`}
              label="Name"
              placeholder="Connection name"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`db${index}_host`}
              label="Host"
              placeholder="localhost"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`db${index}_port`}
              label="Port"
              placeholder="5432"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        )}
      </FormMultiInput>
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Save Connections" />
        </div>
      </div>
    </Form>
  );
};

export const WithPanels: Story = {
  render: () => <WithPanelsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Using panelIcon and panelTitle to wrap each item in its own Panel. This provides better visual separation for complex items.",
      },
    },
  },
};

const WithHeaderComponent = () => {
  const [model, setModel] = useState<Record<string, string>>({});

  const handleAdd = (index: number) => {
    setModel({ ...model, [`env${index}_key`]: "", [`env${index}_value`]: "" });
  };

  const handleRemove = (index: number) => {
    const newModel = Object.entries(model).reduce(
      (acc, [key, value]) => {
        if (!key.startsWith(`env${index}_`)) {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, string>
    );
    setModel(newModel);
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel({ ...newModel })}
      onSubmit={() => action("form submitted")(model)}
    >
      <FormMultiInput
        id="env-vars"
        title="Environment Variables"
        prefix="env"
        onAdd={handleAdd}
        onRemove={handleRemove}
        header={
          <div className="alert alert-info" style={{ marginBottom: "15px" }}>
            <i className="fa fa-info-circle" /> Add environment variables as key-value pairs. They will be available to
            the application at runtime.
          </div>
        }
      >
        {(index) => (
          <>
            <Text
              name={`env${index}_key`}
              label="Key"
              placeholder="API_KEY"
              required
              labelClass="col-md-3"
              divClass="col-md-4"
            />
            <Text
              name={`env${index}_value`}
              label="Value"
              placeholder="secret-value"
              required
              labelClass="col-md-3"
              divClass="col-md-4"
            />
          </>
        )}
      </FormMultiInput>
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Apply Variables" />
        </div>
      </div>
    </Form>
  );
};

export const WithHeader: Story = {
  render: () => <WithHeaderComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Using the header prop to display instructions or context above the item list.",
      },
    },
  },
};

const DisabledComponent = () => {
  const [model, setModel] = useState<Record<string, string>>({
    tag0_name: "production",
    tag1_name: "critical",
  });

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel({ ...newModel })}
      onSubmit={() => action("form submitted")(model)}
    >
      <FormMultiInput
        id="tags"
        title="Tags (Read-only)"
        prefix="tag"
        onAdd={() => {}}
        onRemove={() => {}}
        disabled={true}
      >
        {(index) => (
          <Text name={`tag${index}_name`} label="Tag" labelClass="col-md-3" divClass="col-md-6" disabled={true} />
        )}
      </FormMultiInput>
    </Form>
  );
};

export const Disabled: Story = {
  render: () => <DisabledComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Disabled FormMultiInput. Remove buttons are disabled, preventing item deletion.",
      },
    },
  },
};

const ComplexFormComponent = () => {
  const [model, setModel] = useState<Record<string, string>>({
    contact0_name: "John Doe",
    contact0_email: "john@example.com",
    contact0_phone: "+1-555-0100",
    contact0_role: "admin",
  });

  const handleAdd = (index: number) => {
    setModel({
      ...model,
      [`contact${index}_name`]: "",
      [`contact${index}_email`]: "",
      [`contact${index}_phone`]: "",
      [`contact${index}_role`]: "",
    });
  };

  const handleRemove = (index: number) => {
    const newModel = Object.entries(model).reduce(
      (acc, [key, value]) => {
        if (!key.startsWith(`contact${index}_`)) {
          acc[key] = value;
        }
        return acc;
      },
      {} as Record<string, string>
    );
    setModel(newModel);
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel({ ...newModel });
        action("form changed")(newModel);
      }}
      onSubmit={() => action("form submitted")(model)}
      formDirection="form-horizontal"
    >
      <FormMultiInput
        id="contacts"
        title="Emergency Contacts"
        prefix="contact"
        onAdd={handleAdd}
        onRemove={handleRemove}
        panelIcon={() => "fa-user"}
        panelTitle={(index) => model[`contact${index}_name`] || `Contact ${index + 1}`}
      >
        {(index) => (
          <>
            <Text
              name={`contact${index}_name`}
              label="Full Name"
              placeholder="John Doe"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`contact${index}_email`}
              label="Email"
              placeholder="john@example.com"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`contact${index}_phone`}
              label="Phone"
              placeholder="+1-555-0100"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
            <Text
              name={`contact${index}_role`}
              label="Role"
              placeholder="admin"
              required
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </>
        )}
      </FormMultiInput>
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-success" text="Save Contacts" />
        </div>
      </div>
    </Form>
  );
};

export const ComplexForm: Story = {
  render: () => <ComplexFormComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Complex example with multiple fields per item. Panel titles update dynamically based on the contact's name. Note how the prefix-based naming keeps all fields organized in the model.",
      },
    },
  },
};
