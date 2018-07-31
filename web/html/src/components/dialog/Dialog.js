"use strict";

const React = require("react");
const PopUp = require("../popup").PopUp;

function Dialog(props) {
  return (
      <PopUp
          id={props.id}
          className={props.className}
          content={props.content}
          title={props.title}
          footer={props.buttons}
          onClosePopUp={() => props.onClosePopUp && props.onClosePopUp(props.item)}
      />
  );
}

module.exports = {
    Dialog
};
