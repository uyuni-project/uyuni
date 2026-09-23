import { type MouseEvent, type ReactElement, cloneElement } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { type SystemOverview, iconAndName, statusDisplay } from "./systems";

const identities: SystemOverview[] = [
  {
    id: 101,
    serverName: "physical.example.com",
    isVirtualGuest: false,
    isVirtualHost: false,
    proxy: false,
    mgrServer: false,
  },
  {
    id: 102,
    serverName: "virtual-host.example.com",
    isVirtualGuest: false,
    isVirtualHost: true,
    proxy: true,
    mgrServer: false,
  },
  {
    id: 103,
    serverName: "guest.example.com",
    isVirtualGuest: true,
    isVirtualHost: false,
    proxy: false,
    mgrServer: true,
  },
  {
    id: 104,
    serverName: "unprovisioned.example.com",
    isVirtualGuest: false,
    isVirtualHost: false,
    entitlement: ["bootstrap_entitled"],
    proxy: false,
    mgrServer: false,
  },
];

const statuses = [
  "unentitled",
  "awol",
  "kickstarting",
  "reboot needed",
  "updates scheduled",
  "up2date",
  "critical",
  "updates",
];

type StoryLinkProps = {
  href?: string | null;
  onClick?: (event: MouseEvent<HTMLAnchorElement>) => void;
};

const preventStoryNavigation = (element: ReactElement<StoryLinkProps>) =>
  element.props.href
    ? cloneElement(element, {
        onClick: (event: MouseEvent<HTMLAnchorElement>) => event.preventDefault(),
      })
    : element;

const StoryStatusIndicator = ({ id, statusType, locked }: { id: number; statusType: string; locked: number }) => {
  const indicator = statusDisplay({ id, statusType, locked }, true);
  return cloneElement(
    indicator,
    undefined,
    preventStoryNavigation(indicator.props.children[0]),
    indicator.props.children[1]
  );
};

const SystemIndicators = () => (
  <div>
    <StripedStorySection>
      <StoryRow>
        <h4>System identity indicators</h4>
        <div style={{ display: "grid", gap: "12px" }}>
          {identities.map((system) => (
            <div key={system.id}>{preventStoryNavigation(iconAndName(system) as ReactElement<StoryLinkProps>)}</div>
          ))}
        </div>
      </StoryRow>
      <StoryRow>
        <h4>System status indicators</h4>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(180px, 1fr))", gap: "12px" }}>
          {statuses.map((statusType, index) => (
            <div key={statusType} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <StoryStatusIndicator id={200 + index} statusType={statusType} locked={index === 6 ? 1 : 0} />
              <span>{statusType}</span>
            </div>
          ))}
        </div>
      </StoryRow>
    </StripedStorySection>
  </div>
);

const meta = {
  title: "Components/Data Display/SystemIndicators",
  component: SystemIndicators,
  parameters: {
    docs: {
      description: {
        component:
          "Gallery for the public iconAndName and statusDisplay renderers used in system lists. Story links are intentionally prevented from navigating away from Storybook.",
      },
    },
  },
  argTypes: {},
} satisfies Meta<typeof SystemIndicators>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Overview: Story = {};
