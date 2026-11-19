package com.marwadiuniversity.flourmate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.repositories.OrderRepository
import kotlinx.coroutines.launch

class DeliveryViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _requests = MutableLiveData<List<Order>>()
    val requests: LiveData<List<Order>> = _requests

    fun loadRequests(partnerId: Long) {
        viewModelScope.launch {
            _requests.value = orderRepository.getAvailableDeliveryRequests(partnerId)
        }
    }
}
