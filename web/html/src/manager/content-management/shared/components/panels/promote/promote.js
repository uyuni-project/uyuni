//@flow
import React from 'react';
import {ModalButton} from "components/dialog/ModalButton";
import DownArrow from '../../down-arrow/down-arrow';

type Props = {
  environment: Object,
}

const Promote = (props: Props) => {
  return (
    <React.Fragment>
      <DownArrow/>

      <div className="text-center">
        <ModalButton
          id={`${props.environment.id}-promote-modal-link`}
          className="btn-default"
          text="Promote"
          target={"todo"}/>
      </div>

      <DownArrow/>
    </React.Fragment>
  );
};

export default Promote;
