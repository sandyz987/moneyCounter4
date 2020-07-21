package com.example.moneycounter4.view.activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.moneycounter4.R
import com.example.moneycounter4.utils.HttpUtilCallback
import com.example.moneycounter4.utils.HttpUtils.HttpUtil
import com.example.moneycounter4.viewmodel.MainApplication
import com.example.moneycounter4.viewmodel.MainViewModel
import com.example.moneycounter4.widgets.ProgressDialogW
import kotlinx.android.synthetic.main.activity_login.imageView
import kotlinx.android.synthetic.main.activity_register.*

class ActivityRegister : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val viewModel :MainViewModel by viewModels()
        imageView.setColorFilter(Color.BLACK)

        floatRegisterBack.setOnClickListener {
            val intent = Intent(this,ActivityLogin::class.java)
            startActivity(intent)
            finish()
        }


        floatRegisterRegister.setOnClickListener {

            if(edittextRegisterAccountnum.text.toString().length<=3 || edittextRegisterPassword.text.toString().length<=3){
                Toast.makeText(this,"账号和密码的长度应大于3~",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ProgressDialogW.show(this@ActivityRegister,"提示","正在注册中")
            HttpUtil.getInstance().httpGet((application as MainApplication).connectionUrlMain,object :
                HttpUtilCallback {
                override fun doSomething(respond: String?) {
                    ProgressDialogW.hide()
                    if(respond.equals("1")){
                        this@ActivityRegister.runOnUiThread { Toast.makeText(applicationContext,R.string.register_success,Toast.LENGTH_SHORT).show() }
                        val intent = Intent(this@ActivityRegister,ActivityLogin::class.java)
                        startActivity(intent)
                        finish()
                    }
                    if(respond.equals("-1")){
                        this@ActivityRegister.runOnUiThread { Toast.makeText(applicationContext,R.string.register_denied_error,Toast.LENGTH_SHORT).show() }
                    }
                }

                override fun error() {
                    ProgressDialogW.hide()
                    this@ActivityRegister.runOnUiThread { Toast.makeText(applicationContext,R.string.register_connection_error,Toast.LENGTH_SHORT).show() }
                }
            },this,"page","test","post","register","accountnum",edittextRegisterAccountnum.text.toString(),"password",edittextRegisterPassword.text.toString())
        }

    }
}