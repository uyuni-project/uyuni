import Network from "utils/network";

declare global {
  interface Window {
    /** The standard network layer made globally available for legacy integrations such as DWR etc */
    network?: typeof Network;
  }
}

window.network = Network;
