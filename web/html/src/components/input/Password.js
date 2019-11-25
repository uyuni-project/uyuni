// @flow

import React from 'react';
import { Text } from './Text';
import { InputBase } from './InputBase';

type Props = {
  placeholder?: string,
  inputClass?: string,
} & InputBase.Props;

export function Password(props: Props) {
  return (<Text type="password" {...props} />);
}

Password.defaultProps = Object.assign({
  placeholder: undefined,
  inputClass: undefined,
}, InputBase.defaultProps);
