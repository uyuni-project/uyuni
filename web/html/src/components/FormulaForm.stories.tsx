import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";

import FormulaForm from "./FormulaForm";

const formulaData = {
  formula_name: "webserver",
  formula_list: ["webserver"],
  layout: {
    service: {
      $type: "group",
      $name: "Service settings",
      $help: "Basic web service configuration.",
      enabled: {
        $type: "boolean",
        $name: "Enable service",
        $default: true,
      },
      package_name: {
        $type: "text",
        $name: "Package name",
        $required: true,
        $default: "nginx",
      },
      listen_port: {
        $type: "number",
        $name: "Listen port",
        $min: 1,
        $max: 65535,
        $default: 443,
      },
    },
    tls: {
      $type: "group",
      $name: "TLS settings",
      $help: "Certificate and protocol settings.",
      certificate_path: {
        $type: "text",
        $name: "Certificate path",
        $required: true,
        $default: "/etc/nginx/tls/server.crt",
      },
      minimum_protocol: {
        $type: "select",
        $name: "Minimum protocol",
        $values: ["TLSv1.2", "TLSv1.3"],
        $default: "TLSv1.2",
      },
      notes: {
        $type: "textarea",
        $name: "Deployment notes",
        $rows: 3,
        $default: "Managed by the webserver formula.",
      },
    },
  },
  system_data: {
    service: {
      enabled: true,
      package_name: "nginx",
      listen_port: 443,
    },
    tls: {
      certificate_path: "/etc/nginx/tls/production.crt",
      minimum_protocol: "TLSv1.3",
      notes: "Production TLS configuration.",
    },
  },
  group_data: {},
  metadata: {
    description: "Representative web service formula with grouped text, number, boolean, select, and textarea fields.",
  },
};

const getFormulaData = () => Promise.resolve(JSON.parse(JSON.stringify(formulaData)));

const mockNetworkPost = ((url: string, data: unknown) => {
  action("formula saved")({ url, data });
  return Utils.cancelable(Promise.resolve(["pillar_only_formula_saved"]));
}) as typeof Network.post;

const meta = {
  title: "Compositions/Configuration/FormulaForm",
  component: FormulaForm,
  beforeEach: () => {
    const originalNetworkPost = Network.post;
    Network.post = mockNetworkPost;

    return () => {
      if (Network.post === mockNetworkPost) {
        Network.post = originalNetworkPost;
      }
    };
  },
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Renders and saves a form from Salt formula metadata. This representative schema covers the common grouped field types without contacting a backend.",
      },
    },
  },
  args: {
    dataUrl: "/storybook/formula/webserver",
    getDataPromise: getFormulaData,
    saveUrl: "/storybook/formula/webserver/save",
    formulaId: 0,
    systemId: 1000010001,
    getFormulaUrl: (formulaId) => `/storybook/formula/${formulaId}`,
    scope: "system",
    messageTexts: {},
    addFormulaNavBar: (formulaList, activeFormulaId) =>
      action("formula navigation updated")({ formulaList, activeFormulaId }),
  },
  argTypes: {
    dataUrl: {
      control: false,
      description: "Backend URL used when no external data promise is supplied.",
    },
    getDataPromise: {
      control: false,
      description: "Provides a fresh local copy of the representative formula data.",
    },
    saveUrl: {
      control: false,
      description: "Save endpoint intercepted by the story lifecycle mock.",
    },
    formulaId: {
      control: false,
      description: "Index of the active formula in the navigation list.",
    },
    systemId: {
      control: false,
      description: "System or group identifier included in the save payload.",
    },
    getFormulaUrl: {
      control: false,
      description: "Builds the previous and next formula links.",
    },
    scope: {
      control: "inline-radio",
      options: ["system", "group"],
      description: "Controls whether system- or group-scoped values are editable.",
    },
    messageTexts: {
      control: false,
      description: "Optional translations for backend save-result message keys.",
    },
    addFormulaNavBar: {
      control: false,
      description: "Updates the surrounding formula navigation.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "900px", padding: "24px" }}>
      <FormulaForm key={args.scope} {...args} />
    </div>
  ),
} satisfies Meta<typeof FormulaForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
