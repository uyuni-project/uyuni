import SpaRenderer from "core/spa/spa-renderer";

import { ActionChainPicker } from "./action-chain-picker";

type Props = {
  actionChains: string;
};

export const renderer = (parent: Element, props: Props) => {
  let actionChains = [] as any[];
  try {
    actionChains = JSON.parse((props.actionChains || "").replace(/&quot;/g, '"'));
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(<ActionChainPicker actionChains={actionChains} />, parent);
};
