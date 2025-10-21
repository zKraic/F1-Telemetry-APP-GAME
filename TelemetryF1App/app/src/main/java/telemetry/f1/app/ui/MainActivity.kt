package telemetry.f1.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        // Iniciar escucha en el puerto por defecto de F1
        telemetryRepository.startListening(20777)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        telemetryRepository.stopListening()
    }
}