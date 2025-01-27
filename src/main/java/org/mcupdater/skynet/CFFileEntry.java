package org.mcupdater.skynet;

public record CFFileEntry(Integer projectid, Integer fileid, String filename, String md5, Integer size) {

	public String getUrl() {
		String fileidString = fileid.toString();
		return "http://edge.forgecdn.net/files/" + new StringBuffer(fileidString).insert(fileidString.length() - 3, "/") + "/" + filename;
	}
}
