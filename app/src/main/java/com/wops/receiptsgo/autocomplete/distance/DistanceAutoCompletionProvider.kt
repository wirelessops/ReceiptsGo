package com.wops.receiptsgo.autocomplete.distance

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.autocomplete.AutoCompletionProvider
import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.persistence.database.controllers.TableController
import com.wops.receiptsgo.persistence.database.controllers.impl.DistanceTableController
import java.util.*
import javax.inject.Inject

/**
 * Implements the [AutoCompletionProvider] contract for [Distance] instances
 */
@ApplicationScope
class DistanceAutoCompletionProvider(override val autoCompletionType: Class<Distance>,
                                     override val tableController: TableController<Distance>,
                                     override val supportedAutoCompleteFields: List<AutoCompleteField>) : AutoCompletionProvider<Distance> {

    @Inject
    constructor(tableController: DistanceTableController) :
            this(Distance::class.java, tableController, Arrays.asList(DistanceAutoCompleteField.Location, DistanceAutoCompleteField.Comment))

}