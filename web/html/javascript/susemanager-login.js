$(document).on("ready", function() {
  // move the footer to the end of the body
  const footer = $('footer');
  footer.remove();
  $('body').append(footer);

  $("aside").remove();
  $('.navbar-toggle').remove();
  formFocus('loginForm', 'username');
  $("body").addClass('login-page');
  $('header').wrapInner('<div class="wrap"></div>').addClass('Raleway-font');
  $('section').wrapInner('<div class="wrap"></div>');
  $('footer .wrapper').addClass('wrap');
});
