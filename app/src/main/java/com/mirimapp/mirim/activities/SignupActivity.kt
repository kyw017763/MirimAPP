package com.mirimapp.mirim.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import com.dahyeon.mirim.R
import com.dahyeon.mirim.R.string.email
import com.mirimapp.mirim.dialogs.EmailCertifyDialog
import com.mirimapp.mirim.network.Connector
import com.mirimapp.mirim.network.Res
import com.mirimapp.mirim.util.BaseActivity
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.dialog_email_certify.*

class SignupActivity : BaseActivity() {
    val passwordRegex = "^[a-zA-Z0-9_*]{5,12}$".toRegex()
    //비밀번호는 5~12글자 이하, _, * 특수문자만 사용 가능

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        textview_signup_go_to_signin.setOnClickListener {
            startActivity(Intent(applicationContext, SigninActivity::class.java))
        }

        button_signup_confirm.setOnClickListener {
            val emailPrefix = edittext_signup_email.text.toString()
            val password = edittext_signup_password.text.toString()
            val passwordToCheck = edittext_signup_password_check.text.toString()

            if(emailPrefix.isEmpty() || password.isEmpty() || passwordToCheck.isEmpty()) {
                // TODO
            }

            val emailWithSuffix = emailPrefix + getString(R.string.email_suffix)

            if (password.matches(passwordRegex)) {
                if (password == passwordToCheck) {
                    Connector.api.signup(hashMapOf(
                        "email" to emailWithSuffix,
                        "pw" to password
                    )).enqueue(object: Res<Void>(this) {
                        override fun callBack(code: Int, body: Void?) {
                            when(code) {
                                409 -> "이미 가입된 이메일입니다."
                                401 -> "이메일 인증이 필요합니다."
                                201 -> null
                                else -> "오류: $code"
                            }.let {
                                if(it != null) {
                                    showToast(it)
                                } else {
                                    showToast("회원가입 성공")
                                    startActivity(Intent(context, SigninActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    })
                } else {
                    textview_signup_invalid_reason.setText("비밀번호와 비밀번호 확인 불일치")
                }
            } else {
                textview_signup_invalid_reason.setText("비밀번호에는 5~12 길이의 영 대소문자, 숫자, 특수기호 '_' '*'만 사용 가능")
            }
        }

        textview_signup_send_certify_mail.setOnClickListener {
            val emailPrefix = edittext_signup_email.text.toString()

            if(emailPrefix.isEmpty()) {
                showToast("이메일을 입력해 주세요.")
            } else {
                val emailWithSuffix = emailPrefix + getString(R.string.email_suffix)

                Connector.api.sendCertifyEmail(
                    emailWithSuffix
                ).enqueue(object : Res<Void>(this) {
                    override fun callBack(code: Int, body: Void?) {
                        when (code) {
                            200 -> null
                            204 -> "이미 회원가입이 완료된 이메일입니다."
                            208 -> "이미 인증된 이메일입니다."
                            400 -> "유효한 이메일 포맷이 아닙니다."
                            else -> "오류: $code"
                        }.let {
                            if (it != null) {
                                showToast(it)
                            } else {
                                val dialog = EmailCertifyDialog(context, emailWithSuffix)
                                dialog.show()
                            }
                        }
                    }
                })
            }
        }
    }
}
