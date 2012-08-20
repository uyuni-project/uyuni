setTheTextareaChecking();
function setTheTextareaChecking(){
	var formSubmitButton = document.getElementById("autoinstallationDetailsUpdateButton");
	if(formSubmitButton){ //we do not want this to happen if the element does not exist
		formSubmitButton.disabled = false; //First disable the submit button
		var textArea = document.getElementById("contents"); //Get the text area
		var timer = function(){
            if(textArea.value!=""&&formSubmitButton.disabled) formSubmitButton.disabled = false;
            	else formSubmitButton.disabled = true;
        }
        setTimeout(timer, 1200); //Set up the timed checks
	}
}