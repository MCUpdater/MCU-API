package org.mcupdater.curse;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record CurseFile(Data data) {

	record Data(
			Integer id,
			Integer gameId,
			Integer modId,
			Boolean isAvailable,
			String displayName,
			String fileName,
			Integer releaseType,
			Integer fileStatus,
			List<Hash> hashes,
			String fileDate,
			Long fileLength,
			Long downloadCount,
			Long fileSizeOnDisk,
			String downloadUrl,
			List<String> gameVersions,
			List<SortableGameVersion> sortableGameVersions,
			List<Dependency> dependencies,
			Boolean exposeAsAlternative,
			Integer parentProjectFileId,
			Integer alternateFileId,
			Boolean isServerPack,
			Integer serverPackFileId,
			Boolean isEarlyAccessContent,
			String earlyAccessEndDate,
			Long fileFingerprint,
			List<Module> modules
	) {
		public String getDownloadUrl() {
			return downloadUrl != null ? downloadUrl() : "https://edge.forgecdn.net/files/" + new StringBuilder(this.id().toString()).insert(this.id().toString().length()-3,"/").toString() + "/" + this.fileName;
		}
	}

	record Hash(
			String value,
			Integer algo
	) {}

	record SortableGameVersion(
			String gameVersionName,
			String gameVersionPadded,
			String gameVersion,
			String gameVersionReleaseDate,
			Integer gameVersionTypeId
	) {}

	record Dependency(
			Integer modId,
			Integer dependencyType
	) {}

	record Module(
			String name,
			Long fingerprint
	) {}
}
