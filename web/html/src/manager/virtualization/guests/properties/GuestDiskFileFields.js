// @flow

import * as React from 'react';
import { Text } from 'components/input/Text';
import { FormContext } from 'components/input/Form';

type Props = {
  index: number,
  domainCaps: Object,
  poolCaps: Object,
  pools: Array<Object>,
  onlyHandledDisks: boolean,
}

export function GuestDiskFileFields(props: Props) : React.Node {
  const formContext = React.useContext(FormContext);

  return (
    <>
      <Text
        name={`disk${props.index}_source_file`}
        label={t('File')}
        disabled={!Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
        labelClass="col-md-3"
        divClass="col-md-6"
      />
    </>
  );
}
