import { type ReactNode, Component } from "react";

import { Button, LinkButton } from "components/buttons";
import { DangerDialog } from "components/dialog/DangerDialog";
import { Dialog } from "components/dialog/Dialog";
import { Form, Radio, Text, TextArea } from "components/input";
import { showErrorToastr } from "components/toastr";

export enum ButtonMode {
  TextAndIcon,
  Text,
  Icon,
}

enum EditMethod {
  Upload = "upload",
  Paste = "paste",
}

type EditFormModel = {
  method: EditMethod;
  uploadedValue?: string;
  pastedValue?: string;
};

type Props = {
  /** The text data, can be null */
  value: string | null;
  /** The filename to be suggested to the user for the download */
  filename: string;
  /** Hide the text message before the action buttons */
  hideMessage: boolean;
  /** The message to show when the data is present */
  presentMessage?: string;
  /** The message to show when the data is null */
  absentMessage?: string;
  /** Defines if the component allows editing the data */
  editable: boolean;
  /** Defines if the allows downloading the data */
  downloadable: boolean;
  /** The title of the editing dialog, used for both modification and creation */
  editDialogTitle: string;
  /** A custom message displayed in the editing dialog, before the form */
  editMessage?: string;
  /** Message displayed on the delete confirmation dialog */
  confirmDeleteMessage: string;
  /** Show only icons for the buttons */
  buttonMode: ButtonMode;
  /** Enable or disable the component */
  disabled: boolean;
  /** Callback to invoked when the user confirms on the edit dialog */
  onEdit: (value: string) => Promise<any>;
  /** Callback to invoked when the user confirms on the delete dialog */
  onDelete: () => Promise<any>;
};

type State = {
  showDeleteDialog: boolean;
  showEditDialog: boolean;
  editFormModel: EditFormModel;
  editFormValidated: boolean;
};

/**
 * Component to handle large text data. It allows the user to download, edit and delete the text data.
 */
export class LargeTextAttachment extends Component<Props, State> {
  private static readonly INITAL_MODEL: EditFormModel = { method: EditMethod.Upload };

  static defaultProps: Partial<Props> = {
    filename: "attachment.txt",
    editable: false,
    downloadable: true,
    hideMessage: false,
    presentMessage: t("Data is present."),
    absentMessage: t("Data is not present."),
    editDialogTitle: t("Edit"),
    editMessage: undefined,
    confirmDeleteMessage: "Are you sure?",
    buttonMode: ButtonMode.TextAndIcon,
    disabled: false,
    onEdit: async () => undefined,
    onDelete: async () => undefined,
  };

  public constructor(props: Props) {
    super(props);

    this.state = {
      showDeleteDialog: false,
      showEditDialog: false,
      editFormModel: { ...LargeTextAttachment.INITAL_MODEL },
      editFormValidated: false,
    };
  }

