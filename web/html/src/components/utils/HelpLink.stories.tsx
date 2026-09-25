import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { HelpLink } from "./HelpLink";

const meta = {
  title: "Components/Utilities/HelpLink",
  component: HelpLink,
  parameters: {
    docs: {
      description: {
        component:
          "Help link component that displays a clickable help icon linking to documentation. Opens documentation in a new tab with the appropriate locale. The link is prefixed with `/docs/{locale}/` automatically.",
      },
    },
  },
  args: {
    url: "reference/systems/system-details.html",
    text: "Help",
  },
  argTypes: {
    url: {
      control: "text",
      description:
        "Relative documentation URL (without the `/docs/{locale}/` prefix). The locale is determined from user preferences.",
      table: { type: { summary: "string" } },
    },
    text: {
      control: "text",
      description: 'Tooltip text for the help icon. Defaults to "Help" if not provided.',
      table: { type: { summary: "string" }, defaultValue: { summary: "Help" } },
    },
  },
} satisfies Meta<typeof HelpLink>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Interactive help link. Click the icon to open documentation (opens the documentation URL in a new tab).",
      },
    },
  },
};

export const InContext: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div>
          <h4>
            System Groups <HelpLink url="reference/systems/system-groups.html" />
          </h4>
          <p>Organize your systems into groups for easier management.</p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <h4>
            Configuration Channels{" "}
            <HelpLink url="reference/configuration/config-channels.html" text="Learn about configuration channels" />
          </h4>
          <p>Manage configuration files across multiple systems.</p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <label htmlFor="activation-key-help-link">
            Activation Key <HelpLink url="reference/systems/activation-keys.html" text="What are activation keys?" />
          </label>
          <input
            id="activation-key-help-link"
            type="text"
            className="form-control"
            placeholder="Enter activation key"
          />
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Help links used in various contexts: section headings and form field labels.",
      },
    },
  },
};

export const Variants: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div>
          <p>
            Inline with text: Learn how to configure systems <HelpLink url="reference/systems/index.html" /> for more
            information.
          </p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <p>
            After label:{" "}
            <strong>
              Organization Name <HelpLink url="reference/admin/organizations.html" text="About organizations" />
            </strong>
          </p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <div className="panel panel-default">
            <div className="panel-heading">
              <h3 className="panel-title">
                Content Lifecycle <HelpLink url="reference/admin/content-lifecycle.html" />
              </h3>
            </div>
            <div className="panel-body">Manage the lifecycle of your content across environments.</div>
          </div>
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Different usage patterns for help links in various UI contexts.",
      },
    },
  },
};
