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

        viewModel.connections.observe(this) {
            val useRecent = it.isNotEmpty()
            supportFragmentManager.commit {
                if (useRecent) {
                    replace(R.id.connect_fragment_container, ConnectRecentFragment::class.java, null)
                } else {
                    replace(R.id.connect_fragment_container, ConnectNewFragment::class.java, null)
                }
                setReorderingAllowed(true)
            }
        }
    }
}
