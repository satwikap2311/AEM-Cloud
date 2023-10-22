package com.aem.avengers.core.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

import com.adobe.cq.wcm.core.components.models.Teaser;
import com.aem.avengers.core.services.AvengersBasicService;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.constants.NameConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component(service = { javax.servlet.Servlet.class }, property = { "sling.servlet.selectors=articlesList",
		"sling.servlet.resourceTypes=cq/Page", "sling.servlet.methods=GET", "sling.servlet.extensions=" + "json" })
public class ArticlesListServlet extends SlingAllMethodsServlet {

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
		String facets = request.getParameter("facets");
		String nodePath = request.getParameter("listComponentNode");
		String currentHit = request.getParameter("currentHit");

		try {
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

			int limit = 0;
			int offset = 0;
			if (StringUtils.isNotBlank(currentHit)) {
				int hit = Integer.valueOf(currentHit);
				int maxItemsInt = 0;
				if (StringUtils.isNotBlank(maxItems)) {
					maxItemsInt = Integer.valueOf(maxItems);
				}
//			int offset = (hit) * maxItemsInt;
//			int endValue = offset + maxItemsInt;

				limit = maxItemsInt * hit;
				offset = limit - maxItemsInt;
				LOGGER.info("limit :: {}", limit);
				LOGGER.info("offset :: {}", offset);
			}

			String queryString = "/jcr:root".concat(parentPage).concat("//element(*, cq:Page)");
			LOGGER.info("queryString :: {}", queryString);
			if (StringUtils.isNotBlank(facets)) {
				queryString = queryString.concat("[");
				List<String> facetslist = Arrays.asList(facets.split(","));
				for (int i = 0; i < facetslist.size(); i++) {
					if (i == 0) {
						queryString = queryString.concat(
								"jcr:like(jcr:content/@" + NameConstants.PN_TAGS + ", '%" + facetslist.get(i) + "%')");
					} else {
						queryString = queryString.concat(" or jcr:like(jcr:content/@" + NameConstants.PN_TAGS + ", '%"
								+ facetslist.get(i) + "%')");
					}
				}
				queryString = queryString.concat("]");
			}
			LOGGER.info("queryString :: {}", queryString);

			QueryManager queryManager = workspace.getQueryManager();
			Query query = queryManager.createQuery(queryString, "xpath");
			NodeIterator nodes = query.execute().getNodes();
			long size = nodes.getSize();
			JsonArray jsonArr = new JsonArray();
			JsonObject sizeObj = new JsonObject();
			sizeObj.addProperty("articlesSize", size);
			jsonArr.add(sizeObj);
			if (StringUtils.isNotBlank(currentHit)) {
				query.setOffset(offset);
				query.setLimit(limit);
				nodes = query.execute().getNodes();
				LOGGER.info("nodes :: {}", nodes);

//			int loopval = 0;
				while (nodes.hasNext()) {
//				JsonObject jsonObj = new JsonObject();
					Node node = nodes.nextNode();
					String pagePath = node.getPath();
					if (StringUtils.isNotBlank(pagePath) && pagePath.contains("/" + JcrConstants.JCR_CONTENT)) {
						pagePath = pagePath.replace(("/" + JcrConstants.JCR_CONTENT), "");

					}
					LOGGER.info("node :: {}", pagePath);
					Page page = pageManager.getPage(pagePath);
					LOGGER.info("page :: {}", page.getPath());
					if (page != null && !pagePath.equals(parentPage)) {
						jsonArr.add(avengersBasicService.addPageProperties(page));
//					LOGGER.info("jsonObj :: {}", jsonObj);
					}
				}
			}

			/*
			 * for (int i = offset; i <= endValue; i++) { String pagePath = pageList.get(i);
			 * JsonObject jsonObj = new JsonObject(); boolean isFacet = false; Page page =
			 * pageManager.getPage(pagePath); if (page != null) { Tag[] cqTags =
			 * page.getTags(); for (Tag tag : cqTags) { String tagID = tag.getTagID(); for
			 * (String facet : facetsArr) { if (tagID.equals(facet)) { isFacet = true;
			 * break; } } } if (isFacet) { String pageTitle = page.getTitle();
			 * jsonObj.addProperty("pageTitle", pageTitle); ValueMap pageProperties =
			 * page.getProperties(); if (pageProperties.containsKey("author")) { String
			 * author = pageProperties.get("author", String.class);
			 * jsonObj.addProperty("author", author); } if
			 * (pageProperties.containsKey("date")) { SimpleDateFormat simpleFormat = new
			 * SimpleDateFormat("dd/MM/yyyy"); Date date = pageProperties.get("date",
			 * Date.class); String formatedDate = simpleFormat.format(date);
			 * jsonObj.addProperty("date", formatedDate); }
			 * 
			 * } } if (null != jsonObj) { jsonArr.add(jsonObj); // loopval++; } }
			 */

			/*
			 * while (nodes.hasNext()) { Node node = nodes.nextNode(); JsonObject jsonObj =
			 * new JsonObject(); Page page = pageManager.getPage(node.getPath()); if (page
			 * != null) { if (jsonArr.size() > 0) { boolean isFacet = false; Tag[] cqTags =
			 * page.getTags(); for (Tag tag : cqTags) { String tagID = tag.getTagID(); for
			 * (String facet : facetsArr) { if (tagID.equals(facet)) { isFacet = true;
			 * break; } } } if (isFacet) { jsonObj =
			 * avengersBasicService.addPageProperties(jsonObj, page); } } else { jsonObj =
			 * avengersBasicService.addPageProperties(jsonObj, page); } if (null != jsonObj)
			 * { jsonArr.add(jsonObj); } }
			 * 
			 * }
			 */

			response.getWriter().write(jsonArr.toString());
		} catch (RepositoryException e) {
			LOGGER.error("RepositoryException in doGet :: {}", e);
		}

	}

}
