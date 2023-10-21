package com.aem.avengers.core.servlets;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.avengers.core.services.AvengersBasicService;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component(service = { javax.servlet.Servlet.class }, property = { "sling.servlet.selectors=articleSearch",
		"sling.servlet.resourceTypes=cq/Page", "sling.servlet.methods=GET", "sling.servlet.extensions=" + "json" })
public class ArticleSearchServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ArticlesListServlet.class);

	@Reference
	AvengersBasicService avengersBasicService;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		ResourceResolver resourceResolver = request.getResourceResolver();
		PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
		Session session = resourceResolver.adaptTo(Session.class);
		Workspace workspace = session.getWorkspace();
		String searchKeyword = request.getParameter("searchKeyword");
		String typeKeyword = request.getParameter("typeKeyword");
		String nodePath = request.getParameter("listComponentNode");
		String currentHit = request.getParameter("currentHit");

		try {
			QueryManager queryManager = workspace.getQueryManager();
			Resource list = resourceResolver.getResource(nodePath);
			Node listNode = list.adaptTo(Node.class);
			LOGGER.info("listNode :: {}", listNode);
			String maxItems = "";
			if (listNode.hasProperty("maxItems")) {
				maxItems = listNode.getProperty("maxItems").getString();
			}
			String parentPage = "";
			if (listNode.hasProperty("parentPage")) {
				parentPage = listNode.getProperty("parentPage").getString();
			}

			int hit = Integer.valueOf(currentHit);
			int maxItemsInt = 0;
			if (StringUtils.isNotBlank(maxItems)) {
				maxItemsInt = Integer.valueOf(maxItems);
			}
//			int offset = (hit) * maxItemsInt;
//			int endValue = offset + maxItemsInt;

			int limit = maxItemsInt * hit;
			int offset = limit - maxItemsInt;

			LOGGER.info("limit :: {}", limit);
			LOGGER.info("offset :: {}", offset);
			String queryString = "/jcr:root".concat(parentPage).concat("//element(*, cq:Page)");
			Query query = queryManager.createQuery(queryString, "xpath");
			QueryResult result = query.execute();
			NodeIterator nodes = result.getNodes();
			String contentStr = "";
			JsonArray jsonArr = new JsonArray();
			while (nodes.hasNext()) {
				Node node = nodes.nextNode();
				JsonObject jsonObj = new JsonObject();
				Page page = pageManager.getPage(node.getPath());
				String tageName = "";
				if (page != null) {
					Tag[] cqTags = page.getTags();
					for (Tag tag : cqTags) {
						String tagID = tag.getTagID();
						tageName = tag.getName();
						contentStr = contentStr.concat(tagID + " ");
					}
				}
				String pageTitle = page.getTitle();
				contentStr = contentStr.concat(pageTitle);
				ValueMap pageProperties = page.getProperties();
				String author = "";
				if (pageProperties.containsKey("author")) {
					author = pageProperties.get("author", String.class);
					contentStr = contentStr.concat(author);
				}
				if (StringUtils.isNotBlank(searchKeyword) && StringUtils.isNotBlank(contentStr)
						&& StringUtils.isNotBlank(searchKeyword)
						&& StringUtils.containsIgnoreCase(contentStr, searchKeyword)) {
					jsonArr.add(avengersBasicService.addPageProperties(page));
				} else if (StringUtils.isNotBlank(typeKeyword)) {
					if (StringUtils.isNotBlank(author) && author.startsWith(typeKeyword)) {
						jsonArr.add(author.toLowerCase());
					}
					if (StringUtils.isNotBlank(tageName) && tageName.startsWith(typeKeyword)) {
						jsonArr.add(tageName.toLowerCase());
					}
					if (StringUtils.isNotBlank(pageTitle) && pageTitle.startsWith(typeKeyword)) {
						jsonArr.add(pageTitle.toLowerCase());
					}

				}
			}

			response.getWriter().write(jsonArr.toString());
		} catch (RepositoryException e) {
			LOGGER.error("RepositoryException in doGet :: {}", e);
		}
	}

}
