import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import HelpIcon from "./HelpIcon";

const meta = {
  title: "Components/Utils/HelpIcon",
  component: HelpIcon,
  parameters: {
    docs: {
      description: {
        component:
          "Help icon component that displays a question mark icon with a tooltip. Used throughout the application to provide contextual help and explanations. Returns `null` if no text is provided.",
      },
    },
  },
  args: {
    text: "This is helpful information",
  },
  argTypes: {
    text: {
      control: "text",
      description:
        "Tooltip text displayed when hovering over the icon. If `null` or empty, the component renders nothing.",
      table: { type: { summary: "string | null" } },
    },
  },
} satisfies Meta<typeof HelpIcon>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive help icon. Hover over the icon to see the tooltip with the help text.",
      },
    },
  },
};

export const WithTooltip: Story = {
  args: {
    text: "This field is required for system registration",
  },
  parameters: {
    docs: {
      description: {
        story: "Help icon with descriptive tooltip text.",
      },
    },
  },
};

export const LongTooltip: Story = {
  args: {
    text: "This setting controls the automatic update behavior. When enabled, the system will check for updates daily at 3:00 AM UTC and automatically apply security patches. Regular updates still require manual approval.",
  },
  parameters: {
    docs: {
      description: {
        story: "Help icon with longer tooltip text providing detailed explanation.",
      },
    },
  },
};

export const NoText: Story = {
  args: {
    text: null,
  },
  parameters: {
    docs: {
      description: {
        story: "When `text` is `null` or empty, the component renders nothing. This allows conditional help icons.",
      },
    },
  },
};

export const InContext: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div>
          <label htmlFor="username-help-icon">
            Username <HelpIcon text="Choose a unique username for your account" />
          </label>
          <input id="username-help-icon" type="text" className="form-control" placeholder="Enter username" />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <label htmlFor="apikey-help-icon">
            API Key <HelpIcon text="Generate an API key from your user profile settings" />
          </label>
          <input id="apikey-help-icon" type="password" className="form-control" placeholder="Enter API key" />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <label htmlFor="maxconn-help-icon">
            Max Connections <HelpIcon text="Maximum number of concurrent database connections (recommended: 100)" />
          </label>
          <input id="maxconn-help-icon" type="number" className="form-control" defaultValue="100" />
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Help icons used alongside form field labels to provide contextual help.",
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
            Short help text: <HelpIcon text="Brief explanation" />
          </p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <p>
            In a sentence: The system will restart automatically <HelpIcon text="Restart occurs at 3:00 AM" /> after
            updates.
          </p>
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <h4>
            Section Title <HelpIcon text="This section contains important configuration options" />
          </h4>
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Different usage patterns for the help icon in various contexts.",
      },
    },
  },
};
