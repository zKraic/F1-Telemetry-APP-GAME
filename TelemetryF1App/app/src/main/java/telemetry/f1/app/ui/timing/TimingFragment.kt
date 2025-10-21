package telemetry.f1.app.ui.timing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import telemetry.f1.app.databinding.FragmentTimingBinding

class TimingFragment : Fragment() {

    private var _binding: FragmentTimingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimingViewModel by viewModels()
    private lateinit var lapTimeAdapter: LapTimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Defensive UI updates to prevent crashes from null data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lapData.collectLatest { data ->
                // Only update the UI if the data is not null
                data?.let { 
                    binding.currentLapTime.text = viewModel.formatTime(it.m_currentLapTimeInMS.toLong())
                    binding.positionValue.text = it.m_carPosition.toString()
                    binding.lapNumberValue.text = it.m_currentLapNum.toString()
                    binding.sector1Time.text = viewModel.formatTime(it.m_sector1TimeInMS.toLong())
                    binding.sector2Time.text = viewModel.formatTime(it.m_sector2TimeInMS.toLong())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
             viewModel.sector3Time.collectLatest { binding.sector3Time.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bestLapTime.collectLatest { binding.bestLapTime.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentLaps.collectLatest { laps ->
                // Only update the adapter if the list is not empty
                if (laps.isNotEmpty()) {
                    lapTimeAdapter.updateLaps(laps)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        lapTimeAdapter = LapTimeAdapter()
        binding.lapTimesRecyclerView.apply {
            adapter = lapTimeAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
