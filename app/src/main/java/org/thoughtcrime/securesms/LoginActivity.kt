package org.thoughtcrime.securesms

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.IdlingResource
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import org.thoughtcrime.securesms.IdlingResource.SimpleIdlingResource

class LoginActivity : AppCompatActivity(), MessageDelayer.DelayerCallback {

    private lateinit var etName: EditText
    private lateinit var etPwd: EditText
    private lateinit var btnLogin: Button
    private lateinit var ivHead: ImageView

    private var mIdlingResource: SimpleIdlingResource = SimpleIdlingResource()

    @VisibleForTesting
    fun getIdlingResource(): SimpleIdlingResource {
//        if (mIdlingResource == null) {
//            mIdlingResource = SimpleIdlingResource()
//        }
        return mIdlingResource
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        ivHead = findViewById(R.id.ivHead)
        etName = findViewById(R.id.etName)
        etPwd = findViewById(R.id.etPwd)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            btnLogin.text = "等待消息"
            val pwd = etPwd.text.toString()

            MessageDelayer.processMessage(pwd, this, mIdlingResource!!)
            Toast.makeText(
                this@LoginActivity,
                "点击了登录按钮",
                Toast.LENGTH_LONG
            ).show()
        }

        //App 开始进入忙碌状态 ，等待通知
        mIdlingResource!!.increment()

        Glide.with(this)
            .load("https://avatars2.githubusercontent.com/u/2297803?v=3&s=460")
            .into(object : DrawableImageViewTarget(ivHead) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    super.onResourceReady(resource, transition)
                    ivHead.setImageDrawable(resource)
                    //加载完毕后，将App设置成空闲状态
                    mIdlingResource!!.decrement()
                }
            })
    }

    override fun onDone(text: String) {
        btnLogin.text = text
    }
}