  public render(): ReactNode {
    const valueObject = new Blob([this.props.value ?? ""], { type: "text/plain" });
    const valuePresent = this.props.value !== null;
    const downloadUrl = URL.createObjectURL(valueObject);

    return (
      <>
        {!this.props.hideMessage && <p>{valuePresent ? this.props.presentMessage : this.props.absentMessage}</p>}
        <div className={`btn-group${this.props.hideMessage ? "" : " pull-right"}`}>
          {valuePresent && this.props.downloadable && (
            <LinkButton
              text={this.showTextIfNeeded(t("Download"))}
              icon={this.showIconIfNeeded("fa-download")}
              title={this.showTooltipIfNeeded(t("Download"))}
              className="btn-default"
              href={downloadUrl}
              download={this.props.filename}
              disabled={this.props.disabled}
            />
          )}
          {this.props.editable && (
            <>
              <Button
                text={this.showTextIfNeeded(valuePresent ? t("Edit") : t("Add"))}
                icon={this.showIconIfNeeded(valuePresent ? "fa-edit" : "fa-plus")}
                title={this.showTooltipIfNeeded(valuePresent ? t("Edit") : t("Add"))}
                className="btn-default"
                disabled={this.props.disabled}
                handler={() =>
                  this.setState({ showEditDialog: true, editFormModel: { ...LargeTextAttachment.INITAL_MODEL } })
                }
              />
              {this.state.showEditDialog && (
                <Dialog
                  id="edit-modal"
                  title={this.props.editDialogTitle}
                  isOpen={this.state.showEditDialog !== undefined}
                  closableModal={false}
                  content={
                    <>
                      {this.props.editMessage !== undefined && <p>{this.props.editMessage}</p>}
                      <Form
                        className="form-horizontal"
                        model={this.state.editFormModel}
                        onValidate={(valid) => this.onEditFormValidate(valid)}
                        onChange={(updatedValues) => this.onEditFormChange(updatedValues)}
                      >
                        <Radio
                          name="method"
                          label={t("Edit method")}
                          title={t("Edit method")}
                          hint={t("Define how to provide the new value")}
                          inline={true}
                          required
                          labelClass="col-md-3"
                          divClass="col-md-6"
                          defaultValue={EditMethod.Upload}
                          items={[
                            { label: t("Upload a file"), value: EditMethod.Upload },
                            { label: t("Paste the new value"), value: EditMethod.Paste },
                          ]}
                        />
                        {this.state.editFormModel.method === EditMethod.Upload && (
                          <Text
                            name="uploadedValue"
                            label={t("File")}
                            required
                            type="file"
                            labelClass="col-md-3"
                            divClass="col-md-6"
                          />
                        )}
                        {this.state.editFormModel.method === EditMethod.Paste && (
                          <TextArea
                            name="pastedValue"
                            label={t("New value")}
                            required
                            rows={15}
                            labelClass="col-md-3"
                            divClass="col-md-6"
                          />
                        )}
                      </Form>
                    </>
                  }
                  footer={
                    <div className="col-lg-6">
                      <div className="pull-right btn-group">
                        <Button
                          id="edit-cancel-button"
                          className="btn-default"
                          text={t("Cancel")}
                          handler={() => this.setState({ showEditDialog: false })}
                        />
                        <Button
                          id="creation-modal-submit-button"
                          className="btn-primary"
                          disabled={!this.state.editFormValidated}
                          text={t("Save")}
                          handler={() => this.onEditFormSubmit()}
                        />
                      </div>
                    </div>
                  }
                />
              )}
              {valuePresent && (
                <>
                  <Button
                    text={this.showTextIfNeeded(t("Delete"))}
                    icon={this.showIconIfNeeded("fa-trash")}
                    title={this.showTooltipIfNeeded(t("Delete"))}
                    className="btn-default"
                    disabled={this.props.disabled}
                    handler={() => this.setState({ showDeleteDialog: true })}
                  />
                  {this.state.showDeleteDialog && (
                    <DangerDialog
                      id="confirm-delete-modal"
                      title={t("Confirm Deletion")}
                      content={<span>{this.props.confirmDeleteMessage}</span>}
                      isOpen={this.state.showDeleteDialog}
                      onConfirmAsync={this.props.onDelete}
                      onClose={() => this.setState({ showDeleteDialog: false })}
                      submitText={t("Delete")}
                    />
                  )}
                </>
              )}
            </>
          )}
        </div>
      </>
    );
  }

  private onEditFormValidate(valid: boolean): void {
    this.setState({ editFormValidated: valid });
  }

  private onEditFormChange(updatedValues: Partial<EditFormModel>): void {
    this.setState((prevState) => ({
      editFormModel: { ...prevState.editFormModel, ...updatedValues },
    }));
  }

  private onEditFormSubmit(): void {
    const formData = this.state.editFormModel;

    let newValuePromise: Promise<string>;
    switch (formData.method) {
      case EditMethod.Upload:
        newValuePromise = new Promise((resolve, reject) => {
          const uploadField = document.getElementById("uploadedValue") as HTMLInputElement | null;
          const filename = uploadField?.files?.[0];
          if (filename) {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result as string);
            reader.onerror = () => reject(reader.error);
            reader.readAsText(filename);
          }
        });
        break;

      case EditMethod.Paste:
        newValuePromise = Promise.resolve(formData.pastedValue ?? "");
        break;
    }

    newValuePromise
      .then((value) => this.props.onEdit(value))
      .catch((error) => showErrorToastr(error))
      .finally(() => this.setState({ showEditDialog: false }));
  }

  // Shows the text based on the current button mode
  private showTextIfNeeded(text: string): string | undefined {
    if (this.props.buttonMode === ButtonMode.Icon) {
      return undefined;
    }

    return text;
  }

  // Shows the icon based on the current button mode
  private showIconIfNeeded(iconName: string): string {
    if (this.props.buttonMode === ButtonMode.Text) {
      return "";
    }

    return iconName;
  }

  // Shows the tooltip based on the current button mode
  private showTooltipIfNeeded(tooltip: string): string | undefined {
    if (this.props.buttonMode !== ButtonMode.Icon) {
      return "";
    }

    return tooltip;
  }
}
