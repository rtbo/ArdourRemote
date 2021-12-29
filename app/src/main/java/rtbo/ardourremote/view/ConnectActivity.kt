package rtbo.ardourremote.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
                startConnectNewActivity()
            }
            listAdapter.itemViewModels = it
        })
        list.adapter = listAdapter

        val create = findViewById<FloatingActionButton>(R.id.create_new_btn)
        create.setOnClickListener {
            startConnectNewActivity()
        }
    }

    private fun startConnectNewActivity() {
        val intent = Intent(this, ConnectNewActivity::class.java)
        startActivity(intent)
    }
}
