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
          "Manages dynamic lists of form fields with add/remove functionality. Uses a prefix-based naming scheme (`prefix{index}_fieldname`) to track items in the form model. Items are laid out on a single line, as a group of stacked fields, or in their own Panel, depending on `variant`. Ideal for managing lists like remote hosts, email addresses, or any repeating data structures.",
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
    variant: "singleLine",
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
      description: "Function returning an icon class for each item's Panel. Only used by the `panel` variant.",
    },
    panelTitle: {
      control: false,
      description: "Function returning a title for each item's Panel. Only used by the `panel` variant.",
    },
    header: {
      control: false,
      description: "Content displayed between the title and the first item.",
    },
    rowClass: {
      control: "text",
      description:
        "CSS class applied to the element containing each item's fields. Used by the `singleLine` and `group` variants.",
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
    variant: {
      control: "select",
      options: ["panel", "singleLine", "group"],
      description:
        "Layout of the items. `singleLine` puts each item on one line with the remove button at its end, `group` stacks each item's fields with the remove button in the top-right corner and a divider between items, `panel` wraps each item in its own Panel.",
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
        variant={args.variant}
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
        variant="panel"
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
        variant="singleLine"
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

const SingleLineComponent = () => {
  const { model, setModel, onAdd, onRemove } = useMultiInputModel(
    "server",
    { _name: "" },
    { server0_name: "ntp1.example.com", server1_name: "ntp2.example.com" }
  );

  return (
    <Form model={model} onChange={(newModel) => setModel({ ...newModel })}>
      <FormMultiInput
        id="ntp-servers"
        title="NTP Servers"
        prefix="server"
        onAdd={onAdd}
        onRemove={onRemove}
        variant="singleLine"
      >
        {(index) => <Text name={`server${index}_name`} label="Server" labelClass="col-md-3" divClass="col-md-6" />}
      </FormMultiInput>
    </Form>
  );
};

export const SingleLine: Story = {
  render: () => <SingleLineComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          '`variant="singleLine"` shows each item on its own line, with the label, the input and the remove button aligned horizontally.',
      },
    },
  },
};

const GroupComponent = () => {
  const { model, setModel, onAdd, onRemove } = useMultiInputModel(
    "mirror",
    { _url: "", _user: "" },
    {
      mirror0_url: "https://mirror1.example.com",
      mirror0_user: "admin",
      mirror1_url: "https://mirror2.example.com",
      mirror1_user: "",
    }
  );

  return (
    <Form model={model} onChange={(newModel) => setModel({ ...newModel })}>
      <FormMultiInput id="mirrors" title="Mirrors" prefix="mirror" onAdd={onAdd} onRemove={onRemove} variant="group">
        {(index) => (
          <>
            <Text name={`mirror${index}_url`} label="URL" labelClass="col-md-3" divClass="col-md-6" />
            <Text name={`mirror${index}_user`} label="User" labelClass="col-md-3" divClass="col-md-6" />
          </>
        )}
      </FormMultiInput>
    </Form>
  );
};

export const Group: Story = {
  render: () => <GroupComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          '`variant="group"` stacks the fields of each item, puts the remove button in the top-right corner of the group and separates groups with a divider. Removing a group removes all its fields.',
      },
    },
  },
};
