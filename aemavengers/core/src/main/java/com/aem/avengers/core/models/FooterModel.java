package com.aem.avengers.core.models;
import java.util.List;



import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.aem.avengers.core.utils.ResourceUtils;

import org.apache.sling.models.annotations.DefaultInjectionStrategy;


import org.apache.sling.api.resource.Resource;

@Model(adaptables = Resource.class, defaultInjectionStrategy=DefaultInjectionStrategy.OPTIONAL)
public class FooterModel  {
   
   @ValueMapValue
    private String copyright;

    @ValueMapValue
    private String icon;

    @ValueMapValue
    private String url; 

    @ValueMapValue
    private String linkTarget;

    @ValueMapValue
    private String alttext;    
   
    public String getCopyright(){
        return copyright;
    }
   public String getIcon() {
        return icon;
    }
    public String getUrl(){
        return url;
    }
    public String getLinkTarget() {
        return linkTarget;
    }
    public String getAlttext(){
        return alttext;
    }

    @ChildResource(name = "channels")
	private Resource multiDetailsFieldNameMultiResource;

	protected List<FooterModel> multiDetailsFieldNameMultiList;

	public List<FooterModel> getMultiFieldNameMultiList() {

		multiDetailsFieldNameMultiList = ResourceUtils.getListFromResource(multiDetailsFieldNameMultiResource, FooterModel.class);
		return multiDetailsFieldNameMultiList;
	}
    
    
}