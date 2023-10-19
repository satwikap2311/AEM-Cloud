package com.aem.avengers.core.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

public class ResourceUtils  extends ResourceUtil{
	public static List<Resource> getChildrenWithResourceType(Resource resource, String resourceType) {
		List<Resource> foundResources = new LinkedList<>();
		Iterator<Resource> children = resource.listChildren();
		while (children.hasNext()) {
			Resource child = children.next();
			if (child.getResourceType().equals(resourceType)) {
				foundResources.add(child);
			}
			foundResources.addAll(getChildrenWithResourceType(child, resourceType));
		}
		return foundResources;
	}
}
