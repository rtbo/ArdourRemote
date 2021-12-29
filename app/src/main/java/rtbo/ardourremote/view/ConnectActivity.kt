package rtbo.ardourremote.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R

@AndroidEntryPoint
class ConnectActivity : AppCompatActivity() {
    private val viewModel: ConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
        val list = findViewById<RecyclerView>(R.id.recent_connection_list)
        val listAdapter = ConnectionListAdapter()
        viewModel.connections.observe(this, {
            if (it.isEmpty()) {
                val intent = Intent(this, ConnectNewActivity::class.java)
                startActivity(intent)
            }
            listAdapter.itemViewModels = it
        })
        list.adapter = listAdapter
    }
}
