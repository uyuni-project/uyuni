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
      customIconClass="fa-small"
      // buttons={ this.renderModalAddButton('newFilter', t('Edit Filters')) }
    >
      <div className="min-height-panel">
        <h4>{t("To be implemented in the next beta")}</h4>
      </div>

    </Panel>
  );
};

export default Filters;
