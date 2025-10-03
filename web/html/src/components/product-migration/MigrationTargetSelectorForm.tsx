import * as React from "react";
import { useState } from "react";

import { AsyncButton } from "components/buttons";
import { IconTag } from "components/icontag";
import { Form, FormGroup, Label, Radio } from "components/input";

import { MigrationProductList } from "./MigrationProductList";
import { MigrationProduct, MigrationTarget } from "./types";

type Props = {
  migrationSource: MigrationProduct;
  migrationTargets: MigrationTarget[];
  targetId: string | undefined;
  onTargetChange: (targetId: string) => Promise<void>;
};

type FormModel = {
  selectedTarget?: string;
};

export const MigrationTargetSelectorForm: React.FC<Props> = ({
  migrationSource,
  migrationTargets,
  targetId,
  onTargetChange,
}): JSX.Element => {
  const firstSelectableTarget = migrationTargets.find((item) => item.missingChannels.length === 0);
  const [formModel, setFormModel] = useState<FormModel>(
    firstSelectableTarget !== undefined ? { selectedTarget: targetId ?? firstSelectableTarget.id } : {}
  );

  async function onSubmit(): Promise<void> {
    if (formModel.selectedTarget !== undefined) {
      await onTargetChange(formModel.selectedTarget);
    }
  }

  function getToolTipForTarget(target: MigrationTarget): string | undefined {
    if (target.missingChannels.length === 0) {
      return `Product ${target.targetProduct.id} - ${target.targetProduct.name}`;
    }

    return t("Target not available, the following channels are not synced:\n\n{missingChannels}", {
      missingChannels: target.missingChannels.map((channel) => "-" + channel).join("\n"),
    });
  }

  return (
    <Form
      className="form-horizontal"
      model={formModel}
      onChange={(newModel: FormModel) => setFormModel({ ...newModel })}
    >
      <FormGroup>
        <Label className="col-md-3" name={t("Source Product")} />
        <div className="form-control-static col-md-6">
          <MigrationProductList product={migrationSource} />
        </div>
      </FormGroup>
      <Radio
        name="selectedTarget"
        title={t("Product id")}
        label={t("Target Products")}
        labelClass="col-md-3"
        divClass="col-md-6"
        inputClass="mt-2"
        disabled
        items={migrationTargets.map((target) => ({
          label: (
            <div className="d-inline-flex">
              {target.missingChannels.length !== 0 && (
                <IconTag className="mt-1 mt-1 help-block" type="header-info" title={getToolTipForTarget(target)} />
              )}
              <MigrationProductList product={target.targetProduct} />
            </div>
          ),
          value: target.id,
          title: getToolTipForTarget(target),
          disabled: target.missingChannels.length !== 0,
        }))}
      />
      <div className="col-md-offset-3 offset-md-3 col-md-6">
        <AsyncButton id="submit-btn" className="btn-primary" text={t("Select Channels")} action={onSubmit} />
      </div>
    </Form>
  );
};
