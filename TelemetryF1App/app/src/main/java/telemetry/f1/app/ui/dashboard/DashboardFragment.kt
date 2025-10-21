package telemetry.f1.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import telemetry.f1.app.R
import telemetry.f1.app.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.telemetryData.collectLatest { data ->
                if (data == null) return@collectLatest

                binding.speedValue.text = getString(R.string.speed_value_kmh, data.m_speed)
                binding.rpmValue.text = data.m_engineRPM.toString()
                binding.rpmProgressBar.progress = (data.m_engineRPM / 100).coerceAtMost(100)
                binding.gearValue.text = when (data.m_gear) {
                    -1 -> "R"
                    0 -> "N"
                    else -> data.m_gear.toString()
                }
                binding.throttleBar.progress = (data.m_throttle * 100).toInt()
                binding.brakeBar.progress = (data.m_brake * 100).toInt()

                val drsColorId = if (data.m_drs == 1) R.color.f1_green else R.color.f1_dark_gray
                binding.drsIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), drsColorId))

                binding.engineTempValue.text = getString(R.string.temperature_celsius, data.m_engineTemperature)

                if (data.m_tyresSurfaceTemperature.size == 4) {
                    binding.tyreTempRl.text = getString(R.string.temperature_celsius, data.m_tyresSurfaceTemperature[0])
                    binding.tyreTempRr.text = getString(R.string.temperature_celsius, data.m_tyresSurfaceTemperature[1])
                    binding.tyreTempFl.text = getString(R.string.temperature_celsius, data.m_tyresSurfaceTemperature[2])
                    binding.tyreTempFr.text = getString(R.string.temperature_celsius, data.m_tyresSurfaceTemperature[3])
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionStatus.collectLatest { connected ->
                val colorId = if (connected) R.color.f1_green else R.color.f1_red
                binding.connectionIndicator.setBackgroundColor(ContextCompat.getColor(requireContext(), colorId))
                binding.connectionStatus.text = if (connected) getString(R.string.connected) else getString(R.string.disconnected)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
