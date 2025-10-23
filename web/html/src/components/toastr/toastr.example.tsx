import { MessagesContainer, showSuccessToastr } from "./toastr";

export default () => {
  return (
    <>
      <MessagesContainer />
      <button
        onClick={() =>
          showSuccessToastr("Great success", {
            autoHide: false,
            closeButton: true,
          })
        }
      >
        showSuccessToastrs
      </button>
    </>
  );
};
