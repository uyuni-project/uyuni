import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { PopUp } from "./popup";

type PopUpProps = React.ComponentProps<typeof PopUp>;

const HeaderlessPopUp = (args: PopUpProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const close = () => jQuery(`#${args.id}`).modal("hide");

  return (
    <div style={{ minHeight: "240px" }}>
      <Button
        className="btn-primary"
        text="Open headerless popup"
        handler={() => {
          setIsOpen(true);
          setTimeout(() => jQuery(`#${args.id}`).modal("show"), 0);
        }}
      />
      {isOpen ? (
        <PopUp
          {...args}
          content={
            <div>
              <p>This low-level popup omits the complete modal header.</p>
              <p>Prefer the React Dialog component for new code.</p>
            </div>
          }
          footer={<Button className="btn-default" text="Close" handler={close} />}
          onClosePopUp={() => {
            action("popup closed")();
            setIsOpen(false);
          }}
        />
      ) : null}
    </div>
  );
};

const meta = {
  title: "Deprecated/Dialogs/PopUp",
  component: PopUp,
  parameters: {
    docs: {
      story: {
        inline: false,
        height: "420px",
      },
      description: {
        component:
          "Low-level jQuery Bootstrap modal retained for compatibility. LegacyDialog already demonstrates its normal header, content, footer, and closability; this story covers the distinct headerless mode. Prefer Dialog for new code.",
      },
    },
  },
  args: {
    id: "headerless-popup",
    hideHeader: true,
    closableModal: true,
  },
  argTypes: {
    id: { control: "text", description: "Unique Bootstrap modal element ID." },
    className: { control: "text", description: "Additional class applied to the modal dialog." },
    title: { control: false, description: "Optional modal heading, omitted in this focused story." },
    content: { control: false, description: "Modal body content." },
    footer: { control: false, description: "Optional modal footer content." },
    hideHeader: {
      control: "boolean",
      description: "Removes the complete modal header, including title and close button.",
    },
    closableModal: {
      control: "boolean",
      description: "Allows closing with the backdrop or keyboard when enabled.",
    },
    onClosePopUp: { action: "popup closed", description: "Called after Bootstrap reports that the modal was hidden." },
  },
  render: (args) => <HeaderlessPopUp {...args} />,
} satisfies Meta<typeof PopUp>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Headerless: Story = {};
