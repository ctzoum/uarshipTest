/* Copyright 2016 Urban Airship and Contributors */

package com.urbanairship.push;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.urbanairship.Logger;
import com.urbanairship.json.JsonValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class containing utility methods related to tags.
 */
class TagUtils {

    private static final int MAX_TAG_LENGTH = 127;

    /**
     * Converts a JSONValue to a Tags Map
     */
    @NonNull
    static Map<String, Set<String>> convertToTagsMap(JsonValue jsonValue) {
        Map<String, Set<String>> tagGroups = new HashMap<>();

        if (jsonValue != null && jsonValue.isJsonMap()) {
            for (Map.Entry<String, JsonValue> groupEntry : jsonValue.getMap()) {
                Set<String> tags = new HashSet<>();
                for (JsonValue tag : groupEntry.getValue().getList()) {
                    if (tag.isString()) {
                        tags.add(tag.getString());
                    }
                }
                if (!tags.isEmpty()) {
                    tagGroups.put(groupEntry.getKey(), tags);
                }
            }
        }

        return tagGroups;
    }

    /**
     * Normalizes a set of tags. Each tag will be trimmed of white space and any tag that
     * is empty, null, or exceeds {@link #MAX_TAG_LENGTH} will be dropped.
     *
     * @param tags The set of tags to normalize.
     * @return The set of normalized, valid tags.
     */
    @NonNull
    static Set<String> normalizeTags(@NonNull Set<String> tags) {
        Set<String> normalizedTags = new HashSet<>();

        for (String tag : tags) {
            if (tag == null) {
                Logger.debug("Null tag was removed from set.");
                continue;
            }

            tag = tag.trim();
            if (tag.length() <= 0 || tag.length() > MAX_TAG_LENGTH) {
                Logger.error("Tag with zero or greater than max length was removed from set: " + tag);
                continue;
            }

            normalizedTags.add(tag);
        }

        return normalizedTags;
    }

    /**
     * Combine the tags from bundle with the pending tags.
     *
     * @param tagsBundle The tags bundle.
     * @param tagsToAdd The pending tags to add tags to.
     * @param tagsToRemove The pending tags to remove tags from.
     */
    static void combineTagGroups(Bundle tagsBundle, Map<String, Set<String>> tagsToAdd, Map<String, Set<String>> tagsToRemove) {
        if (tagsBundle == null) {
            return;
        }

        for (String group : tagsBundle.keySet()) {
            List<String> tags = tagsBundle.getStringArrayList(group);

            if (tags == null) {
                continue;
            }

            // Add tags to tagsToAdd.
            if (tagsToAdd.containsKey(group)) {
                tagsToAdd.get(group).addAll(tags);
            } else {
                tagsToAdd.put(group, new HashSet<>(tags));
            }

            // Remove tags from tagsToRemove.
            if (tagsToRemove.containsKey(group)) {
                tagsToRemove.get(group).removeAll(tags);
            }
        }
    }
}
