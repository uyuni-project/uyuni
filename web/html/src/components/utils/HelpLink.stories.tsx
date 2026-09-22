import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { HelpLink } from "./HelpLink";

const meta = {
  title: "Components/Utils/HelpLink",
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

export const SystemDetails: Story = {
  args: {
    url: "reference/systems/system-details.html",
    text: "Learn more about system details",
  },
  parameters: {
    docs: {
      description: {
        story: "Help link to system details documentation with custom tooltip text.",
      },
    },
  },
};

export const DefaultTooltip: Story = {
  args: {
    url: "reference/admin/organizations.html",
  },
  parameters: {
    docs: {
      description: {
        story: 'Help link with default tooltip text "Help".',
      },
    },
  },
};

export const QuickStart: Story = {
  args: {
    url: "quickstart/index.html",
    text: "View quick start guide",
  },
  parameters: {
    docs: {
      description: {
        story: "Help link to the quick start guide.",
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
          <label>
            Activation Key <HelpLink url="reference/systems/activation-keys.html" text="What are activation keys?" />
          </label>
          <input type="text" className="form-control" placeholder="Enter activation key" />
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
            <label>
              Organization Name <HelpLink url="reference/admin/organizations.html" text="About organizations" />
            </label>
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

export const DocumentationLinks: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div style={{ display: "flex", gap: "20px", alignItems: "center" }}>
          <span>
            Quick Start <HelpLink url="quickstart/index.html" />
          </span>
          <span>
            Installation <HelpLink url="installation/index.html" />
          </span>
          <span>
            Upgrade <HelpLink url="upgrade/index.html" />
          </span>
          <span>
            Administration <HelpLink url="administration/index.html" />
          </span>
          <span>
            API <HelpLink url="api/index.html" text="API documentation" />
          </span>
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Collection of common documentation links.",
      },
    },
  },
};
