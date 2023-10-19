package com.aem.avengers.core.services.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.aem.avengers.core.services.AvengersBasicService;
import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;

import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(immediate = true, service = {
		AvengersBasicService.class }, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@Designate(ocd = AvengersBasicServiceImpl.Config.class)
public class AvengersBasicServiceImpl implements AvengersBasicService {

	@ObjectClassDefinition(name = "AEM Avengers Basic Service", description = "This service includes the basic configuration")
	public @interface Config {

	}

	@Override
	public JsonObject addPageProperties(Page page) {
		JsonObject jsonObj = new JsonObject();
		String pageTitle = page.getTitle();
		jsonObj.addProperty("pageTitle", pageTitle);
		ValueMap pageProperties = page.getProperties();
		if (pageProperties.containsKey("author")) {
			String author = pageProperties.get("author", String.class);
			jsonObj.addProperty("author", author);
		}
		if (pageProperties.containsKey("date")) {
			SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date date = pageProperties.get("date", Date.class);
			String formatedDate = simpleFormat.format(date);
			jsonObj.addProperty("date", formatedDate);
		}
		return jsonObj;
	}
}
