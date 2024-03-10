package ru.practicum.android.diploma.filter.placeselector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentPlaceSelectorBinding

class PlaceSelectorFragment : Fragment() {

    private var _binding: FragmentPlaceSelectorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaceSelectorViewModel by viewModel()

    private var countryName: String = ""
    private var countryId: String = ""
    private var regionName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.observe(viewLifecycleOwner) {
            it.country?.let { country -> countryId = country.id }
            binding.countryText.setText(it.country?.name ?: "")
            binding.regionText.setText(it.area?.name ?: "")
            if (it.area?.name.isNullOrEmpty() && it.country?.name.isNullOrEmpty()) {
                binding.selectButton.visibility = View.GONE
            } else {
                binding.selectButton.visibility = View.VISIBLE
            }
        }
        getData()
        initUi()
        initListeners()
    }

    private fun initListeners() {
        binding.filterToolbarPlace.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.countryNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_placeSelectorFragment_to_countryFragment)
        }
        binding.regionNavigation.setOnClickListener {
            val action = PlaceSelectorFragmentDirections.actionPlaceSelectorFragmentToRegionFragment(
                countryId,
                countryName
            )
            findNavController().navigate(action)
        }
    }

    private fun getData() {
        setFragmentResultListener(PLACE_SELECTOR_KEY) { _, bundle ->
            viewModel.onFragmentResultEvent(
                countryId = bundle.getString(COUNTRY_ID_KEY, ""),
                countryName = bundle.getString(COUNTRY_NAME_KEY, ""),
                areaId = bundle.getString(AREA_ID_KEY, ""),
                areaName = bundle.getString(AREA_NAME_KEY, "")
            )
        }
        viewModel.init()
    }

    private fun initUi() {
        changeIcon(binding.countryText, binding.countryIcon)
        changeIcon(binding.regionText, binding.regionIcon)
        if (regionName.isEmpty() && countryName.isEmpty()) {
            binding.selectButton.visibility = View.GONE
        } else {
            binding.selectButton.visibility = View.VISIBLE
        }
        binding.selectButton.setOnClickListener {
            viewModel.onBtnSelectClickEvent()
            findNavController().popBackStack()
        }
    }

    private fun changeIcon(editText: EditText, view: ImageView) {
        editText.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                view.setImageResource(R.drawable.ic_arrow_forward)
            } else {
                view.setImageResource(R.drawable.ic_close)
                view.setOnClickListener {
                    editText.setText("")
                    if (it.id == binding.countryIcon.id) {
                        countryId = ""
                        countryName = ""
                        viewModel.onBtnClearCountryClickEvent()
                    } else if (it.id == binding.regionIcon.id) {
                        viewModel.onBtnClearAreaClickEvent()
                    }
                    changeIcon(editText, view)
                }
            }
        }
    }

    companion object {
        const val PLACE_SELECTOR_KEY = "PLACE_SELECTOR_KEY"
        const val COUNTRY_ID_KEY = "COUNTRY_ID_KEY"
        const val COUNTRY_NAME_KEY = "COUNTRY_NAME_KEY"
        const val AREA_ID_KEY = "AREA_ID_KEY"
        const val AREA_NAME_KEY = "AREA_NAME_KEY"
    }
}
