import React from 'react';
import { storiesOf } from '@storybook/react';
import { DeleteDialog } from "./DeleteDialog";
import { ModalLink } from "./ModalLink";


storiesOf('Dialogs', module)
  .add('delete dialog', () => (
    <div>
      <ModalLink
        id={`delete-modal-link`}
        text="delete"
        target="delete-modal"
      />
      <DeleteDialog id="delete-modal"
                    title={t("Delete project")}
                    content={
                      <span>
                        {t("Are you sure you want to delete project ")}
                        <strong>{'CLM2'}</strong>
                      </span>
                    }
                    onConfirm={() => alert("deleted pressed")}
                    onClosePopUp={() => alert("modal closed")}
      />
    </div>
  ))

