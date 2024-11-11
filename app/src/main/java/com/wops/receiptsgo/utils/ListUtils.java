package com.wops.receiptsgo.utils;

import androidx.annotation.NonNull;

import java.util.List;

public final class ListUtils {

    /**
     * Replaces an item in a list with a new item (at the same position as the original)
     *
     * @param list the {@link List} from which the replace operation will be performed
     * @param oldItem the old item
     * @param newItem the new item that will take the old items place at it's position
     * @return true if the operation completed successfully
     */
    public static <T> boolean replace(@NonNull List<T> list, @NonNull T oldItem, @NonNull T newItem) {
        final int id = list.indexOf(oldItem);
        if (id >= 0) {
            list.remove(id);
            list.add(id, newItem);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes the last element from a list
     *
     * @param list the {@link List} from which the last element will be removed
     * @return the last element in the list of type {@link T} or {@code null} if there is no last element (i.e. the list is already empty)
     */
    public static <T> T removeLast(@NonNull List<T> list) {
        if (!list.isEmpty()) {
            return list.remove(list.size() - 1);
        } else {
            return null;
        }
    }
}
