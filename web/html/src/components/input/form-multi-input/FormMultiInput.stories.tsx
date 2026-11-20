/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

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
    onAdd: action("item added"),
    onRemove: action("item removed"),
    children: () => null,
    id: "multi-input",
    title: "Email Addresses",
    prefix: "item",
    disabled: false,
  },
  argTypes: {
    id: {
      control: "text",
      description: "Unique identifier for the component.",
    },
    title: {
      control: "text",
      description: "Title displayed in the panel header.",
    },
    prefix: {
      control: "text",
      description:
        "Prefix for field names. Fields are named `{prefix}{index}_fieldname`. For example, with prefix 'host', fields become 'host0_name', 'host0_port', etc.",
    },
    disabled: {
      control: "boolean",
      description: "Disables the remove buttons. Add button is always enabled.",
    },
    panelIcon: {
      control: false,
      description: "Function returning an icon class for each item's panel. If provided, items are wrapped in panels.",
    },
    panelTitle: {
      control: false,
      description: "Function returning a title for each item's panel. If provided, items are wrapped in panels.",
    },
    header: {
      control: false,
      description: "Content displayed between the title and the first item.",
    },
    rowClass: {
      control: "text",
      description: "CSS class applied to the row containing each item's fields.",
    },
    panelHeading: {
      control: "select",
      options: ["h1", "h2", "h3", "h4", "label"],
      description:
        "HTML tag used for the list title (default 'h2'). Use 'label' to make the list look like a form field.",
    },
    panelClassName: {
      control: "text",
      description: "CSS class for the wrapping Panel.",
    },
    onAdd: {
      action: "item added",
      description: "Callback invoked when adding a new item. Receives the new index as parameter.",
    },
    onRemove: {
      action: "item removed",
      description: "Callback invoked when removing an item. Receives the item's index as parameter.",
    },
    children: {
      control: false,
      description: "Function that renders the fields for one item. Receives the item's index as parameter.",
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
        title={args.title ?? "Email Addresses"}
        prefix="email"
        onAdd={handleAdd}
        onRemove={handleRemove}
        disabled={args.disabled}
        rowClass={args.rowClass}
        panelHeading={args.panelHeading}
        panelClassName={args.panelClassName}
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

const useMultiInputModel = (
  prefix: string,
  emptyItem: Record<string, string>,
  initial: Record<string, string> = {}
) => {
  const [model, setModel] = useState<Record<string, string>>(initial);

  const onAdd = (index: number) => {
    const newFields = Object.fromEntries(
      Object.entries(emptyItem).map(([name, value]) => [`${prefix}${index}${name}`, value])
    );
    setModel({ ...model, ...newFields });
  };

  const onRemove = (index: number) => {
    setModel(
      Object.fromEntries(Object.entries(model).filter(([key]) => !new RegExp(`^${prefix}${index}(_|$)`).test(key)))
    );
  };

  return { model, setModel, onAdd, onRemove };
};

const FormFieldStyleComponent = () => {
  const { model, setModel, onAdd, onRemove } = useMultiInputModel(
    "intermediateCA",
    { "": "" },
    { intermediateCA0: "" }
  );

  return (
    <Form model={model} onChange={(newModel) => setModel({ ...newModel })} formDirection="form-horizontal">
      <Text name="rootCA" label="Root CA" type="file" labelClass="col-md-3" divClass="col-md-6" />
      <div className="form-group">
        <FormMultiInput
          id="intermediateCAs"
          title="Intermediate CAs"
          prefix="intermediateCA"
          onAdd={onAdd}
          onRemove={onRemove}
          panelHeading="label"
          panelClassName="panel-default col-md-6 offset-md-3 no-padding"
        >
          {(index) => (
            <Text
              name={`intermediateCA${index}`}
              label="An intermediate CA certificate (PEM format)"
              type="file"
              divClass="col-md-8"
              labelClass="col-md-4 no-padding"
              className="col-md-11"
            />
          )}
        </FormMultiInput>
      </div>
      <Text name="proxyCertificate" label="Proxy certificate" type="file" labelClass="col-md-3" divClass="col-md-6" />
    </Form>
  );
};

export const FormFieldStyle: Story = {
  render: () => <FormFieldStyleComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          'Embedding the list among regular form fields, as the proxy configuration pages do. `panelHeading="label"` renders the title like a field label and `panelClassName` places the panel in the form grid. Each item has a single field named `{prefix}{index}`, without a suffix.',
      },
    },
  },
};

const PanelTitleOnlyComponent = () => {
  const { model, setModel, onAdd, onRemove } = useMultiInputModel(
    "repo",
    { _url: "" },
    { repo0_url: "https://download.example.com/repo" }
  );

  return (
    <Form model={model} onChange={(newModel) => setModel({ ...newModel })}>
      <FormMultiInput
        id="repositories"
        title="Repositories"
        prefix="repo"
        onAdd={onAdd}
        onRemove={onRemove}
        panelTitle={(index) => `Repository ${index + 1}`}
      >
        {(index) => <Text name={`repo${index}_url`} label="URL" labelClass="col-md-3" divClass="col-md-6" />}
      </FormMultiInput>
    </Form>
  );
};

export const PanelTitleOnly: Story = {
  render: () => <PanelTitleOnlyComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Passing only `panelTitle` is enough to wrap each item in its own Panel, without an icon.",
      },
    },
  },
};

const RowClassComponent = () => {
  const { model, setModel, onAdd, onRemove } = useMultiInputModel(
    "alias",
    { _name: "" },
    { alias0_name: "web01", alias1_name: "web02" }
  );

  return (
    <Form model={model} onChange={(newModel) => setModel({ ...newModel })}>
      <FormMultiInput
        id="aliases"
        title="Host Aliases"
        prefix="alias"
        onAdd={onAdd}
        onRemove={onRemove}
        rowClass="align-items-center"
      >
        {(index) => <Text name={`alias${index}_name`} label="Alias" labelClass="col-md-3" divClass="col-md-6" />}
      </FormMultiInput>
    </Form>
  );
};

export const RowClass: Story = {
  render: () => <RowClassComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "`rowClass` adds CSS classes to each item row in the default (non-panel) layout, here to center the fields and the remove button vertically. It has no effect when items are wrapped in panels.",
      },
    },
  },
};
