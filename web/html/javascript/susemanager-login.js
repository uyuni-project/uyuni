$(document).on("ready", function() {
  $("aside").remove();
  $('.navbar-toggle').remove();
  $('#breadcrumb').remove();
  formFocus('loginForm', 'username');
  $("body").addClass('login-page');
  $('header').wrapInner('<div class="wrap"></div>').addClass('Raleway-font');
  $('section').wrapInner('<div class="wrap"></div>');
  $('footer .wrapper').addClass('wrap');
});
