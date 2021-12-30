package rtbo.ardourremote.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import rtbo.ardourremote.databinding.ConnectionItemBinding

class ConnectionListAdapter : RecyclerView.Adapter<ConnectionViewHolder>() {

    var connections: List<ConnectionItemViewModel> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder {
        val binding  = ConnectionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConnectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) {
        holder.bind(connections[position])
    }

    override fun getItemCount(): Int = connections.size
}

class ConnectionViewHolder(private val binding: ConnectionItemBinding): RecyclerView.ViewHolder(binding.root)
{
    fun bind(vm: ConnectionItemViewModel) {
        binding.model = vm
        binding.executePendingBindings()
    }
}