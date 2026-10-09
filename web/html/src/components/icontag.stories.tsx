import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { icons, IconTag } from "./icontag";

const iconTypes = Object.keys(icons).sort();

const sizeOptions = ["sm", "md", "lg", "xl", "2xl"];

const statusOptions = ["danger", "warning", "success", "info", "muted"];

const tooltipPlacementOptions = ["top", "right", "bottom", "left"];

/** The semantic types share a prefix per domain, so the catalog below groups them the same way */
const catalogGroups = [
  { title: "Actions", prefix: "action-" },
  { title: "Patches", prefix: "errata-" },
  { title: "Event types", prefix: "event-type-" },
  { title: "Configuration files", prefix: "file-" },
  { title: "Page headers", prefix: "header-" },
  { title: "List items", prefix: "item-" },
  { title: "Navigation", prefix: "nav-" },
  { title: "Repositories", prefix: "repo-" },
  { title: "Setup wizard", prefix: "setup-wizard-" },
  { title: "Sorting", prefix: "sort-" },
  { title: "Systems", prefix: "system-" },
];

const groupedIconTypes = catalogGroups
  .map((group) => ({
    title: group.title,
    types: iconTypes.filter((type) => type.startsWith(group.prefix)),
  }))
  .concat({
    title: "Other",
    types: iconTypes.filter((type) => !catalogGroups.some((group) => type.startsWith(group.prefix))),
  });

const IconCatalog = (props: { types: string[] }) => (
  <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: "12px" }}>
    {props.types.map((type) => (
      <div key={type} style={{ display: "flex", alignItems: "baseline", gap: "8px", minWidth: 0 }}>
        <IconTag type={type} size="lg" />
        <code style={{ overflowWrap: "anywhere" }}>{type}asas</code>
      </div>
    ))}
  </div>
);

const meta = {
  title: "Components/IconTag",
  component: IconTag,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Renders a Font Awesome 4 or Spacewalk icon as an `<i>` element. Pass a semantic `type` from the icon map so the same concept looks the same everywhere, or raw Font Awesome classes through `icon` when no type fits — never both. The shared `fa` base class is added automatically.",
      },
    },
  },
  args: {
    type: "header-info",
    size: "lg",
    title: "",
    className: "",
    tooltipPlacement: "top",
    tooltipWide: false,
    ariaHidden: true,
    ariaLabel: "",
  },
  argTypes: {
    type: {
      control: "select",
      options: iconTypes,
    },
    icon: {
      control: false,
      description: "Raw Font Awesome classes. Mutually exclusive with `type`, so it has no control here.",
    },
    size: {
      control: "select",
      options: sizeOptions,
    },
    status: {
      control: "select",
      options: statusOptions,
    },
    className: { control: "text" },
    title: { control: "text" },
    tooltipPlacement: {
      control: "select",
      options: tooltipPlacementOptions,
    },
    tooltipWide: { control: "boolean" },
    ariaHidden: { control: "boolean" },
    ariaLabel: { control: "text" },
  },
} satisfies Meta<typeof IconTag>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Use the controls to try out `type`, `size`, `status`, `className` and the tooltip and ARIA properties.",
      },
    },
  },
};

export const Sizes: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "`size` maps to an `icon-size-*` class. Leave it out to inherit the font size of the surrounding text, which is what inline icons usually want.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <IconTag type="header-info" />
        <IconTag type="header-info" size="sm" />
        <IconTag type="header-info" size="md" />
        <IconTag type="header-info" size="lg" />
        <IconTag type="header-info" size="xl" />
        <IconTag type="header-info" size="2xl" />
      </StoryRow>
      <StoryRow>
        <code>inherited</code>
        <code>sm</code>
        <code>md</code>
        <code>lg</code>
        <code>xl</code>
        <code>2xl</code>
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Statuses: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "`status` maps to a `text-*` colour class. Colour alone never carries the meaning, so keep the icon next to a label or give it an accessible name.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <IconTag type="system-crit" size="lg" status="danger" />
        <IconTag type="system-warn" size="lg" status="warning" />
        <IconTag type="system-ok" size="lg" status="success" />
        <IconTag type="scap-nochange" size="lg" status="info" />
        <IconTag type="item-disabled" size="lg" status="muted" />
      </StoryRow>
      <StoryRow>
        <code>danger</code>
        <code>warning</code>
        <code>success</code>
        <code>info</code>
        <code>muted</code>
      </StoryRow>
    </StripedStorySection>
  ),
};

export const RawIcons: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Use `icon` with raw Font Awesome classes when the icon map has no fitting entry. Any extra Font Awesome class, such as `fa-spin`, goes in the same string.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <IconTag icon="fa-question-circle" size="lg" />
        <IconTag icon="fa-flask" size="lg" />
        <IconTag icon="fa-spinner fa-spin" size="lg" />
        <IconTag icon="spacewalk-icon-salt" size="lg" />
      </StoryRow>
      <StoryRow>
        <code>fa-question-circle</code>
        <code>fa-flask</code>
        <code>fa-spinner fa-spin</code>
        <code>spacewalk-icon-salt</code>
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Tooltips: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "A `title` turns the icon into a tooltip trigger, optionally placed with `tooltipPlacement` and widened with `tooltipWide` for multi-line text. The component only renders the markup; the tooltip itself is created by `initializeTooltips()`, which the app runs on every page load and the Storybook preview mirrors.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <IconTag type="header-info" size="lg" title="Tooltip on top" tooltipPlacement="top" />
        <IconTag type="header-info" size="lg" title="Tooltip on the right" tooltipPlacement="right" />
        <IconTag type="header-info" size="lg" title="Tooltip at the bottom" tooltipPlacement="bottom" />
        <IconTag type="header-info" size="lg" title="Tooltip on the left" tooltipPlacement="left" />
        <IconTag
          type="header-info"
          size="lg"
          tooltipWide
          title={`Required channels:

            SLE-Module-Basesystem15-SP5 - aarch64
            SLE-Product-SLES15-SP5 - aarch64`}
        />
      </StoryRow>
      <StoryRow>
        <code>top</code>
        <code>right</code>
        <code>bottom</code>
        <code>left</code>
        <code>tooltipWide</code>
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Accessibility: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Icons are hidden from assistive technology by default, which is right for decorative icons sitting next to a label. An icon that carries meaning on its own needs both `ariaLabel` and `ariaHidden={false}`, because a label is never announced on a hidden element.",
      },
    },
  },
  render: () => (
    <StorySection>
      <StoryRow>
        <span>
          <IconTag type="item-add" /> Add system
        </span>
        <code>decorative, hidden by default</code>
      </StoryRow>
      <StoryRow>
        <IconTag type="system-crit" size="lg" status="danger" ariaLabel="System is critical" ariaHidden={false} />
        <code>meaningful, named for screen readers</code>
      </StoryRow>
    </StorySection>
  ),
};

export const Catalog: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Every semantic `type` the component resolves, grouped by the part of the product it belongs to.",
      },
    },
  },
  render: () => (
    <>
      {groupedIconTypes.map((group) => (
        <StorySection key={group.title}>
          <h4>{group.title}</h4>
          <IconCatalog types={group.types} />
        </StorySection>
      ))}
    </>
  ),
};
