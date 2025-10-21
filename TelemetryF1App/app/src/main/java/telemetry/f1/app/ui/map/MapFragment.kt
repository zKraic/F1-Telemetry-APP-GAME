package telemetry.f1.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import telemetry.f1.app.databinding.FragmentMapBinding
import telemetry.f1.app.R

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
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Set initial canvas size for coordinate transformation
        binding.trackMapView.post {
            val width = binding.trackMapView.width
            val height = binding.trackMapView.height
            if (width > 0 && height > 0) {
                viewModel.setCanvasSize(width, height)
            }
        }
    }

    private fun observeViewModel() {
        // Track info updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trackInfo.collect { trackInfo ->
                binding.trackName.text = trackInfo.name
                binding.trackMapImage.setImageResource(trackInfo.drawableRes)
                binding.trackMapView.setTrackInfo(trackInfo)
            }
        }
        
        // Car position updates (optimized for 60 Hz)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.carPositions.collect { positions ->
                binding.trackMapView.updateCarPositions(positions)
                
                // Update car count
                val activeCars = positions.size
                val playerCar = positions.find { it.isPlayer }
                binding.carCount.text = if (playerCar != null) {
                    "$activeCars coches (Posición: ${playerCar.carIndex + 1})"
                } else {
                    "$activeCars coches detectados"
                }
            }
        }
        
        // Connection status updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionStatus.collect { connected ->
                binding.connectionStatus.text = if (connected) "Conectado" else "Desconectado"
                binding.connectionIndicator.backgroundTintList = 
                    resources.getColorStateList(
                        if (connected) R.color.f1_success else R.color.f1_danger,
                        null
                    )
                
                // Show/hide loading state
                binding.loadingIndicator.visibility = if (connected) View.GONE else View.VISIBLE
            }
        }
        
        // Debug info (can be hidden in production)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.debugInfo.collect { info ->
                binding.debugInfo.text = info
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh connection status when fragment becomes visible
        viewModel.refreshConnection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}