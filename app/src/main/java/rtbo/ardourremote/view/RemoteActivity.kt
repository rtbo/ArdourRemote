package rtbo.ardourremote.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.databinding.ActivityRemoteBinding

const val REMOTE_CONN_ID_KEY = "REMOTE_CONN_ID"

@AndroidEntryPoint
class RemoteActivity : AppCompatActivity() {
    private val viewModel: RemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityRemoteBinding.inflate(layoutInflater)
        binding.model = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        if (savedInstanceState != null) {
            val id = savedInstanceState.getSerializable(REMOTE_CONN_ID_KEY).toString().toLong()
            viewModel.setConnectionId(id)
        } else {
            val id = intent.extras?.getLong(REMOTE_CONN_ID_KEY)!!
            viewModel.setConnectionId(id)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.connect()
    }

    override fun onStop() {
        viewModel.disconnect()
        super.onStop()
    }

}