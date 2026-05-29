import { type ReactNode, Component, createRef, RefObject } from "react";

import { productName } from "core/user-preferences";

import { AsyncButton, SubmitButton } from "components/buttons";
import { Form, Password, Radio, Text } from "components/input";
import { LargeTextInput, LargeTextInputMode, LargeTextInputRef } from "components/large-text-input";
import { Messages, MessageType } from "components/messages/messages";
import { TopPanel } from "components/panels";
import Validation from "components/validation";

import Network from "utils/network";

import { HubRegisterRequest } from "./types";

const PEM_CERTIFICATE_PLACEHOLDER = ["-----BEGIN CERTIFICATE-----", "...", "-----END CERTIFICATE-----"].join("\n");

enum RegistrationMode {
  Token = "token",
  UserPassword = "userPassword",
}

type FormModel = {
  serverFqdn: string;
  registrationMode: RegistrationMode;
  token?: string;
  username?: string;
  password?: string;
  rootCA_inputMode?: LargeTextInputMode;
};

type Props = Record<never, never>;

type State = {
  model: FormModel;
  messages: MessageType[];
  loading: boolean;
  validated: boolean;
};

export class RegisterPeripheralForm extends Component<Props, State> {
  // Initial form model
  private static readonly INITIAL_MODEL: FormModel = {
    serverFqdn: "",
    registrationMode: RegistrationMode.Token,
    rootCA_inputMode: LargeTextInputMode.NotNeeded,
  };

  private largeTextInputRef: RefObject<LargeTextInputRef>;

  constructor(props: Props) {
    super(props);

    this.largeTextInputRef = createRef<LargeTextInputRef>();

    this.state = {
      model: { ...RegisterPeripheralForm.INITIAL_MODEL },
      messages: [],
      loading: false,
      validated: false,
    };
  }

  private onSubmit(): void {
    this.setState({ loading: true });

    const formData = this.state.model;

    const commonData = {
      fqdn: formData.serverFqdn,
    };

    const authData =
      formData.registrationMode === RegistrationMode.Token
        ? { token: formData.token }
        : { username: formData.username, password: formData.password };

    const certDataPromise = this.largeTextInputRef.current?.getContent() ?? Promise.resolve(undefined);
    certDataPromise
      .then((certData) => {
        const rootCA = certData !== undefined ? { rootCA: certData.replace(/\r\n/g, "\n") } : {};
        const requestData: HubRegisterRequest = { ...commonData, ...authData, ...rootCA };

        return Network.post("/rhn/manager/api/admin/hub/peripherals", requestData);
      })
      .then(
        (response) => {
          const peripheralId = response.data;
          window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/${peripheralId}`);
        },
        (xhr) => this.setState({ messages: Network.responseErrorMessage(xhr) })
      )
      .finally(() => this.setState({ loading: false }));
  }

  private onValidate(valid: boolean): void {
    this.setState({ validated: valid });
  }

  private onChange(updatedValues: Partial<FormModel>): void {
    this.setState((prevState) => ({
      model: { ...prevState.model, ...updatedValues },
    }));
  }

  private clearFields(): void {
    this.setState({ model: { ...RegisterPeripheralForm.INITIAL_MODEL } });
  }

  public render(): ReactNode {
    return (
      <TopPanel title={t("Register a new peripheral server")} icon="fa fa-plus">
        {this.state.loading && (
          <Messages
            items={[
              {
                severity: "info",
                text: <p>{t("Peripheral registration in progress: waiting for a response...")}</p>,
              },
            ]}
          />
        )}
        {this.state.messages.length > 0 && <Messages items={this.state.messages} />}
        <Form
          className="form-horizontal"
          model={this.state.model}
          onValidate={(valid) => this.onValidate(valid)}
          onChange={(updatedValues) => this.onChange(updatedValues)}
          onSubmit={() => this.onSubmit()}
        >
          <Text
            name="serverFqdn"
            label={t("Peripheral Server FQDN")}
            hint={t("The unique, DNS-resolvable fully-qualified domain name of the server to register.")}
            required
            placeholder={t("e.g. server.domain.com")}
            labelClass="col-md-3"
            divClass="col-md-6"
            validators={[Validation.matches(/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/)]}
            invalidHint={t("Has to be a valid FQDN address")}
          />
          <Radio
            name="registrationMode"
            label={t("Registration mode")}
            title={t("Registration mode")}
            hint={t("Define how to connect to the remote server to initiate the registration")}
            inline={true}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
            defaultValue={RegistrationMode.Token}
            items={[
              { label: t("Existing token"), value: RegistrationMode.Token },
              { label: t("Administrator User/Password"), value: RegistrationMode.UserPassword },
            ]}
          />
          {this.state.model.registrationMode === RegistrationMode.Token && (
            <Text
              name="token"
              label={t("Token")}
              hint={t("The token that grant access to the remote server to establish the connection.")}
              required
              labelClass="col-md-3"
              divClass="col-md-6"
              placeholder="eyJhbGci..."
            />
          )}
          {this.state.model.registrationMode === RegistrationMode.UserPassword && (
            <>
              <Text
                name="username"
                label={t("Username")}
                hint={t("The username of {productName} Administrator of the remote server.", { productName })}
                validators={[Validation.matches(/^[A-Za-z0-9-_,.@]+$/)]}
                required
                labelClass="col-md-3"
                divClass="col-md-6"
              />
              <Password
                name="password"
                label={t("Password")}
                hint={t("The password of the specified user.")}
                required
                labelClass="col-md-3"
                divClass="col-md-6"
              />
            </>
          )}
          <LargeTextInput
            ref={this.largeTextInputRef}
            name="rootCA"
            label={t("Root CA certificate")}
            hint={t(
              "The Root certificate authority of the remote server, that must be trusted to establish a secure connection."
            )}
            uploadLabel={t("Certificate File")}
            uploadHint={t("Certificate file, in PEM format")}
            pasteLabel={t("PEM Certificate")}
            pasteHint={t("The text representing the certificate, in PEM format")}
            pastePlaceholder={PEM_CERTIFICATE_PLACEHOLDER}
          />

          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <SubmitButton
              id="submit-btn"
              className="btn-primary me-4"
              text={t("Register")}
              disabled={!this.state.validated || this.state.loading}
            />
            <AsyncButton
              id="clear-btn"
              defaultType="btn-default"
              icon="fa-eraser"
              text={t("Clear fields")}
              action={() => this.clearFields()}
              disabled={this.state.loading}
            />
          </div>
        </Form>
      </TopPanel>
    );
  }
}
