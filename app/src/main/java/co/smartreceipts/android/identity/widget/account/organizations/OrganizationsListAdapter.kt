package co.smartreceipts.android.identity.widget.account.organizations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.smartreceipts.android.databinding.ItemOrganizationBinding
import co.smartreceipts.android.identity.apis.organizations.OrganizationModel
import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class OrganizationsListAdapter : RecyclerView.Adapter<OrganizationsListAdapter.OrganizationViewHolder>() {

    private var organizationModels = emptyList<OrganizationModel>()

    private val applySettingsClicks = PublishSubject.create<OrganizationModel>()
    private val uploadSettingsClicks = PublishSubject.create<OrganizationModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationViewHolder =
        OrganizationViewHolder(ItemOrganizationBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemCount(): Int = organizationModels.size

    override fun onBindViewHolder(holder: OrganizationViewHolder, index: Int) {
        val organizationModel = organizationModels[index]

        /*
        *  internal val organizationName: TextView = itemView.organization_name
        internal val userRole: TextView = itemView.user_role
        internal val settingsUnsyncedText: TextView = itemView.organization_text_unsynced
        internal val settingsSyncedText: TextView = itemView.organization_text_synced
        internal val updateButton: Button = itemView.organization_update_button

        * */

        holder.binding.apply {
            organizationName.text = organizationModel.organization.name
            userRole.text = organizationModel.userRole.name

            organizationTextUnsynced.visibility = if (organizationModel.settingsMatch) View.GONE else View.VISIBLE
            organizationTextSynced.visibility = if (organizationModel.settingsMatch) View.VISIBLE else View.GONE
            organizationUpdateButton.visibility =
                if (organizationModel.settingsMatch || organizationModel.userRole != OrganizationUser.UserRole.ADMIN) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            organizationTextUnsynced.setOnClickListener { applySettingsClicks.onNext(organizationModel) }
            organizationUpdateButton.setOnClickListener { uploadSettingsClicks.onNext(organizationModel) }
        }
    }

    fun setOrganizations(organizations: List<OrganizationModel>) {
        DiffUtil.calculateDiff(OrganizationDiffUtil(this.organizationModels, organizations)).dispatchUpdatesTo(this)

        this.organizationModels = organizations
    }

    fun getApplySettingsStream(): Subject<OrganizationModel> = applySettingsClicks

    fun getUploadSettingsStream(): Subject<OrganizationModel> = uploadSettingsClicks

    class OrganizationViewHolder internal constructor(val binding: ItemOrganizationBinding) : RecyclerView.ViewHolder(binding.root)

    internal class OrganizationDiffUtil(private val oldList: List<OrganizationModel>, private val newList: List<OrganizationModel>) :
        DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].organization.id == newList[newItemPosition].organization.id

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.settingsMatch == newItem.settingsMatch &&
                    oldItem.organization.name == newItem.organization.name &&
                    oldItem.userRole == newItem.userRole
        }
    }
}