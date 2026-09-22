import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

import { ActionStatus } from "../action/ActionStatus";

// This is not a real component, but documentation for using Bootstrap tooltips
const TooltipDocs = () => null;

const meta = {
  title: "Components/Utils/Tooltip",
  component: TooltipDocs,
  parameters: {
    docs: {
      description: {
        component: `
Bootstrap tooltip system used throughout the application. Tooltips provide contextual help and additional information on hover.

## Usage

Add \`data-bs-toggle="tooltip"\` and a \`title\` attribute to any element:

\`\`\`jsx
<a href="#" data-bs-toggle="tooltip" title="Helpful text">
  Link
</a>
\`\`\`

Some components (Buttons, ActionStatus) already include the \`data-bs-toggle\` attribute automatically — just add a \`title\` prop.

## Placement

Use \`data-bs-placement\` to control position: \`top\` (default), \`right\`, \`bottom\`, \`left\`.

Buttons use the \`tooltipPlacement\` prop instead of \`data-bs-placement\`.

## Wide Tooltips

For multi-line or wider tooltips, use \`data-bs-custom-class="wide-tooltip"\`.
`,
      },
    },
  },
} satisfies Meta;

export default meta;

type Story = StoryObj<typeof meta>;

export const BasicUsage: Story = {
  render: () => (
    <div style={{ padding: "40px" }}>
      <h4>Hover over elements to see tooltips</h4>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-default" title="Add new item" icon="fa-plus" />
          <Button className="btn-tertiary" title="Delete item" icon="fa-trash" />
          <Button className="btn-default" text="Settings" title="Configure settings" icon="fa-cog" />
          <a href="#tooltip-example" data-bs-toggle="tooltip" title="Tooltip on link" style={{ margin: "0 1rem" }}>
            Link with tooltip
          </a>
          <i
            className="fa fa-info-circle fa-2x"
            data-bs-toggle="tooltip"
            title="Additional information"
            style={{ cursor: "pointer" }}
          />
        </StoryRow>
      </StripedStorySection>
      <div style={{ marginTop: "2rem", padding: "1rem", backgroundColor: "#f5f5f5", borderRadius: "4px" }}>
        <p>
          <strong>Code examples:</strong>
        </p>
        <pre style={{ fontSize: "12px" }}>
          {`<Button className="btn-default" title="Add new item" icon="fa-plus" />
<a href="#example" data-bs-toggle="tooltip" title="Tooltip text">Link</a>
<i className="fa fa-info-circle" data-bs-toggle="tooltip" title="Info" />`}
        </pre>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Common tooltip usage patterns. Hover over icons, buttons, and links to see tooltips appear.",
      },
    },
  },
};

export const Placement: Story = {
  render: () => (
    <div style={{ padding: "80px", textAlign: "center" }}>
      <h4>Tooltip Placement</h4>
      <p>Hover over buttons to see tooltips in different positions</p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-default" text="Top" tooltipPlacement="top" title="Top tooltip" />
          <Button className="btn-default" text="Right" tooltipPlacement="right" title="Right tooltip" />
          <Button className="btn-default" text="Bottom" tooltipPlacement="bottom" title="Bottom tooltip" />
          <Button className="btn-default" text="Left" tooltipPlacement="left" title="Left tooltip" />
        </StoryRow>
      </StripedStorySection>
      <div style={{ marginTop: "2rem" }}>
        <h5>Custom Elements</h5>
        <div style={{ display: "flex", gap: "2rem", justifyContent: "center", marginTop: "1rem" }}>
          <a href="#tooltip-top" data-bs-toggle="tooltip" data-bs-placement="top" title="Top tooltip">
            Top
          </a>
          <a href="#tooltip-right" data-bs-toggle="tooltip" data-bs-placement="right" title="Right tooltip">
            Right
          </a>
          <a href="#tooltip-bottom" data-bs-toggle="tooltip" data-bs-placement="bottom" title="Bottom tooltip">
            Bottom
          </a>
          <a href="#tooltip-left" data-bs-toggle="tooltip" data-bs-placement="left" title="Left tooltip">
            Left
          </a>
        </div>
      </div>
      <div style={{ marginTop: "2rem", padding: "1rem", backgroundColor: "#f5f5f5", borderRadius: "4px" }}>
        <p>
          <strong>Code:</strong>
        </p>
        <pre style={{ fontSize: "12px", textAlign: "left" }}>
          {`// For Buttons, use tooltipPlacement prop:
<Button text="Top" tooltipPlacement="top" title="Top tooltip" />

// For other elements, use data-bs-placement attribute:
<a data-bs-toggle="tooltip" data-bs-placement="right" title="Right tooltip">
  Link
</a>`}
        </pre>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Control tooltip position with placement options: top, right, bottom, left.",
      },
    },
  },
};

