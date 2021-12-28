import * as React from "react";

import _isNil from "lodash/isNil";

type Props = {
  device: { name: string; address: string; state: string; "PCI address"?: string };
  short?: boolean;
  showPciAddress?: boolean;
};

/**
 * Renders an interface device.
 * This component is likely to be of no use outside the Interfaces component
 */
export function Interface(props: Props) {
  const state = props.device["state"];
  return (
    <div className="interface">
      <div className={`fa fa-circle state-${state}`}> </div>
      <div className="name">{props.device["name"]}</div>
      {!props.short && props.showPciAddress && !_isNil(props.device["PCI address"]) && (
        <div className="pciaddr">{props.device["PCI address"]}</div>
      )}
      {!props.short && <div className="mac">{props.device["address"]}</div>}
    </div>
  );
}

Interface.defaultProps = {
  short: false,
  showPciAddress: false,
};
