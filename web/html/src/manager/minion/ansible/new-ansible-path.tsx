import * as React from "react";
import { TextField } from "components/fields";
import { AsyncButton } from "components/buttons";

const NewAnsiblePath = (props) => {
  return(
    <>
      <h4>{props.title}</h4>
      <div className="form-group">
        <TextField
          placeholder={props.placeholder}
          value={props.newInventoryPath}
          onChange={(e: any) => props.newPath(e.target.value.toString())}
        />
      </div>
      <div className="pull-right btn-group">
        <AsyncButton
          action={props.savePath}
          defaultType="btn-success"
          text={t("Save")}
          icon="fa-save"
        />
      </div>
    </>
  )
}

export default NewAnsiblePath;