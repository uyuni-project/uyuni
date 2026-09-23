import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { PoliciesPicker } from "./policies-picker";

type PoliciesPickerProps = React.ComponentProps<typeof PoliciesPicker>;

const policies = [
  {
    id: 1,
    policyName: "CIS Server Level 1",
    dataStreamName: "SLES 15 Security Guide",
    description: "A practical baseline for general-purpose servers.",
    assigned: true,
    position: 1,
  },
  {
    id: 2,
    policyName: "CIS Server Level 2",
    dataStreamName: "SLES 15 Security Guide",
    description: "A stricter profile for security-sensitive servers.",
  },
  {
    id: 3,
    policyName: "PCI DSS",
    dataStreamName: "SLES 15 Compliance Guide",
    description: "Controls intended for systems processing payment-card data.",
  },
  {
    id: 4,
    policyName: "DISA STIG",
    dataStreamName: "SLES 15 Security Guide",
    description: "Security requirements for United States Department of Defense systems.",
  },
];

const mockNetworkGet = ((url: string) => {
  const filter = new URL(url, window.location.origin).searchParams.get("filter")?.toLowerCase() ?? "";
  const matches = policies.filter(
    (policy) => policy.policyName.toLowerCase().includes(filter) || policy.dataStreamName.toLowerCase().includes(filter)
  );
  return Utils.cancelable(Promise.resolve(matches.map((policy) => ({ ...policy }))));
}) as typeof Network.get;

const PoliciesPickerStory = (props: PoliciesPickerProps) => {
  return (
    <div style={{ maxWidth: "1200px", minHeight: "420px" }}>
      <PoliciesPicker {...props} />
    </div>
  );
};

const meta = {
  title: "Compositions/Compliance/PoliciesPicker",
  component: PoliciesPicker,
  beforeEach: () => {
    const originalNetworkGet = Network.get;
    Network.get = mockNetworkGet;

    return () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
    };
  },
  parameters: {
    docs: {
      description: {
        component:
          "Searches and assigns a single SCAP policy. Search for `CIS`, `PCI`, an unknown term, or clear the field to exercise populated and empty results without contacting a backend.",
      },
    },
  },
  args: {
    matchUrl: (filter = "") => `/storybook/policies?filter=${encodeURIComponent(filter)}`,
    saveRequest: async (selectedPolicies) => {
      action("policy saved")(selectedPolicies);
      return selectedPolicies;
    },
    messages: action("messages changed"),
  },
  argTypes: {
    matchUrl: {
      control: false,
      description: "Builds the search endpoint; the story intercepts it and filters local fixture data.",
    },
    saveRequest: {
      control: false,
      description: "Persists the selected policy.",
    },
    messages: {
      action: "messages changed",
      description: "Receives save messages instead of rendering them inside the picker.",
    },
  },
  render: (args) => <PoliciesPickerStory {...args} />,
} satisfies Meta<typeof PoliciesPicker>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
