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

//if (typeof accessibleAutocomplete != 'undefined' && document.querySelector('.autocomplete') != null) {
//    // load autocomplete
//    accessibleAutocomplete.enhanceSelectElement({
//        selectElement: document.querySelector('.autocomplete'),
//        showAllValues: true,
//        defaultValue: ''
//    });
//
//    // =====================================================
//    // Polyfill autocomplete once loaded
//    // =====================================================
//    var checkForLoad = setInterval(checkForAutocompleteLoad, 50);
//    var originalSelect = document.querySelector('select.autocomplete');
//    var parentForm = upTo(originalSelect, 'form');
//
//    function polyfillAutocomplete(){
//        var combo = parentForm.querySelector('[role="combobox"]');
//        // =====================================================
//        // Update autocomplete once loaded with fallback's aria attributes
//        // Ensures hint and error are read out before usage instructions
//        // =====================================================
//        if(originalSelect && originalSelect.getAttribute('aria-describedby') > ""){
//            if(parentForm){
//                if(combo){
//                    combo.setAttribute('aria-describedby', originalSelect.getAttribute('aria-describedby') + ' ' + combo.getAttribute('aria-describedby'));
//                }
//            }
//        }
//
//        // =====================================================
//        // Ensure when user replaces valid answer with a non-valid answer, then valid answer is not retained
//        // =====================================================
//        var holdSubmit = true;
//        parentForm.addEventListener('submit', function(e){
//            if(holdSubmit){
//                e.preventDefault()
//                if(originalSelect.querySelectorAll('[selected]').length > 0 || originalSelect.value > ""){
//
//                    var resetSelect = false;
//
//                    if(originalSelect.value){
//                        if(combo.value != originalSelect.querySelector('option[value="' + originalSelect.value +'"]').text){
//                            resetSelect = true;
//                        }
//                    }
//                    if(resetSelect){
//                        originalSelect.value = "";
//                        if(originalSelect.querySelectorAll('[selected]').length > 0){
//                            originalSelect.querySelectorAll('[selected]')[0].removeAttribute('selected');
//                        }
//                    }
//                }
//
//                holdSubmit = false;
//                //parentForm.submit();
//                HTMLFormElement.prototype.submit.call(parentForm); // because submit buttons have id of "submit" which masks the form's natural form.submit() function
//            }
//        })
//
//    }
//    function checkForAutocompleteLoad(){
//        if(parentForm.querySelector('[role="combobox"]')){
//            clearInterval(checkForLoad)
//            polyfillAutocomplete();
//        }
//    }
//}


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
