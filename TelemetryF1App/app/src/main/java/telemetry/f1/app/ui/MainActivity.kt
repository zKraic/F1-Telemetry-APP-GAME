package telemetry.f1.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import telemetry.f1.app.R
import telemetry.f1.app.databinding.ActivityMainBinding
import telemetry.f1.app.network.TelemetryRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val telemetryRepository = TelemetryRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        startTelemetryListening()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun startTelemetryListening() {
        // Start listening on F1 default port
        // F1 2025 supports up to 60 Hz if configured in game settings
        telemetryRepository.startListening(20777)
        println("F1 Telemetry App: Started listening on port 20777")
        println("Configure F1 2025 game: UDP Telemetry ON, Port 20777, Send Rate 60 Hz, Format 2025")
    }

    override fun onDestroy() {
        super.onDestroy()
        telemetryRepository.stopListening()
        println("F1 Telemetry App: Stopped telemetry listening")
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure telemetry is still running
        if (!telemetryRepository.isConnected()) {
            telemetryRepository.startListening(20777)
        }
    }
}