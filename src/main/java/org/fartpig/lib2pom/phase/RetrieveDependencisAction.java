package org.fartpig.lib2pom.phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.fartpig.lib2pom.archivahelper.ArchivaSearchHelper;
import org.fartpig.lib2pom.constant.GlobalConst;
import org.fartpig.lib2pom.entity.ArtifactObj;
import org.fartpig.lib2pom.util.ArchivaUtil;
import org.fartpig.lib2pom.util.ToolLogger;

// 根据信息调用接口获取依赖关系和完善自身信息，可能有会返回多个需要解决冲突
public class RetrieveDependencisAction {

	private static String CURRENT_PHASE = GlobalConst.PHASE_RETRIEVE_DEPENDENCIS;

	private static int PRIORITY_MAX = 999;
	private static String[] REPOSITORY_PRIORITY = { "internal", "central" };

	private int reposityPriorityIndex(String repositoryId) {
		for (int i = 0; i < REPOSITORY_PRIORITY.length; i++) {
			if (REPOSITORY_PRIORITY[i].equals(repositoryId)) {
				return i;
			}
		}
		return PRIORITY_MAX;
	}

	public RetrieveDependencisAction() {
		ToolLogger log = ToolLogger.getInstance();
		log.setCurrentPhase(CURRENT_PHASE);
	}

	public List<ArtifactObj> retrieveDependencis(List<ArtifactObj> artifactObjs) {
		List<ArtifactObj> extraArtifactObjs = new ArrayList<ArtifactObj>();
		Set<String> resolveArtifactSet = new HashSet<String>();

		ArchivaSearchHelper archivaSearchHelper = new ArchivaSearchHelper();
		// 先解析自身，然后通过自身获取所依赖的artifact信息， 并将依赖的结果返回
		for (ArtifactObj aObj : artifactObjs) {
			List<ArtifactObj> searchResult = archivaSearchHelper.searchByArtifactObj(aObj);
			if (searchResult.size() != 0) {
				for (ArtifactObj aResult : searchResult) {
					if (aResult.getArtifactId().equals(aObj.getArtifactId())
							&& aResult.getVersion().equals(aObj.getVersion())) {
						// priority override
						int newIndex = reposityPriorityIndex(aResult.getRepositoryId());
						int oldIndex = reposityPriorityIndex(aObj.getRepositoryId());
						if (newIndex < oldIndex) {
							ArtifactObj firstObj = aResult;
							aObj.setArtifactId(firstObj.getArtifactId());
							aObj.setClassifier(firstObj.getClassifier());
							aObj.setGroupId(firstObj.getGroupId());
							aObj.setPackaging(firstObj.getPackaging());
							aObj.setVersion(firstObj.getVersion());
							aObj.setRepositoryId(firstObj.getRepositoryId());
							aObj.setResolve(true);
							ToolLogger.getInstance().info("resolve:" + aObj.formateFileName());
						}

						resolveArtifactSet.add(aObj.uniqueName());
					}
				}
			} else {
				aObj.setScope("system");
			}
		}

		Queue<ArtifactObj> artifactObjQueue = new LinkedList<ArtifactObj>();
		for (ArtifactObj aObj : artifactObjs) {
			artifactObjQueue.add(aObj);
		}

		while (!artifactObjQueue.isEmpty()) {
			ArtifactObj aObj = artifactObjQueue.poll();

			if (aObj.isResolve()) {
				List<ArtifactObj> childArtifactObjs = archivaSearchHelper.getFirstLevelTreeEntriesByArtifactObj(aObj);
				for (ArtifactObj aArtifactObj : childArtifactObjs) {
					aArtifactObj.setResolve(true);
					if (!resolveArtifactSet.contains(aArtifactObj.uniqueName())) {
						extraArtifactObjs.add(aArtifactObj);
						artifactObjQueue.add(aArtifactObj);
						ToolLogger.getInstance().info("resolve : " + aArtifactObj.formateFileName() + " get dependency:"
								+ aObj.formateFileName());
					}
				}
				ArchivaUtil.fillDependencisToArtifactObj(aObj, childArtifactObjs);
			}
		}

		return extraArtifactObjs;
	}
}