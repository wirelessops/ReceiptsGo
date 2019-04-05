package co.smartreceipts.android.autocomplete.distance

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompletionProvider
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.persistence.database.controllers.TableController
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController
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