export const WideTooltip: Story = {
  render: () => (
    <div style={{ padding: "40px" }}>
      <h4>Wide Tooltip for Multi-line Content</h4>
      <StripedStorySection>
        <StoryRow>
          <i
            className="fa fa-info-circle fa-3x"
            style={{ cursor: "pointer" }}
            data-bs-toggle="tooltip"
            data-bs-custom-class="wide-tooltip"
            title={`Required channels:

SLE-Module-Basesystem15-SP5 - aarch64
SLE-Product-SLES15-SP5 - aarch64
SLE-Module-Server-Applications15-SP5 - aarch64`}
          />
          <span style={{ marginLeft: "2rem" }}>Hover over the icon for multi-line tooltip</span>
        </StoryRow>
      </StripedStorySection>
      <div style={{ marginTop: "2rem", padding: "1rem", backgroundColor: "#f5f5f5", borderRadius: "4px" }}>
        <p>
          <strong>Code:</strong>
        </p>
        <pre style={{ fontSize: "12px" }}>
          {`<i
  className="fa fa-info-circle"
  data-bs-toggle="tooltip"
  data-bs-custom-class="wide-tooltip"
  title={\`Required channels:

SLE-Module-Basesystem15-SP5 - aarch64
SLE-Product-SLES15-SP5 - aarch64\`}
/>`}
        </pre>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Use `data-bs-custom-class='wide-tooltip'` for tooltips with multiple lines or wider content. Newlines in the title will be preserved.",
      },
    },
  },
};

export const WithActionStatus: Story = {
  render: () => (
    <div style={{ padding: "40px" }}>
      <h4>ActionStatus Component with Tooltip</h4>
      <p>ActionStatus automatically includes tooltip support</p>
      <StripedStorySection>
        <StoryRow>
          <ActionStatus serverId="server123" actionId="456" status="Queued" data-bs-placement="right" />
          <ActionStatus serverId="server124" actionId="457" status="Completed" data-bs-placement="top" />
          <ActionStatus serverId="server125" actionId="458" status="Failed" data-bs-placement="bottom" />
        </StoryRow>
      </StripedStorySection>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "ActionStatus component with built-in tooltip support. Hover over status icons to see details.",
      },
    },
  },
};

export const IconButtons: Story = {
  render: () => (
    <div style={{ padding: "40px" }}>
      <h4>Icon-only Buttons</h4>
      <p>Icon buttons without visible text should always have tooltips for accessibility</p>
      <StripedStorySection>
        <StoryRow>
          <Button className="btn-primary" title="Create new" icon="fa-plus" />
          <Button className="btn-default" title="Edit" icon="fa-pencil" />
          <Button className="btn-danger" title="Delete" icon="fa-trash" />
          <Button className="btn-default" title="Download" icon="fa-download" />
          <Button className="btn-default" title="Refresh" icon="fa-refresh" />
          <Button className="btn-default" title="Settings" icon="fa-cog" />
        </StoryRow>
      </StripedStorySection>
      <div style={{ marginTop: "1rem", padding: "1rem", backgroundColor: "#fffbcc", borderRadius: "4px" }}>
        <strong>⚠️ Accessibility Note:</strong> Icon-only buttons must include a <code>title</code> prop for screen
        readers and tooltip display.
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Icon-only buttons require tooltips to clearly communicate their purpose to all users.",
      },
    },
  },
};
