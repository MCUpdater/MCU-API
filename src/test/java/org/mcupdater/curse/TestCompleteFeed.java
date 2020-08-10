package org.mcupdater.curse;

import org.mcupdater.model.curse.feed.Project;
import org.mcupdater.model.curse.feed.Feed;
import org.mcupdater.model.curse.feed.FeedImporter;

import java.util.HashMap;
import java.util.Map;

public class TestCompleteFeed {

    public static void main(String[] args) {
        long tsStart = System.currentTimeMillis();
        Feed complete = FeedImporter.getFeed(true);
        System.out.println("Feed import time: " + (System.currentTimeMillis()-tsStart)/1000D + " sec");
        System.out.println("Timestamp: " + complete.getTimestamp());
        Map<String,Long> categories = new HashMap<>();
        for (Project entry : complete.getProjects()) {
            System.out.println(entry.getId() + ": " + entry.getName() + " (" + entry.getLatestFiles().get(0).getDownloadURL() + ")");
        }
        System.out.println("============");
    }
}
