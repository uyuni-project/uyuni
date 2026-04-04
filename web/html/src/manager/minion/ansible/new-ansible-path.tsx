import { AsyncButton } from "components/buttons";
import { TextField } from "components/fields";

type Props = {
  title: string;
  pathType: string;
  newPathValue: string;
  newPath: (value: string) => any;
  placeholder?: string;
  savePath: (...args: any) => any;
};

const NewAnsiblePath = (props: Props) => {
  return (
    <>
      <h4>{props.title}</h4>
      <div className="form-group">
        <div>
          <TextField
            id={"new_" + props.pathType + "_path_input"}
            placeholder={props.placeholder}
            value={props.newPathValue}
            onChange={(e: any) => props.newPath(e.target.value.toString())}
          />
        </div>
      </div>
      <div className="d-flex justify-content-end">
        <div className="btn-group">
          <AsyncButton
            id={"new_" + props.pathType + "_path_save"}
            action={props.savePath}
            defaultType="btn-primary"
            text={t("Save")}
            icon="fa-save"
          />
        </div>
      </div>
    </>
  );
};

export default NewAnsiblePath;
