package com.aem.avengers.core.services;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonObject;

public interface AvengersBasicService {

	JsonObject addPageProperties(Page page);

}