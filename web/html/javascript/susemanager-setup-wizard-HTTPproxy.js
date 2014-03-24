// setup wizard
$(document).ready(function(){
  //HTTP proxy 
  hpGetInputValues();
  hpCheckForm();

  function hpGetInputValues() {
    //saving the value of the inputs in the form
    hpInputHostname = $("#http-proxy-input-hostname").val();
    hpInputUsername = $("#http-proxy-input-Username").val();
    hpInputPassword = $("#http-proxy-input-password").val();
  }

  function hpCheckForm() {
    hpGetInputValues();
    //check if the inputs have no information
    if (hpInputHostname.length < 1 || hpInputUsername.length < 1 || hpInputPassword.length < 1) {
      hpShowInputs();
    } else {
      hpShowP();
    }
  }
  //show the inputs
  function hpShowInputs() {
    $.each($("#http-proxy input"), function(index, val) {
       /* iterate through array or object */
       $(this).show(100);
       hpHideP();
       hpShowSaveBtn();
    });
  }
  function hpHideInputs() {
    $.each($("#http-proxy input"), function(index, val) {
       /* iterate through array or object */
       $(this).hide(100);
    });
  }

  //show the <p> with the value of the inputs
  function hpShowP() {
    hpGetInputValues();
    $(".http-proxy-input-hostname").text(hpInputHostname).show(100);
    $(".http-proxy-input-Username").text(hpInputUsername).show(100);
    var hpPassword = hpInputPassword.length;
    $(".http-proxy-input-password").text(Array(hpPassword + 1).join("*")).show(100);
    hpHideInputs();
    hpShowEditBtn();
  }
  function hpHideP() {
    $.each($("#http-proxy form p"), function(index, val) {
       /* iterate through array or object */
       $(this).hide(100);
    });
  }

  function hpShowSaveBtn() {
    $("#http-save-btn").show();
    $("#http-proxy-edit").hide();
  }
  function hpShowEditBtn() {
    $("#http-proxy-edit").show();
    $("#http-save-btn").hide();
  }

  $("#http-proxy .panel-footer .fa-pencil").click(hpShowInputs);

  $("#http-proxy .panel-footer button").click(hpCheckForm);

  $("#http-proxy .fa-check-square.text-success").click(formVerification);


  function formVerification(){
    $(this).removeClass().addClass("fa fa-spinner fa-spin");
    //this is just a test for the message that simulates the response from the server 
    //with this timeout and random response (1 or 2)
    setTimeout(function(){
      var ranNum = Math.floor((Math.random()*2)+1);
      switch (ranNum) {
        case 1:
          var iconStatus = 'fa text-success fa-check-square';
          var modalResponse = "<i class='fa fa-check-square text-success'></i>You are correctly connected to the proxy";
          break;
        case 2:
          var iconStatus = 'fa fa-exclamation-circle text-danger';
          var modalResponse = "<i class='fa fa-exclamation-circle text-danger'></i>We could not connect to the proxy. Please check the information delivered.";
          break;
        default:
          var iconStatus = 'fa fa-exclamation-circle text-danger';
          var modalResponse = "<i class='fa fa-exclamation-circle text-danger'></i>We could not connect to the proxy. Please check the information delivered.";
      }
      $("#http-proxy .fa-spinner.fa-spin").removeClass().addClass(iconStatus);

      $("#Verify-Proxy .modal-body").html(modalResponse);
      $("#Verify-Proxy").modal("show");

    },2000);
  }

})
