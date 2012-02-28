// Put the legal note to the footer
function putLegalNote(text) {
    var p = document.createElement('p');
    p.setAttribute('style', 'margin:3% 30% 0');
    p.appendChild(document.createTextNode(text));
    var footer = document.getElementById('footer');
    footer.appendChild(p);
}

