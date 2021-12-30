package rtbo.ardourremote.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import rtbo.ardourremote.R

class ConnectRecentFragment(private val viewModel: ConnectViewModel) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_connect_recent, container, false)

        val list = view.findViewById<RecyclerView>(R.id.recent_connection_list)
        val listAdapter = ConnectionListAdapter()
        list.adapter = listAdapter
        viewModel.connections.observe (viewLifecycleOwner, {
            listAdapter.connections = it
        })

        val button = view.findViewById<FloatingActionButton>(R.id.connect_new_btn)
        button.setOnClickListener{
            parentFragmentManager.commit {
                replace(R.id.connect_fragment_container, ConnectNewFragment(viewModel))
                setReorderingAllowed(true)
                addToBackStack("Connect")
            }
        }

        return view
    }
}