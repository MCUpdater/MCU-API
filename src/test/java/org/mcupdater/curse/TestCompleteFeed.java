package org.mcupdater.curse;

import org.mcupdater.model.curse.feed.Project;
import org.mcupdater.model.curse.feed.Feed;
import org.mcupdater.model.curse.feed.FeedImporter;

import java.util.HashMap;
import java.util.Map;

public class TestCompleteFeed {

    public static void main(String[] args) {
        Feed complete = FeedImporter.getFeed(true);
        Map<String,Long> categories = new HashMap<>();
        for (Project entry : complete.getProjects()) {
            System.out.println(entry.getCategorySection().getName() + ": " + entry.getName());
            Long catCount =  categories.get(entry.getCategorySection().getName());
            if (catCount == null) {
                catCount = 1L;
            } else {
                catCount++;
            }
            categories.put(entry.getCategorySection().getName(),catCount);
        }
        System.out.println("============");
        for (Map.Entry<String, Long> category : categories.entrySet()) {
            System.out.println(category.getKey() + ": " + category.getValue().toString());
        }
    }
}
