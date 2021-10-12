import { useEffect } from "react";
import Network from "utils/network";
import { useImmer } from "use-immer";

type LoginApiStateType = {
  messages: Array<string>;
  success: boolean;
  loading: boolean;
};

const errorsMessage = {
  "error.invalid_login": t("Either the password or username is incorrect."),
  "account.disabled": t("Your account has been deactivated."),
  "error.user_readonly": t("This user has read only API access. WebUI login is denied."),
};

const useLoginApi = () => {
  const [loginApiState, setLoginApiState] = useImmer<LoginApiStateType>({
    messages: [],
    success: false,
    loading: false,
  });

  useEffect(
    () =>
      window.addEventListener("beforeunload", (e) => {
        if (loginApiState.loading) {
          const confirmationMessage = t("Are you sure you want to close this page while login is in progress?");
          (e || window.event).returnValue = confirmationMessage;
          return confirmationMessage;
        }
      }),
    []
  );

  const onLogin = ({ login, password }: { login: string; password: string }) => {
    setLoginApiState((state) => {
      state.loading = false;
    });

    const formData = {
      login: login.trim(),
      password: password.trim(),
    };

    return Network.post("/rhn/manager/api/login", formData).then(
      (data) => {
        setLoginApiState((state) => {
          state.success = data.success;
          state.messages = data.messages && data.messages.map((msg) => errorsMessage[msg]);
          state.loading = false;
        });
        return data.success;
      },
      (xhr) => {
        const errMessages =
          xhr.status === 0
            ? [t("Request interrupted or invalid response received from the server. Please try again.")]
            : Network.errorMessageByStatus(xhr.status);
        setLoginApiState((state) => {
          state.success = false;
          state.messages = errMessages;
          state.loading = false;
        });
      }
    );
  };

  return {
    onLogin,
    success: loginApiState.success,
    loading: loginApiState.loading,
    messages: loginApiState.messages,
  };
};

export default useLoginApi;
