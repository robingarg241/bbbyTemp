package com.bbby.aem.core.models.component.traits.generic;

import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.widgets.TextField;
import com.bbby.aem.core.models.component.ComponentSlingModel;
import com.bbby.aem.core.models.component.traits.ComponentTrait;
import com.bbby.aem.core.util.SlingModelUtils;
import com.bbby.aem.dialog.classic.multifield.MultiCompositeField;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import java.beans.IntrospectionException;
import java.util.List;

public interface DomainListTrait extends ComponentTrait {

    interface DialogFields {

        @DialogField(fieldLabel = "Domain List", name = "./domainList", ranking = 101)
        @MultiCompositeField(maxLimit = 100)
        List<DomainModel> domainList();
    }

    default List<DomainModel> getDomainList() throws ReflectiveOperationException, IntrospectionException {
        Resource domainListResource = getResource().getChild("domainList");
        return SlingModelUtils.buildComponentSlingModels(domainListResource, getRequest(), DomainModel.class, true);
    }

    @Model(adaptables = { Resource.class, SlingHttpServletRequest.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    class DomainModel extends ComponentSlingModel {

        @DialogField(fieldLabel = "Domain", name = "./domain", ranking = 100,
            fieldDescription = "Text representation of domain", required = true)
        @TextField
        @Inject
        @ValueMapValue
        private String domain;

        public String getDomain() {
            return domain;
        }
    }
}
