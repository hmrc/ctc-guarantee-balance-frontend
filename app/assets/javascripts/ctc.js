document.body.className = ((document.body.className) ? document.body.className + ' js-enabled' : 'js-enabled');

// Find first ancestor of el with tagName
// or undefined if not found
function upTo(el, tagName) {
    tagName = tagName.toLowerCase();

    while (el && el.parentNode) {
      el = el.parentNode;
      if (el.tagName && el.tagName.toLowerCase() == tagName) {
        return el;
      }
    }

    // Many DOM methods return null if they don't
    // find the element they are searching for
    // It would be OK to omit the following and just
    // return undefined
    return null;
  }


// initialise GovUK lib
GOVUKFrontend.initAll();
HMRCFrontend.initAll();

if (document.querySelector('.autocomplete') != null) {
    accessibleAutocomplete.enhanceSelectElement({
        selectElement: document.querySelector('.autocomplete'),
        showAllValues: true
    });

    // =====================================================
    // Update autocomplete once loaded with fallback's aria attributes
    // Ensures hint and error are read out before usage instructions
    // =====================================================
    setTimeout(function(){
        var originalSelect = document.querySelector('select.autocomplete');
        if(originalSelect && originalSelect.getAttribute('aria-describedby') > ""){
            var parentForm = upTo(originalSelect, 'form');
            if(parentForm){
                var combo = parentForm.querySelector('[role="combobox"]');
                if(combo){
                    combo.setAttribute('aria-describedby', originalSelect.getAttribute('aria-describedby') + ' ' + combo.getAttribute('aria-describedby'));
                }
            }

        }
    }, 2000)
}

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
