document.body.className = ((document.body.className) ? document.body.className + ' js-enabled' : 'js-enabled');

// initialise GovUK lib
GOVUKFrontend.initAll();

// back link
var backLink = document.querySelector('.govuk-back-link');
if(backLink){
    backLink.addEventListener('click', function(e){
        e.preventDefault();
        if (window.history && window.history.back && typeof window.history.back === 'function'){
            window.history.back();
        }
    });
}


// Introduce direct skip link control, to work around voiceover failing of hash links
// https://bugs.webkit.org/show_bug.cgi?id=179011
// https://axesslab.com/skip-links/
document.querySelector('.govuk-skip-link').addEventListener('click',function(e) {
    e.preventDefault();
    var header = [].slice.call(document.querySelectorAll('h1'))[0];
    if(header!=undefined){
        header.setAttribute('tabindex', '-1')
        header.focus();
        setTimeout(function(){
            header.removeAttribute('tabindex')
        }, 1000)
    }
});
