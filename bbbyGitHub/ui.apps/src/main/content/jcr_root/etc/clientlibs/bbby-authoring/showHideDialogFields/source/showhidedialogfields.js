/**
 * Coral 3 compatible extension to dropdowns enabling show/hide of dialog fields based on selected value.
 */

/*

# Instructions

## Key Attributes:

- coral3-dropdown-showhide : indicates this field should be watched
- coral3-showhide-target : CSS selector to use to find show/hide targets
- showhidetargetvalue : value which dictates visible/hidden state
- granite:class : The way you add custom classes to a dialog field in Coral 3

 <foo
     fieldLabel="Use Fixed Height"
     jcr:primaryType="nt:unstructured"
     name="./foo"
     sling:resourceType="granite/ui/components/coral/foundation/form/select">

     <granite:data
         jcr:primaryType="nt:unstructured"
         coral3-dropdown-showhide=""
         coral3-showhide-target=".foo-showhide-target"/>

     <items jcr:primaryType="nt:unstructured">
         <option0 jcr:primaryType="nt:unstructured" text="No" value="false"/>
         <option1 jcr:primaryType="nt:unstructured" text="Yes" value="true"/>
     </items>
 </foo>
 <bar
     granite:class="hide foo-showhide-target"
     jcr:primaryType="nt:unstructured"
     sling:resourceType="granite/ui/components/coral/foundation/container">

     <granite:data
         jcr:primaryType="nt:unstructured"
         showhidetargetvalue="true"/>

     <items jcr:primaryType="nt:unstructured">
         <title
             fieldLabel="Title"
             jcr:primaryType="nt:unstructured"
             name="./title"
             sling:resourceType="granite/ui/components/coral/foundation/form/textfield"/>
     </items>
 </bar>
 */

(function(document, $, Coral) {
    "use strict";

    $(document).on("dialog-ready", function(e) {
        $('[data-coral3-dropdown-showhide]').each(function() {
            var el = this;
            Coral.commons.ready(el, function() {
                showHide(el)

                el.addEventListener('change', function(e) {
                    showHide(e.currentTarget);
                })
            })
        })
    });

    function showHide(el) {
        var value = el.value;
        var target = $(el).data("coral3-showhide-target");

        if (target) {
        	hideAllTargets(target, value);
        	showMatchingTargets(target, value);
        }
    }

    function hideAllTargets(target) {
        $(target).each(function() {

            // Hide the field itself
            $(this).addClass('hide');
            // Hide the wrapper (so that label is also hidden)
            $(this).closest('.coral-Form-fieldwrapper').addClass('hide');
        });
    }

    function showMatchingTargets(target, value){
    	$(target).filter("[data-showhidetargetvalue='" + value + "']").each(function() {
            // Show the field
            $(this).removeClass('hide');
            // Show the wrapper (so that label is also shown)
            $(this).closest('.coral-Form-fieldwrapper').removeClass('hide');
        });
    }

})(document, Granite.$, Coral);
