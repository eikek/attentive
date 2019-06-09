/* Attentive JS */

var elmApp = Elm.Main.init({
    node: document.getElementById("attentive-app"),
    flags: elmFlags
});

elmApp.ports.initElements.subscribe(function() {
    console.log("Initialsing elements …");
    $('.ui.dropdown').dropdown();
    $('.ui.checkbox').checkbox();
    $('.ui.accordion').accordion();
});

elmApp.ports.setAccount.subscribe(function(args) {
    var state = args[0];
    var goto = args[1];
    if (state.authenticator && state.authenticator.length > 0) {
        localStorage.setItem('account', JSON.stringify(state));
    } else {
        localStorage.removeItem('account');
    }
    if (goto) {
        location.href = goto;
        location.reload();
    }
});

elmApp.ports.removeAccount.subscribe(function(goto) {
    localStorage.removeItem('account');
    if (goto && goto.length > 0) {
        location.href = goto;
        location.reload();
    }
});
