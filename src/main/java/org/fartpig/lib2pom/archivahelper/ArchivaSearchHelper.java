package org.fartpig.lib2pom.archivahelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.archiva.maven2.model.Artifact;
import org.apache.archiva.maven2.model.TreeEntry;
import org.apache.archiva.rest.api.model.SearchRequest;
import org.apache.archiva.rest.api.services.ArchivaRestServiceException;
import org.apache.archiva.rest.api.services.BrowseService;
import org.apache.archiva.rest.api.services.SearchService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.fartpig.lib2pom.entity.ArtifactObj;
import org.fartpig.lib2pom.entity.TreeEntryObj;
import org.fartpig.lib2pom.util.ArchivaUtil;
import org.fartpig.lib2pom.util.ToolLogger;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class ArchivaSearchHelper {

	// guest with an empty password
	public static String guestAuthzHeader = "Basic "
			+ org.apache.cxf.common.util.Base64Utility.encode(("guest" + ":").getBytes());

	// with an other login/password
	// public String authzHeader = "Basic "
	// + org.apache.cxf.common.util.Base64Utility.encode(("username" +
	// ":password").getBytes());
	// 需要尝试将账号密码加上
	// public String authzHeader = "Basic " +
	// org.apache.cxf.common.util.Base64Utility.encode(("guest" +
	// ":").getBytes());

	private String getBaseUrl() {
		return "http://192.111.25.32:9080/archiva/";
	}

	private String getRestServicesPath() {
		return "restServices";
	}

	// with an other login/password
	// public String authzHeader =
	// "Basic " + org.apache.cxf.common.util.Base64Utility.encode( ( "login" +
	// ":password" ).getBytes() );
	public List<ArtifactObj> searchByArtifactObj(ArtifactObj artifactObj) {
		SearchService searchService = getSearchServer();
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.setArtifactId(artifactObj.getArtifactId());
		searchRequest.setVersion(artifactObj.getVersion());
		searchRequest.setPackaging(artifactObj.getPackaging());
		List<ArtifactObj> result = new ArrayList<ArtifactObj>();

		try {
			List<Artifact> artifacts = searchService.searchArtifacts(searchRequest);
			for (Artifact aArtifact : artifacts) {
				ToolLogger.getInstance().info(aArtifact.toString());
				ArtifactObj aObj = new ArtifactObj();
				ArchivaUtil.copyArtifactToArtifactObj(aArtifact, aObj);
				result.add(aObj);
			}
		} catch (ArchivaRestServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public List<ArtifactObj> getFirstLevelTreeEntriesByArtifactObj(ArtifactObj artifactObj) {
		List<ArtifactObj> result = new ArrayList<ArtifactObj>();
		BrowseService browseService = getBrowseService(false);
		List<TreeEntry> treeEntries;
		try {
			treeEntries = browseService.getTreeEntries(artifactObj.getGroupId(), artifactObj.getArtifactId(),
					artifactObj.getVersion(), artifactObj.getRepositoryId());
			for (TreeEntry aEntry : treeEntries) {
				TreeEntryObj obj = new TreeEntryObj();
				ArtifactObj aObj = new ArtifactObj();
				ArchivaUtil.copyArtifactToArtifactObj(aEntry.getArtifact(), aObj);
				obj.setArtifact(aObj);

				for (TreeEntry aChildEntry : aEntry.getChilds()) {
					TreeEntryObj childObj = new TreeEntryObj();
					ArtifactObj aChildObj = new ArtifactObj();
					ArchivaUtil.copyArtifactToArtifactObj(aChildEntry.getArtifact(), aChildObj);
					childObj.setArtifact(aChildObj);
					obj.getChilds().add(childObj);

					result.add(aChildObj);
				}
			}
		} catch (ArchivaRestServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public SearchService getSearchServer() {
		SearchService service = JAXRSClientFactory.create(
				getBaseUrl() + "/" + getRestServicesPath() + "/archivaServices/", SearchService.class,
				Collections.singletonList(new JacksonJaxbJsonProvider()));
		// to add authentification
		if (guestAuthzHeader != null) {
			WebClient.client(service).header("Authorization", guestAuthzHeader);
		}
		// to configure read timeout
		WebClient.getConfig(service).getHttpConduit().getClient().setReceiveTimeout(100000000);
		// if you want to use json as exchange format xml is supported too
		WebClient.client(service).accept(MediaType.APPLICATION_JSON_TYPE);
		WebClient.client(service).type(MediaType.APPLICATION_JSON_TYPE);
		return service;
	}

	public BrowseService getBrowseService(boolean useXml) {
		// START SNIPPET: cxf-browseservice-creation
		BrowseService service = JAXRSClientFactory.create(
				getBaseUrl() + "/" + getRestServicesPath() + "/archivaServices/", BrowseService.class,
				Collections.singletonList(new JacksonJaxbJsonProvider()));
		// to add authentification
		if (guestAuthzHeader != null) {
			WebClient.client(service).header("Authorization", guestAuthzHeader);
		}
		// Set the Referer header to your archiva server url
		// WebClient.client(service).header("Referer","http://localhost:"+port);

		WebClient.getConfig(service).getHttpConduit().getClient().setReceiveTimeout(100000000);
		if (useXml) {
			WebClient.client(service).accept(MediaType.APPLICATION_XML_TYPE);
			WebClient.client(service).type(MediaType.APPLICATION_XML_TYPE);
		} else {
			WebClient.client(service).accept(MediaType.APPLICATION_JSON_TYPE);
			WebClient.client(service).type(MediaType.APPLICATION_JSON_TYPE);
		}
		return service;

	}

}
