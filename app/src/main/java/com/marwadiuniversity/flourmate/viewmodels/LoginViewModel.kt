package com.marwadiuniversity.flourmate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marwadiuniversity.flourmate.models.User
import com.marwadiuniversity.flourmate.repositories.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val user = userRepository.loginUser(email, pass)
            _loginResult.value = user
        }
    }
}
