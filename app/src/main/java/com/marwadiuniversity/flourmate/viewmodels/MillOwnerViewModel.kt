package com.marwadiuniversity.flourmate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.repositories.OrderRepository
import com.marwadiuniversity.flourmate.repositories.ProductRepository
import kotlinx.coroutines.launch

class MillOwnerViewModel(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _millProducts = MutableLiveData<List<Product>>()
    val millProducts: LiveData<List<Product>> = _millProducts

    private val _millOrders = MutableLiveData<List<Order>>()
    val millOrders: LiveData<List<Order>> = _millOrders

    fun loadMillProducts(millId: Long) {
        viewModelScope.launch {
            _millProducts.value = productRepository.getProductsByMillId(millId)
        }
    }

    fun loadMillOrders(millId: Long) {
        viewModelScope.launch {
            _millOrders.value = orderRepository.getOrdersForMill(millId)
        }
    }
}
