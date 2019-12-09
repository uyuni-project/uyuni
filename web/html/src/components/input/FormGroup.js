// @flow

import * as React from 'react';

type Props = {
  isError: boolean,
  children: React.Node,
};

export function FormGroup(props: Props) {
  return (
    <div className={`form-group${props.isError ? ' has-error' : ''}`}>
      {props.children}
    </div>
  );
}
