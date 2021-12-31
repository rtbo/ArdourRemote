package rtbo.ardourremote.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import rtbo.ardourremote.R
import rtbo.ardourremote.databinding.FragmentConnectNewBinding

class ConnectNewFragment(private val viewModel: ConnectViewModel) : Fragment() {
    init {
        viewModel.newConn.observe(this) {
            val intent = Intent(activity, RemoteActivity::class.java)
            intent.putExtra(REMOTE_CONN_ID_KEY, it.id)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentConnectNewBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = viewModel

        val connectBtn = binding.root.findViewById<Button>(R.id.connect_button)
        connectBtn.setOnClickListener {
            viewModel.connectNew()
        }

        return binding.root
    }
}