package co.smartreceipts.android.identity.organization

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.apis.organizations.AppSettings
import co.smartreceipts.android.identity.apis.organizations.Configurations
import co.smartreceipts.android.model.*
import co.smartreceipts.android.persistence.database.controllers.TableController
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController
import co.smartreceipts.android.persistence.database.controllers.impl.PaymentMethodsTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function5
import javax.inject.Inject


@ApplicationScope
class AppSettingsSynchronizer @Inject constructor(
    private val categoriesTableController: CategoriesTableController,
    private val paymentMethodsTableController: PaymentMethodsTableController,
    private val csvTableController: CSVTableController,
    private val pdfTableController: PDFTableController,
    private val preferencesSynchronizer: AppPreferencesSynchronizer
) {

    /**
     * @return Single that emits current app's AppSettings
     */
    internal fun getCurrentAppSettings(): Single<AppSettings> {

        return Single.zip(
            preferencesSynchronizer.getAppPreferences(),
            categoriesTableController.get(),
            paymentMethodsTableController.get(),
            csvTableController.get(),
            pdfTableController.get(),
            Function5<Map<String, Any>, List<Category>, List<PaymentMethod>, List<Column<Receipt>>, List<Column<Receipt>>, AppSettings> { preferencesMap, categories, paymentMethods, csvColumns, pdfColumns ->
                AppSettings(
                    Configurations(), // TODO: 18.02.2019 Configurations are not defined in the app
                    preferencesMap,
                    categories,
                    paymentMethods,
                    csvColumns,
                    pdfColumns
                )
            }
        )

    }

    /**
     * @return Single that emits {true} if argument categories match app categories, else emits {false}
     */
    internal fun checkCategoriesMatch(categories: List<Category>): Single<Boolean> {

        return checkListMatch(
            categories,
            categoriesTableController
        ) { c1, c2 -> c1.uuid == c2.uuid && c1.name == c2.name && c1.code == c2.code }
    }

    internal fun applyCategories(categories: List<Category>): Completable {
        return applyList(
            categories,
            categoriesTableController
        ) { c1, c2 -> c1.uuid == c2.uuid && c1.name == c2.name && c1.code == c2.code }
    }

    /**
     * @return Single that emits {true} if argument payment methods match app payment methods, else emits {false}
     */
    internal fun checkPaymentMethodsMatch(paymentMethods: List<PaymentMethod>): Single<Boolean> {
        return checkListMatch(paymentMethods, paymentMethodsTableController) { p1, p2 -> p1.uuid == p2.uuid && p1.method == p2.method }
    }

    internal fun applyPaymentMethods(paymentMethods: List<PaymentMethod>): Completable {
        return applyList(paymentMethods, paymentMethodsTableController) { p1, p2 -> p1.uuid == p2.uuid && p1.method == p2.method }
    }

    /**
     * @return Single that emits {true} if argument csv columns match app csv columns, else emits {false}
     */
    internal fun checkCsvColumnsMatch(csvColumns: List<Column<Receipt>>): Single<Boolean> {
        return checkListMatch(csvColumns, csvTableController) { col1, col2 -> col1.uuid == col2.uuid && col1.type == col2.type }
    }

    internal fun applyCsvColumns(csvColumns: List<Column<Receipt>>): Completable {
        return applyList(csvColumns, csvTableController) { col1, col2 -> col1.uuid == col2.uuid && col1.type == col2.type }
    }

    /**
     * @return Single that emits {true} if argument pdf columns match app pdf columns, else emits {false}
     */
    internal fun checkPdfColumnsMatch(pdfColumns: List<Column<Receipt>>): Single<Boolean> {
        return checkListMatch(pdfColumns, pdfTableController) { col1, col2 -> col1.uuid == col2.uuid && col1.type == col2.type }
    }

    internal fun applyPdfColumns(pdfColumns: List<Column<Receipt>>): Completable {
        return applyList(pdfColumns, pdfTableController) { col1, col2 -> col1.uuid == col2.uuid && col1.type == col2.type }
    }


    internal fun checkOrganizationPreferencesMatch(organizationSettings: Map<String, Any?>): Single<Boolean> {
        return preferencesSynchronizer.checkOrganizationPreferencesMatch(organizationSettings)
    }

    internal fun applyOrganizationPreferences(organizationSettings: Map<String, Any?>): Completable {
        return preferencesSynchronizer.applyOrganizationPreferences(organizationSettings)
    }


    private fun <T> checkListMatch(organizationItems: List<T>, tableController: TableController<T>, equals: (o1: T, o2: T) -> Boolean)
            : Single<Boolean> {
        return tableController.get()
            .flatMap { appItems ->
                Logger.debug(this, "Comparing app settings with organization...")
                for (organizationItem in organizationItems) {
                    var foundSame = false

                    for (appItem in appItems) {
                        if (equals(appItem, organizationItem)) {
                            foundSame = true
                            Logger.debug(this, "Found same item: {}", appItem)
                            break
                        }
                    }

                    if (!foundSame) {
                        Logger.debug(this, "Didn't find item: {}", organizationItem)
                        return@flatMap Single.just(false)
                    }
                }

                Single.just(true)
            }
    }

    private fun <T : Keyed> applyList(organizationItems: List<T>, tableController: TableController<T>, equals: (o1: T, o2: T) -> Boolean)
            : Completable {

        return tableController.get()
            .flatMapCompletable { appItems ->
                for (organizationItem in organizationItems) {
                    var found = false

                    for (appItem in appItems) {

                        if (equals(appItem, organizationItem)) { // app contains same item
                            found = true
                            Logger.debug(this, "Found organization's item: {}", appItem)
                            break
                        }

                        if (appItem.uuid == organizationItem.uuid) { // app contains changed item, need to update it
                            found = true
                            Logger.debug(this, "Found changed organization's item: {}, updating...", appItem)
                            tableController.update(appItem, organizationItem, DatabaseOperationMetadata())
                            break
                        }
                    }

                    if (!found) { // app doesn't contain this item, need to insert
                        Logger.debug(this, "Didn't find organization's item: {}, inserting...", organizationItem)
                        tableController.insert(organizationItem, DatabaseOperationMetadata())
                    }
                }

                Completable.complete()
            }
    }
}