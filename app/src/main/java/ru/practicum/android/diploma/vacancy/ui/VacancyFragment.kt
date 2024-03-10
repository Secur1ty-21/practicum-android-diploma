package ru.practicum.android.diploma.vacancy.ui

import android.Manifest
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.core.domain.model.DetailVacancy
import ru.practicum.android.diploma.databinding.FragmentVacancyBinding
import ru.practicum.android.diploma.util.StringUtils
import ru.practicum.android.diploma.vacancy.presentation.VacancyScreenState
import ru.practicum.android.diploma.vacancy.presentation.VacancyViewModel
import kotlin.properties.Delegates

class VacancyFragment : Fragment() {
    private var id by Delegates.notNull<Long>()
    private val viewModel by viewModel<VacancyViewModel> { parametersOf(id) }
    private var _binding: FragmentVacancyBinding? = null
    private val binding get() = _binding!!
    private var detailVacancy: DetailVacancy? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        id = VacancyFragmentArgs.fromBundle(requireArguments()).vacancyId
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVacancyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
        binding.vacancyToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    viewModel.makeCall(detailVacancy!!.phone)
                }
            }
        binding.textViewPhoneValue.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun render(vacancyScreenState: VacancyScreenState) {
        when (vacancyScreenState) {
            is VacancyScreenState.Loading -> showProgressBar()
            is VacancyScreenState.Error -> showError()
            is VacancyScreenState.Content -> {
                setContent(vacancyScreenState.vacancy, vacancyScreenState.isFavourite)
                showContent()
            }
        }
    }

    private fun showProgressBar() {
        binding.scrollViewVacancy.isVisible = false
        binding.progressBarVacancy.isVisible = true
        binding.imageViewServerError.isVisible = false
        binding.textViewServerError.isVisible = false
    }

    private fun showContent() {
        binding.scrollViewVacancy.isVisible = true
        binding.progressBarVacancy.isVisible = false
        binding.imageViewServerError.isVisible = false
        binding.textViewServerError.isVisible = false
    }

    private fun showError() {
        binding.scrollViewVacancy.isVisible = false
        binding.progressBarVacancy.isVisible = false
        binding.imageViewServerError.isVisible = true
        binding.textViewServerError.isVisible = true
    }

    private fun setContent(detailVacancy: DetailVacancy, isInFavourites: Boolean) {
        this.detailVacancy = detailVacancy
        binding.textViewVacancyValue.text = detailVacancy.name
        binding.textViewEmployerValue.text = detailVacancy.employerName
        binding.textViewRequiredExperienceValue.text = detailVacancy.experience
        binding.textViewEmploymentAndSchedule.text = requireContext().resources.getString(
            R.string.tv_schedule_and_employment,
            detailVacancy.employment,
            detailVacancy.workSchedule
        )
        binding.textViewDescriptionValue.text = detailVacancy.description
        setSalary(detailVacancy.salaryFrom, detailVacancy.salaryTo, detailVacancy.currency)
        setLogo(detailVacancy.employerLogoUrl)
        setKeySkills(requireContext().resources)
        setLocation(detailVacancy.city, detailVacancy.area)
        setContactInfo(
            detailVacancy.contactName,
            detailVacancy.email,
            detailVacancy.phone,
            detailVacancy.contactComment
        )
        setFavouriteImage(isInFavourites)
        binding.imageViewShareVacancy.setOnClickListener { viewModel.shareVacancy(detailVacancy.alternateUrl) }
        binding.imageViewFavorite.setOnClickListener {
            viewModel.setFavourites()
            setContent(detailVacancy, !isInFavourites)
        }
        binding.textViewEmailValue.setOnClickListener { viewModel.sendEmail(detailVacancy.email) }
    }

    private fun setLogo(employerLogoUrl: String) {
        Glide.with(binding.imageViewEmployerLogo)
            .load(employerLogoUrl)
            .placeholder(R.drawable.placeholder_vacancy)
            .transform(
                RoundedCorners(
                    requireContext().resources
                        .getDimensionPixelSize(R.dimen.tv_detailed_vacancy_corner_radius)
                )
            )
            .into(binding.imageViewEmployerLogo)
    }

    private fun setSalary(salaryFrom: String, salaryTo: String, currency: String) {
        binding.textViewSalaryInfoValue.text = StringUtils.getSalary(
            salaryFrom = salaryFrom,
            salaryTo = salaryTo,
            currency = currency,
            context = requireContext()
        )
    }

    private fun setKeySkills(resources: Resources) {
        val formattedKeySkills = viewModel.formatKeySkills(resources)
        if (detailVacancy?.keySkills?.isEmpty() == true) {
            binding.textViewKeySkillsTitle.isVisible = false
            binding.textViewKeySkillsValue.isVisible = false
        } else {
            binding.textViewKeySkillsValue.text = formattedKeySkills
        }
    }

    private fun setContactInfo(contactName: String, email: String, phone: String, contactComment: String) {
        if (contactName.isEmpty()) {
            binding.textViewContactNameTitle.isVisible = false
            binding.textViewContactNameValue.isVisible = false
            binding.textViewContactInfoTitle.isVisible = false
        } else {
            binding.textViewContactNameValue.text = contactName
        }
        if (email.isEmpty()) {
            binding.textViewEmailTitle.isVisible = false
            binding.textViewEmailValue.isVisible = false
        } else {
            binding.textViewEmailValue.text = email
        }

        if (phone.isEmpty()) {
            binding.textViewPhoneTitle.isVisible = false
            binding.textViewPhoneValue.isVisible = false
        } else {
            binding.textViewPhoneValue.text = phone
        }

        if (contactComment.isEmpty()) {
            binding.textViewCommentTitle.isVisible = false
            binding.textViewCommentValue.isVisible = false
        } else {
            binding.textViewCommentValue.text = contactComment
        }
    }

    private fun setLocation(city: String, area: String) {
        if (city.isNotEmpty()) {
            binding.textViewEmployerCityValue.text = city
        } else {
            binding.textViewEmployerCityValue.text = area
        }
    }

    private fun setFavouriteImage(isInFavourites: Boolean) {
        if (isInFavourites) {
            binding.imageViewFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_favorites_add
                )
            )
        } else {
            binding.imageViewFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_favorites_remove
                )
            )
        }
    }
}
