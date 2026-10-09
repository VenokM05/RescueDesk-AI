package com.rescuedesk.ai.ui

import androidx.annotation.StringRes
import com.rescuedesk.ai.R
import com.rescuedesk.ai.data.local.TextSize
import com.rescuedesk.ai.domain.model.GoBagCategories

/**
 * Shared label mapping for enum / stable-key values that live in the data and
 * domain layers but are rendered in the UI (FR-07): the strings themselves are
 * resources so they follow the selected app language.
 */

@StringRes
fun TextSize.labelRes(): Int = when (this) {
    TextSize.NORMAL -> R.string.text_size_normal
    TextSize.LARGE -> R.string.text_size_large
    TextSize.EXTRA_LARGE -> R.string.text_size_extra_large
}

/**
 * Localized header for a go-bag category key. Unknown keys return null and are
 * rendered as stored (user-authored) text instead of failing the list.
 */
@StringRes
fun goBagCategoryLabelRes(key: String): Int? = when (key) {
    GoBagCategories.WATER -> R.string.gobag_cat_water
    GoBagCategories.FIRST_AID -> R.string.gobag_cat_first_aid
    GoBagCategories.LIGHTING -> R.string.gobag_cat_lighting
    GoBagCategories.DOCUMENTS -> R.string.gobag_cat_documents
    GoBagCategories.CLOTHING -> R.string.gobag_cat_clothing
    GoBagCategories.SPECIAL -> R.string.gobag_cat_special
    else -> null
}
