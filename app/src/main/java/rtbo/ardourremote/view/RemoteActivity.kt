package rtbo.ardourremote.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R
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

        val id = intent.extras?.getLong(REMOTE_CONN_ID_KEY)!!
        viewModel.setConnectionId(id)

        val recBtn = findViewById<ImageButton>(R.id.record_toggle_btn)
        val defRes = recBtn.background
        val recEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.rec_enabled))
        val recDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.rec_disabled))
        viewModel.recordBtnStyle.observe(this) {
            when (it) {
                RecordBtnStyle.OFF -> {
                    Log.d("REMOTE_VIEW", "rec off")
                    recBtn.background = defRes
                    ImageViewCompat.setImageTintList(recBtn, recDisabled)
                }
                RecordBtnStyle.SOLID -> {
                    Log.d("REMOTE_VIEW", "rec solid")
                    recBtn.background = defRes
                    ImageViewCompat.setImageTintList(recBtn, recEnabled)
                }
                RecordBtnStyle.BLINK -> {
                    Log.d("REMOTE_VIEW", "rec blink")
                    recBtn.setBackgroundResource(R.drawable.rec_blink_animation)
                }
                null -> {}
            }
        }

        val stopBtn = findViewById<ImageButton>(R.id.stop_btn)
        val stopEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.stop_enabled))
        val stopDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.stop_disabled))
        viewModel.stopped.observe(this) {
            val col = if (it) {
                Log.d("REMOTE_VIEW", "stop enabled")
                stopEnabled
            } else {
                Log.d("REMOTE_VIEW", "stop disabled")
                stopDisabled
            }
            ImageViewCompat.setImageTintList(stopBtn, col)
        }

        val playBtn = findViewById<ImageButton>(R.id.play_btn)
        val playEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.play_enabled))
        val playDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.play_disabled))
        viewModel.stopped.observe(this) {
            val col = if (it) {
                Log.d("REMOTE_VIEW", "play enabled")
                playEnabled
            } else {
                Log.d("REMOTE_VIEW", "play disabled")
                playDisabled
            }
            ImageViewCompat.setImageTintList(playBtn, col)
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