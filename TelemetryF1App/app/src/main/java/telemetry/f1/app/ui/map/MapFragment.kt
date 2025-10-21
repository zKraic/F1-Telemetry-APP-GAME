package telemetry.f1.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import telemetry.f1.app.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackId.collectLatest { trackId ->
                // Post the action to the view's message queue to ensure it runs after the layout pass
                binding.trackMapView.post {
                    binding.trackMapView.setTrackId(trackId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.carPositions.collectLatest { positions ->
                binding.trackMapView.updateCarPositions(positions)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionStatus.collectLatest { connected ->
                binding.connectionStatus.text = if (connected) "Connected" else "Disconnected"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
