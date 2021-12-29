package rtbo.ardourremote.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R
import rtbo.ardourremote.databinding.ActivityConnectNewBinding

@AndroidEntryPoint
class ConnectNewActivity : AppCompatActivity() {
    private val viewModel: ConnectNewViewModel by viewModels()
    private lateinit var binding: ActivityConnectNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectNewBinding.inflate(LayoutInflater.from(baseContext))
        binding.lifecycleOwner = this
        binding.model = viewModel
        setContentView(binding.root)
    }
}