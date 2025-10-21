package telemetry.f1.app.ui.timing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import telemetry.f1.app.data.LapTime
import telemetry.f1.app.databinding.ItemLapTimeBinding
import java.text.SimpleDateFormat
import java.util.*

class LapTimeAdapter : RecyclerView.Adapter<LapTimeAdapter.LapTimeViewHolder>() {
    
    private var laps = listOf<LapTime>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    fun updateLaps(newLaps: List<LapTime>) {
        val diffCallback = LapTimeDiffCallback(laps, newLaps)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        laps = newLaps
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapTimeViewHolder {
        val binding = ItemLapTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LapTimeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: LapTimeViewHolder, position: Int) {
        holder.bind(laps[position])
    }
    
    override fun getItemCount() = laps.size
    
    inner class LapTimeViewHolder(
        private val binding: ItemLapTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(lapTime: LapTime) {
            binding.trackName.text = lapTime.trackName
            binding.lapTimeValue.text = formatTime(lapTime.lapTimeInMs)
            binding.sector1Value.text = formatTime(lapTime.sector1TimeMs)
            binding.sector2Value.text = formatTime(lapTime.sector2TimeMs)
            binding.sector3Value.text = formatTime(lapTime.sector3TimeMs)
            binding.timestamp.text = dateFormat.format(Date(lapTime.timestamp))
            
            binding.validIndicator.alpha = if (lapTime.isValidLap) 1.0f else 0.3f
        }
        
        private fun formatTime(milliseconds: Long): String {
            if (milliseconds <= 0) return "--:--.---"
            
            val minutes = (milliseconds / 60000).toInt()
            val seconds = ((milliseconds % 60000) / 1000).toInt()
            val millis = (milliseconds % 1000).toInt()
            
            return String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
    
    private class LapTimeDiffCallback(
        private val oldList: List<LapTime>,
        private val newList: List<LapTime>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}