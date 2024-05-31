package com.example.storyappnadhira

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.example.storyappnadhira.animation.applyFadeInAnimations
import com.example.storyappnadhira.api.ApiConfig
import com.example.storyappnadhira.api.RegisterCredentials
import com.example.storyappnadhira.api.RegisterResponse
import com.example.storyappnadhira.api.StoryAPI
import com.example.storyappnadhira.utils.MyButton
import com.example.storyappnadhira.utils.MyEditText
import com.example.storyappnadhira.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var storyApi: StoryAPI
    private lateinit var myButton: MyButton
    private lateinit var myEditText: MyEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        storyApi = ApiConfig.getApiService()

        val button = listOf(
            binding.edRegisterName,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.btnRegister
        )
        applyFadeInAnimations(this, button)

        binding.btnRegister.isEnabled = false

        myButton = findViewById(R.id.btn_register)
        myEditText = findViewById(R.id.ed_register_password)

        setMyButtonEnable()

        myEditText.listener = object: MyEditText.OnPasswordLengthChanged {
            override fun onLengthChanged(length: Int) {
                myButton.updateTextBasedOnPasswordLength(length)
            }
        }
        setMyButtonEnable()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            val user = RegisterCredentials(name, email, password)
            val call = storyApi.register(user)

            showLoading(true)

            call.enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse?.error == false) {
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, registerResponse?.message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    private fun setMyButtonEnable() {
        val result = myEditText.text
        myButton.isEnabled = result != null && result.toString().isNotEmpty() && result.length >= 8
    }

    private fun showLoading(isLoading: Boolean) {
        val loadingOverlay = findViewById<FrameLayout>(R.id.loading_overlay)
        if (isLoading) {
            loadingOverlay.visibility = View.VISIBLE
        } else {
            loadingOverlay.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}