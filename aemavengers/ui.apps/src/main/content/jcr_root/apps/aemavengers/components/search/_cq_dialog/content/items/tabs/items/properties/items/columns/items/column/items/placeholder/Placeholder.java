/*******************************************************************************
 * Copyright 2016 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package libs.wcm.foundation.components.parsys.placeholder;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.components.EditContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class Placeholder extends WCMUsePojo {

    private String title;
    private Set<String> classNames = new HashSet<String>();

    @Override
    public void activate() throws Exception {
        Resource targetResource = (Resource)getRequest().getAttribute(getResource().getPath());

        if (targetResource == null) {
            return;
        }

        classNames.add("cq-placeholder");

        ValueMap targetVm = targetResource.getValueMap();

        if (targetVm != null && targetVm.containsKey(JcrConstants.JCR_TITLE)) {
            title = targetVm.get(JcrConstants.JCR_TITLE, String.class);
        }

        EditContext editContext = get("editContext", EditContext.class);
        if (editContext != null && editContext.getComponentContext() != null) {
            Set<String> cssClassNames = editContext.getComponentContext().getCssClassNames();

            if (cssClassNames != null) {
                classNames.addAll(cssClassNames);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public Set<String> getClassNames() {
        return classNames;
    }
}
