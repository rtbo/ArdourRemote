package rtbo.ardourremote.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import dagger.hilt.android.AndroidEntryPoint
import rtbo.ardourremote.R
import rtbo.ardourremote.databinding.ActivityRemoteBinding
import java.util.*

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
        val recEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.rec_enabled))
        val recDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.rec_disabled))
        var blinkTimer: Timer? = null
        var blinkHandler = Handler(Looper.getMainLooper()) {
            val col = if (it.what == 1) {
                recEnabled
            } else {
                recDisabled
            }
            ImageViewCompat.setImageTintList(recBtn, col)
            true
        }
        viewModel.recordBtnStyle.observe(this) {
            when (it) {
                RecordBtnStyle.OFF -> {
                    blinkTimer?.cancel()
                    blinkTimer = null
                    ImageViewCompat.setImageTintList(recBtn, recDisabled)
                }
                RecordBtnStyle.SOLID -> {
                    blinkTimer?.cancel()
                    blinkTimer = null
                    ImageViewCompat.setImageTintList(recBtn, recEnabled)
                }
                RecordBtnStyle.BLINK -> {
                    if (blinkTimer === null) {
                        blinkTimer = Timer("Rec blink")
                        blinkTimer?.scheduleAtFixedRate(RecordBlink(blinkHandler), 0, 300)
                    }
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
                stopEnabled
            } else {
                stopDisabled
            }
            ImageViewCompat.setImageTintList(stopBtn, col)
        }

        val playBtn = findViewById<ImageButton>(R.id.play_btn)
        val playEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.play_enabled))
        val playDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.play_disabled))
        viewModel.playing.observe(this) {
            val col = if (it) {
                playEnabled
            } else {
                playDisabled
            }
            ImageViewCompat.setImageTintList(playBtn, col)
        }

        val hbImg = findViewById<ImageView>(R.id.heartbeat_led)
        val hbEnabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.heartbeat_enabled))
        val hbDisabled =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.heartbeat_disabled))
        viewModel.heartbeat.observe(this) {
            val col = if (it) {
                hbEnabled
            } else {
                hbDisabled
            }
            ImageViewCompat.setImageTintList(hbImg, col)
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

    class RecordBlink(
        private val handler: Handler
    ) : TimerTask() {
        private var state = false
        override fun run() {
            val what = if (state) { 1 } else { 0 }
            handler.sendEmptyMessage(what)
            state = !state
        }
    }
}
