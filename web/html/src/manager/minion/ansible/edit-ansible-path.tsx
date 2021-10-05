import * as React from "react";
import { TextField } from "components/fields";
import { AsyncButton, Button } from "components/buttons";

const EditAnsiblePath = (props) => {
  return (
    <>
      <div className="d-block">
        <div className="col-md-9">
          <TextField value={props.ansiblePath.path} onChange={(e) => props.editPath(e.target.value.toString())} />
        </div>
        <div className="btn-group pull-right">
          <AsyncButton
            defaultType="btn-success btn-sm"
            icon="fa-save"
            title={t("Save")}
            action={() => props.saveEditPath(props.editEntity)}
          />
          <Button className="btn-default btn-sm" icon="fa-close" title={t("Cancel")} handler={props.cancelHandler} />
          <AsyncButton
            defaultType="btn-danger btn-sm"
            icon="fa-trash"
            title={t("Delete")}
            action={() => props.deletePath(props.ansiblePath)}
          />
        </div>
      </div>
      <br />
    </>
  );
};

export default EditAnsiblePath;
