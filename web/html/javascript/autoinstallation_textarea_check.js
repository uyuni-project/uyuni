var checkTheTextareaTimer;

function theTextareaIsNotEmpty(t){return t.replace(/[^a-zA-Z.]/g,"")!="";}

function checkTheTextarea(){
  var textAreaText = false;
  //Check textarea
  var textArea = document.getElementById("contents"); //Get the text area
  if(textArea) textAreaText = theTextareaIsNotEmpty(textArea.value);
  //END Check textarea
  //Check iFrame
  var iFrame = document.getElementById("frame_contents");
  if(iFrame&&iFrame.style.display=="inline"){
    var innerDoc = iFrame.contentDocuments || iFrame.contentWindow.document;
    if(innerDoc){
      var frameTextArea = innerDoc.getElementById("selection_field");
      textAreaText = theTextareaIsNotEmpty(frameTextArea.textContent||frameTextArea.innerText);
    }
  }
  //END Check iFrame
  //Toggle Submit button status
  var formSubmitButton = document.getElementById("autoinstallationDetailsUpdateButton");
  if(formSubmitButton){
    if(textAreaText && formSubmitButton.disabled) formSubmitButton.disabled = false;
    if(!textAreaText && !formSubmitButton.disabled) formSubmitButton.disabled = true;
  }
  checkTheTextareaTimer = setTimeout(function(){checkTheTextarea()}, 1200);
}