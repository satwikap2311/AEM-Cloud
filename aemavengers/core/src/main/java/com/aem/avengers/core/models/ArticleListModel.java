/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aem.avengers.core.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.avengers.core.utils.ResourceUtils;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonArray;

@Model(adaptables = { Resource.class,
		SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArticleListModel {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArticleListModel.class);

	@ValueMapValue
	private String fecets;

	@ValueMapValue
	private String pagination;

	@ValueMapValue
	private String parentPage;

	@SlingObject
	private ResourceResolver resourceResolver;

	public String getFecets() {
		LOGGER.info("fecets",fecets);
		return fecets;
	}

	public void setFecets(String fecets) {
		this.fecets = fecets;
	}

	public String getPagination() {
		return pagination;
	}

	public void setPagination(String pagination) {
		this.pagination = pagination;
	}

	public Map<String, String> getAllFecets() {
		Map<String, String> fecets = new HashMap<>();
		PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
//		Page page = pageManager.getPage(parentPage);
		Session session = resourceResolver.adaptTo(Session.class);
		String queryString = "/jcr:root".concat(parentPage).concat("//element(*, cq:Page)");
		Workspace workspace = session.getWorkspace();
		try {
			QueryManager queryManager = workspace.getQueryManager();
			Query query = queryManager.createQuery(queryString, "xpath");
			QueryResult result = query.execute();
			NodeIterator nodes = result.getNodes();
			while (nodes.hasNext()) {
				Node node = nodes.nextNode();
				Page page = pageManager.getPage(node.getPath());
				if (page != null) {
					Tag[] cqTags = page.getTags();
					for (Tag tag : cqTags) {
						String tagID = tag.getTagID();
						String tageTitle = tag.getTitle();
						fecets.put(tagID, tageTitle);
					}
				}
			}
		} catch (RepositoryException e) {
			LOGGER.error("RepositoryException in getAllFecets :: {}", e);
		}
		return fecets;
	}
	
	@ChildResource(name = "fieldNameMulti")
	private Resource multiDetailsFieldNameMultiResource;

	protected List<ArticleListModel> multiDetailsFieldNameMultiList;

	public List<ArticleListModel> getMultiFieldNameMultiList() {

		multiDetailsFieldNameMultiList = ResourceUtils.getListFromResource(multiDetailsFieldNameMultiResource, ArticleListModel.class);
		return multiDetailsFieldNameMultiList;
	}
}
