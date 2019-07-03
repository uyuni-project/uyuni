//@flow

import React from 'react';
import {AsyncButton} from "../../../components/buttons";

type TopPanelButtonsProps = {
  onCreate: Function,
}

const TopPanelButtons = (props: TopPanelButtonsProps) => {
    return (
      <div className="pull-right btn-group">
        <AsyncButton
          id="savebutton"
          className="btn-primary"
          title={t('Create project')}
          text={t('Create')}
          icon="fa-plus"
          action={props.onCreate}
        />
      </div>
    )
}

export default TopPanelButtons;
