// @flow

import * as React from 'react';

type Props = {
  className?: string,
  children: React.Node,
}

function PanelRow(props: Props) {
  return (
    <div className={`row ${props.className != null ? props.className : ""}`}>
      {props.children}
    </div>
  );
}

export {
  PanelRow,
};
