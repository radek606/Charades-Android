package com.ick.kalambury.list

import com.ick.kalambury.R
import com.ick.kalambury.TableKind

enum class ListType(
    val viewId: Int,
    val itemMode: ItemMode,
    val key: String?,
    val isWithAds: Boolean
) {
    CATEGORY_DEFAULT(R.layout.list_item_words_set,
        ItemMode.DEFAULT,
        null,
        false),
    CATEGORY_SELECTABLE(R.layout.list_item_words_set,
        ItemMode.SELECTABLE,
        null,
        false),
    PLAYER_DEFAULT(R.layout.list_item_player_score,
        ItemMode.DEFAULT,
        null,
        false),
    PLAYER_SELECTABLE(R.layout.list_item_player_score,
        ItemMode.SELECTABLE,
        null,
        false),
    TABLE_DEFAULT(R.layout.list_item_table,
        ItemMode.DEFAULT,
        TableKind.DEFAULT.name,
        true),
    TABLE_PUBLIC(R.layout.list_item_table,
        ItemMode.DEFAULT,
        TableKind.PUBLIC.name,
        true),
    DEVICE(R.layout.list_item_device,
        ItemMode.DEFAULT,
        null,
        false),
    MESSAGE(R.layout.list_item_message,
        ItemMode.DEFAULT,
        null,
        false);

    enum class ItemMode {
        DEFAULT, SELECTABLE
    }
}