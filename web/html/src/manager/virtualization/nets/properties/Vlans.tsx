import * as React from "react";
import Validation from "components/validation";
import { FormContext } from "components/input/Form";
import { FormMultiInput } from "components/input/FormMultiInput";
import { Text } from "components/input/Text";
import { Select } from "components/input/Select";

type Props = {};

export function Vlans(props: Props) {
  const formContext = React.useContext(FormContext);
  const model = formContext.model || {};
  return (
    <div className="col-md-12">
      <div className="col-md-7 col-md-offset-2">
        <FormMultiInput
          id="vlans"
          title={t("VLANs")}
          prefix={"vlans"}
          onAdd={(index) => {
            formContext.setModelValue?.(`vlans${index}_tag`, "");
            // If adding more than 1 VLAN tag, the user surely needs trunking too, auto enable it.
            if (index > 0) {
              formContext.setModelValue?.(`vlantrunk`, true);
            }
          }}
          onRemove={(index: number) => {
            Object.keys(model)
              .filter((key) => key.startsWith(`vlans${index}_`))
              .forEach((key) => formContext.setModelValue?.(key, undefined));
            // Vlan trunking makes no sense if there is zero or one vlan tag setup, disable it in those cases.
            if (Object.keys(model).filter((key) => key.match(/^vlan[0-9]+_tag$/)).length <= 1) {
              formContext.setModelValue?.(`vlantrunk`, false);
            }
          }}
          header={
            <div className="row multi-input-table-row">
              <div className="column-title col-md-6">
                {t("Tag")}
                <span className="required-form-field"> *</span>
              </div>
              <div className="column-title col-md-6">{t("Native mode")}</div>
            </div>
          }
          rowClass="multi-input-table-row"
        >
          {(index: number) => (
            <>
              <Text
                name={`vlans${index}_tag`}
                title={t(`VLAN ${index} tag`)}
                divClass="col-md-12"
                className="col-md-6"
                required
                validators={[Validation.isInt({ min: 0, max: 4095 })]}
                invalidHint={t("Integer between 0 and 4095")}
              />
              <Select
                name={`vlans${index}_native`}
                isClearable
                options={[
                  { value: "tagged", label: t("Tagged") },
                  { value: "untagged", label: t("Untagged") },
                ]}
                divClass="col-md-12"
                className="col-md-6"
                title={t(`VLAN ${index} native mode`)}
              />
            </>
          )}
        </FormMultiInput>
      </div>
    </div>
  );
}
