import React from "react";

import { Button, DropdownButton, LinkButton } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { TextField } from "components/fields";
import { Form, Text } from "components/input";
import { MessagesContainer, showInfoToastr } from "components/toastr";
import Validation from "components/validation";

import Network from "utils/network";

import { CreateTokenRequest, TokenType } from "./types";

export enum AddTokenMethod {
  Issue,
  Store,
  IssueAndStore,
}

type Props = {
  method: AddTokenMethod;
  onCreated?: () => void;
};

type State = {
  createRequest: CreateTokenRequest | undefined;
  createRequestValid: boolean;
  generatedToken: string | undefined;
};

export class AddTokenButton extends React.Component<Props, State> {
  static defaultProps: Partial<Props> = {
    method: AddTokenMethod.IssueAndStore,
    onCreated: undefined,
  };

  public constructor(props: Props) {
    super(props);

    this.state = {
      createRequest: undefined,
      createRequestValid: false,
      generatedToken: undefined,
    };
  }

  public render(): React.ReactNode {
    return (
      <>
        {this.renderButton()}
        {this.renderCreationForm()}
        {this.renderTokenModal()}
      </>
    );
  }

  private renderButton(): React.ReactNode {
    switch (this.props.method) {
      case AddTokenMethod.Issue:
        return (
          <Button
            id="issue-btn"
            icon="fa-plus"
            className="btn-default"
            handler={() => this.setState({ createRequest: { type: TokenType.ISSUED } })}
            text={t("Issue a new token")}
          />
        );

      case AddTokenMethod.Store:
        return (
          <Button
            id="save-btn"
            icon="fa-save"
            className="btn-default"
            handler={() => this.setState({ createRequest: { type: TokenType.CONSUMED } })}
            text={t("Store an external token")}
          />
        );

      case AddTokenMethod.IssueAndStore:
        return (
          <DropdownButton
            text={t("Add token")}
            icon="fa-plus"
            title={t("Add a new access token")}
            className="btn-primary"
            items={[
              // eslint-disable-next-line jsx-a11y/anchor-is-valid
              <a
                id="issue-btn-link"
                key="issue"
                href="#"
                onClick={() => this.setState({ createRequest: { type: TokenType.ISSUED } })}
              >{t("Issue a new token")}</a>,
              // eslint-disable-next-line jsx-a11y/anchor-is-valid
              <a
                id="store-btn-link"
                key="store"
                href="#"
                onClick={() => this.setState({ createRequest: { type: TokenType.CONSUMED } })}
              >{t("Store an external token")}</a>,
            ]}
          />
        );
    }
  }

  private renderCreationForm(): React.ReactNode {
    if (this.state.createRequest === undefined) {
      return <></>;
    }

    return (
      <Dialog
        id="creation-modal"
        title={
          this.state.createRequest.type === TokenType.ISSUED ? t("Issue a new token") : t("Store an external token")
        }
        isOpen={this.state.createRequest !== undefined}
        onClose={() => this.setState({ createRequest: undefined })}
        content={
          <Form model={this.state.createRequest} onValidate={(valid) => this.setState({ createRequestValid: valid })}>
            <Text
              name="fqdn"
              label={t("Server FQDN")}
              required
              placeholder={t("e.g. server.domain.com")}
              labelClass="col-md-3"
              divClass="col-md-6"
              validators={[Validation.matches(/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/)]}
              invalidHint={t("Has to be a valid FQDN address")}
            />
            {this.state.createRequest.type === TokenType.CONSUMED && (
              <Text
                name="token"
                label={t("Token")}
                required
                labelClass="col-md-3"
                divClass="col-md-6"
                placeholder="eyJhbGci..."
              />
            )}
          </Form>
        }
        footer={
          <div className="col-lg-6">
            <div className="pull-right btn-group">
              <Button
                id="creation-modal-cancel-button"
                className="btn-default"
                text={t("Cancel")}
                handler={() => this.setState({ createRequest: undefined })}
              />
              <Button
                id="creation-modal-submit-button"
                className="btn-primary"
                disabled={!this.state.createRequestValid}
                text={this.state.createRequest.type === TokenType.ISSUED ? t("Issue") : t("Store")}
                handler={() => this.onCreateToken()}
              />
            </div>
          </div>
        }
      />
    );
  }

  private renderTokenModal(): React.ReactNode {
    if (this.state.generatedToken === undefined) {
      return <></>;
    }

    return (
      <Dialog
        id="show-token-modal"
        title={t("New token successfully issued")}
        isOpen={this.state.generatedToken !== undefined}
        closableModal={false}
        content={
          <>
            <p>
              {t(
                "The new token was generated successfully. Make sure to copy it now as you will not be able to see this again."
              )}
            </p>
            <Form className="panel-default" divClass="panel-body">
              <div className="row justify-content-md-center margin-top-sm">
                <TextField
                  type="text"
                  id="generated-token"
                  className="col-md-7"
                  value={this.state.generatedToken}
                  disabled={true}
                />
                <LinkButton
                  icon="fa-copy"
                  text={t("Copy")}
                  className="btn-default col-md-1"
                  handler={() => this.onCopyToClipboard()}
                />
              </div>
              <div className="row justify-content-md-center">
                <div className="col-md-8">
                  <MessagesContainer containerId="show-token-container" />
                </div>
              </div>
            </Form>
          </>
        }
        footer={
          <div className="col-lg-6">
            <div className="btn-group">
              <Button
                id="show-token-modal-close-button"
                className="btn-default"
                handler={() => this.onCloseTokenModal()}
                text={t("Close")}
                icon="fa-close"
              />
            </div>
          </div>
        }
      />
    );
  }

  private onCreateToken(): void {
    const request = this.state.createRequest;

    Network.post("/rhn/manager/api/admin/hub/access-tokens", request)
      .catch((xhr) => Network.showResponseErrorToastr(xhr))
      .then((response) => {
        // If the token was issued ensure it shown so the user can save it
        if (request?.type === TokenType.ISSUED) {
          this.setState({ generatedToken: response.data });
        } else {
          showInfoToastr("Access token successfully stored");
          if (this.props.onCreated !== undefined) {
            this.props.onCreated();
          }
        }
      })
      .finally(() => this.setState({ createRequest: undefined }));
  }

  private onCopyToClipboard() {
    if (this.state.generatedToken !== undefined) {
      navigator.clipboard.writeText(this.state.generatedToken);
      showInfoToastr(t("Access token copied to the clipboard"), {
        autoHide: true,
        containerId: "show-token-container",
      });
    }
  }

  private onCloseTokenModal() {
    this.setState({ generatedToken: undefined });

    if (this.props.onCreated !== undefined) {
      this.props.onCreated();
    }
  }
}
