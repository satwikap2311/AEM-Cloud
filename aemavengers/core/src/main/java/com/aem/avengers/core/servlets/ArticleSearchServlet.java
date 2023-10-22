package com.aem.avengers.core.servlets;

import java.io.IOException;
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

import com.aem.avengers.core.services.AvengersBasicService;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.constants.NameConstants;
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
		String typeKeyword = request.getParameter("fulltext");
		String currentHit = request.getParameter("currentHit");

		try {
			QueryManager queryManager = workspace.getQueryManager();
			String nodePath = "";
			String pagePath = "";
			String maxItems = "";
			int keyWordOffSet = 0;
			if (StringUtils.isNotBlank(typeKeyword)) {
				typeKeyword = typeKeyword.toLowerCase();
				nodePath = request.getRequestPathInfo().getSuffix();
				String resultsOffset = request.getParameter("resultsOffset");
				if (StringUtils.isNotBlank(resultsOffset)) {
					keyWordOffSet = Integer.valueOf(resultsOffset);
				}
				Resource searchRes = resourceResolver.getResource(nodePath);
				Node searchNode = searchRes.adaptTo(Node.class);
				LOGGER.info("searchNode :: {}", searchNode);
				if (searchNode.hasProperty("searchRoot")) {
					pagePath = searchNode.getProperty("searchRoot").getValue().getString();
				}
			} else {
				nodePath = request.getParameter("listComponentNode");
				Resource listRes = resourceResolver.getResource(nodePath);
				Node listNode = listRes.adaptTo(Node.class);
				LOGGER.info("listNode :: {}", listNode);
				if (listNode.hasProperty("maxItems")) {
					maxItems = listNode.getProperty("maxItems").getValue().getString();
				}
				if (listNode.hasProperty("parentPage")) {
					pagePath = listNode.getProperty("parentPage").getValue().getString();
				}
			}

			String queryString = "/jcr:root".concat(pagePath).concat("//element(*, cq:Page)");
			LOGGER.info("searchKeyword :: {}", searchKeyword);
			if (StringUtils.isNotBlank(searchKeyword)) {
				queryString = queryString.concat("[jcr:like(fn:lower-case(jcr:content/@author), '%" + searchKeyword
						+ "%')" + " or jcr:like(fn:lower-case(jcr:content/@" + JcrConstants.JCR_TITLE + "), '%"
						+ searchKeyword + "%')]");
			}
			LOGGER.info("queryString :: {}", queryString);
			Query query = queryManager.createQuery(queryString, "xpath");
			List<String> keyWordList = new LinkedList<String>();
			QueryResult result = query.execute();
			NodeIterator nodes = result.getNodes();
			long size = nodes.getSize();
			nodes = query.execute().getNodes();
			JsonArray jsonArr = new JsonArray();
			if (StringUtils.isNotBlank(searchKeyword)) {
				JsonObject sizeObj = new JsonObject();
				sizeObj.addProperty("articlesSize", size);
				jsonArr.add(sizeObj);
				Integer hit = 0;
				if (StringUtils.isNotBlank(currentHit)) {
					hit = Integer.valueOf(currentHit);
				}
				int maxItemsInt = 0;
				if (StringUtils.isNotBlank(maxItems)) {
					maxItemsInt = Integer.valueOf(maxItems);
				}

				int limit = maxItemsInt * hit;
				int offset = limit - maxItemsInt;

				LOGGER.info("limit :: {}", limit);
				LOGGER.info("offset :: {}", offset);
				query.setLimit(limit);
				query.setOffset(offset);
				nodes = query.execute().getNodes();
			}
			while (nodes.hasNext()) {
				Node node = nodes.nextNode();
				Page page = pageManager.getPage(node.getPath());
				String tageTitle = "";
				if (page != null) {
					/*
					 * Tag[] cqTags = page.getTags(); for (Tag tag : cqTags) { tageTitle =
					 * tag.getTitle().toLowerCase(); }
					 */
				}
				String pageTitle = page.getTitle().toLowerCase();
				ValueMap pageProperties = page.getProperties();
				String author = "";
				if (pageProperties.containsKey("author")) {
					author = pageProperties.get("author", String.class).toLowerCase();
				}
				if (StringUtils.isNotBlank(searchKeyword)) {
					jsonArr.add(avengersBasicService.addPageProperties(page));
				}
				if (StringUtils.isNotBlank(typeKeyword)) {
					if (StringUtils.isNotBlank(author) && author.startsWith(typeKeyword)) {
						if (!keyWordList.contains(author)) {
							keyWordList.add(author);
						}
					}
					/*
					 * if (StringUtils.isNotBlank(tageTitle) && tageTitle.startsWith(typeKeyword)) {
					 * if (!keyWordList.contains(tageTitle)) { keyWordList.add(tageTitle); } }
					 */
					if (StringUtils.isNotBlank(pageTitle) && pageTitle.startsWith(typeKeyword)) {
						if (!keyWordList.contains(pageTitle)) {
							keyWordList.add(pageTitle);
						}
					}

				}
			}
			LOGGER.info("keyWordList :: {}", keyWordList);
			LOGGER.info("keyWordList size :: {}", keyWordList.size());
			LOGGER.info("keyWordOffSet :: {}", keyWordOffSet);
			int keyWordsLimit = keyWordOffSet + 10;
			LOGGER.info("keyWordsLimit :: {}", keyWordsLimit);
			if (keyWordList.size() < keyWordsLimit) {
				keyWordsLimit = keyWordList.size();
			}
			for (int i = keyWordOffSet; i < keyWordsLimit; i++) {
				if (keyWordList.size() >= keyWordOffSet) {
					String keyword = keyWordList.get(i);
					JsonObject json = new JsonObject();
					json.addProperty("title", keyword);
					jsonArr.add(json);
				}
			}
			LOGGER.info("jsonArr :: {}", jsonArr);
			response.getWriter().write(jsonArr.toString());
		} catch (RepositoryException e) {
			LOGGER.error("RepositoryException in doGet :: {}", e);
		}
	}

}
