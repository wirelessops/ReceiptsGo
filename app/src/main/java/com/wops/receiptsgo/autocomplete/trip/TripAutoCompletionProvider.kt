package com.wops.receiptsgo.autocomplete.trip

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.autocomplete.AutoCompletionProvider
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.persistence.database.controllers.TableController
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
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