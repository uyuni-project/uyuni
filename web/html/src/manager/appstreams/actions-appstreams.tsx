import { Button } from "components/buttons";

export const AppStreamActions = ({ numberOfChanges, onReset, onSubmit }) => {
  return (
    <div className="text-right margin-bottom-sm">
      <div className="btn-group">
        {numberOfChanges > 0 && (
          <Button id="revertModuleChanges" className="btn-default" text={t("Reset")} handler={onReset} />
        )}
        <Button
          id="applyModuleChanges"
          className="btn-success"
          disabled={numberOfChanges === 0}
          text={t("Apply Changes") + (numberOfChanges > 0 ? " (" + numberOfChanges + ")" : "")}
          handler={onSubmit}
        />
      </div>
    </div>
  );
};
