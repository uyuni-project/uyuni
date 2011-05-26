var shortPass = 'Password is too short'
var badPass = 'Weak: Use letters & numbers'
var goodPass = 'Medium: Use special characters'
var strongPass = 'Strong password'
var sameAsUsername = 'Password is the same as login'
var emptyPass = 'Enter password'

function passwordStrength(password, username) {
    score = 0
    // password.length == 0
    if (password.length == 0) return emptyPass
    // password.length < 5
    if (password.length < 5) return shortPass
    // Check placeholder
    if (password == '******') return ''
    // password == username
    if (password.toLowerCase() == username.toLowerCase()) return sameAsUsername
    // Password length
    score += password.length * 4
    score += ( checkRepetition(1,password).length - password.length ) * 1
    score += ( checkRepetition(2,password).length - password.length ) * 1
    score += ( checkRepetition(3,password).length - password.length ) * 1
    score += ( checkRepetition(4,password).length - password.length ) * 1
    // Password has 3 numbers
    if (password.match(/(.*[0-9].*[0-9].*[0-9])/)) score += 5
    // Password has 2 symbols
    if (password.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)) score += 5
    // Password has upper and lower chars
    if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)) score += 10
    // Password has numbers and chars
    if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)) score += 15
    // Password has numbers and symbols
    if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([0-9])/)) score += 15
    // Password has chars and symbols
    if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([a-zA-Z])/)) score += 15
    // Password is just numbers or chars
    if (password.match(/^\w+$/) || password.match(/^\d+$/)) score -= 10
    // Verify 0 < score < 100
    if (score < 0) score = 0
    if (score > 100) score = 100
    // Return something
    if (score < 34 ) return badPass
    if (score < 68 ) return goodPass
    return strongPass;
}

function passwordStrengthPercent(password, username) {
    score = 0
    // Return no score if password < 5 chars
    if (password.length < 5) return score
    // Check placeholder
    if (password == '******') return score
    // Password == username
    if (password.toLowerCase()==username.toLowerCase()) return score
    // Password length
    score += password.length * 4
    score += ( checkRepetition(1,password).length - password.length ) * 1
    score += ( checkRepetition(2,password).length - password.length ) * 1
    score += ( checkRepetition(3,password).length - password.length ) * 1
    score += ( checkRepetition(4,password).length - password.length ) * 1
    // Password has 3 numbers
    if (password.match(/(.*[0-9].*[0-9].*[0-9])/))  score += 5
    // Password has 2 symbols
    if (password.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)) score += 5
    // Password has upper and lower chars
    if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)) score += 10
    // Password has number and chars
    if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)) score += 15 
    // Password has number and symbol
    if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([0-9])/))  score += 15
    // Password has char and symbol
    if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([a-zA-Z])/))  score += 15
    // Password is just a nubers or chars
    if (password.match(/^\w+$/) || password.match(/^\d+$/) )  score -= 10
    // Return something
    if (score > 100) score = 100
    return score
}

// checkRepetition(1,'aaaaaaabcbc')   = 'abcbc'
// checkRepetition(2,'aaaaaaabcbc')   = 'aabc'
// checkRepetition(2,'aaaaaaabcdbcd') = 'aabcd'
function checkRepetition(pLen, str) {
  res = ""
  for (i=0; i<str.length; i++) {
    repeated = true
    for (j=0; j<pLen && (j+i+pLen) < str.length; j++)
      repeated = repeated && (str.charAt(j+i) == str.charAt(j+i+pLen))
    if (j < pLen) repeated = false
    if (repeated) {
      i += pLen-1
      repeated = false
    } else {
      res+=str.charAt(i)
    }
  }
  return res
}

// Get a css value from stylesheet, external or inline
function getStyle(el, cssprop){
  if (el.currentStyle)
    return el.currentStyle[cssprop] // IE
  else if (document.defaultView && document.defaultView.getComputedStyle)
    return document.defaultView.getComputedStyle(el, "")[cssprop] // FF
  else
    return el.style[cssprop] // Inline style
}

// Return the width of the graybar as integer
function getGraybarWidth() {
  var graybar = document.getElementById("pwstrength-graybar");
  return parseInt(getStyle(graybar, "width"));
}

// Get the desired password
function getDesiredPass() {
  var element = document.getElementById("desiredpass");
  return element.value;
}

// Get the login, might be innerHTML
function getLogin() {
  var element = document.getElementById("login");
  var login = element.value;
  if (typeof(login) == "undefined") {
	login = element.innerHTML;
  }
  return login;
}

// Set innerHTML of an element
function setInnerHTML(element, value) {
  var element = document.getElementById(element);
  element.innerHTML = value;
}

// Set the colorbar background-position and width
function setColorbar(position, width) {
  colorbar = document.getElementById("pwstrength-colorbar");
  colorbar.style.backgroundPosition = position;
  colorbar.style.width = width;
}

// Call this 'onkeyup' for the login and password input fields
function checkPassword() {
    var login = getLogin();
    var pass = getDesiredPass();
    var factor = getGraybarWidth() / 100;
    var result = passwordStrength(pass, login); 
    setInnerHTML("pwstrength-result", result);
    var perc = passwordStrengthPercent(pass, login);
    setColorbar("0px -" + perc + "px", (perc * factor) + "px");
    setInnerHTML("pwstrength-percent", " " + perc + "% ");
}
