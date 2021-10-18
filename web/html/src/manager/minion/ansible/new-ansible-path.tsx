import * as React from "react";
import { TextField } from "components/fields";
import { AsyncButton } from "components/buttons";

const NewAnsiblePath = (props) => {
  return (
    <>
      <h4>{props.title}</h4>
      <div className="form-group">
        <TextField
          id={"new_" + props.pathType + "_path_input"}
          placeholder={props.placeholder}
          value={props.newPathValue}
          onChange={(e: any) => props.newPath(e.target.value.toString())}
        />
      </div>
      <div className="pull-right btn-group">
        <AsyncButton
          id={"new_" + props.pathType + "_path_save"}
          action={props.savePath}
          defaultType="btn-success"
          text={t("Save")}
          icon="fa-save"
        />
      </div>
    </>
  );
};

export default NewAnsiblePath;
