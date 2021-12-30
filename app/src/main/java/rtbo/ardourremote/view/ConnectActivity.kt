package rtbo.ardourremote.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R

@AndroidEntryPoint
class ConnectActivity : AppCompatActivity() {
    private val viewModel: ConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        viewModel.connections.observe(this, {
            val fragment = if (it.isEmpty()) {
                ConnectNewFragment(viewModel)
            } else {
                ConnectRecentFragment(viewModel)
            }
            supportFragmentManager.commit {
                replace(R.id.connect_fragment_container, fragment)
                setReorderingAllowed(true)
            }
        })
    }
}
