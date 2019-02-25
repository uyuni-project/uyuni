// @flow
import React from 'react';
import {Panel} from "../../../../../../components/panels/Panel";

type Props = {};

const Filters = (props:  Props) => {
  return (
    <Panel
      headingLevel="h2"
      title={t('Filters')}
      collapseId="filters-id"
      // buttons={ this.renderModalAddButton('newFilter', t('Add new Filter')) }
    >
      <div className="min-height-panel">

      </div>

    </Panel>
  );
}

export default Filters;
