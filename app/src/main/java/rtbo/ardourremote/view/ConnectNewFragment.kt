package rtbo.ardourremote.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import rtbo.ardourremote.R
import rtbo.ardourremote.databinding.FragmentConnectNewBinding

class ConnectNewFragment(private val viewModel: ConnectViewModel) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentConnectNewBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = viewModel

        val connectBtn = binding.root.findViewById<Button>(R.id.connect_button)
        connectBtn.setOnClickListener{
            viewModel.connectNew()
        }

        return binding.root
    }
}