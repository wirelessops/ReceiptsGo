package co.smartreceipts.android.autocomplete.trip

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompletionProvider
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.database.controllers.TableController
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import java.util.*
import javax.inject.Inject

/**
 * Implements the [AutoCompletionProvider] contract for [Trip] instances
 */
@ApplicationScope
class TripAutoCompletionProvider(override val autoCompletionType: Class<Trip>,
                                 override val tableController: TableController<Trip>,
                                 override val supportedAutoCompleteFields: List<AutoCompleteField>) : AutoCompletionProvider<Trip> {

    @Inject
    constructor(tableController: TripTableController) :
            this(Trip::class.java, tableController, Arrays.asList(TripAutoCompleteField.Name, TripAutoCompleteField.Comment, TripAutoCompleteField.CostCenter))

}