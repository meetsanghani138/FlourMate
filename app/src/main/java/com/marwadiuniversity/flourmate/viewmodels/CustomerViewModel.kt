package com.marwadiuniversity.flourmate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.repositories.ProductRepository
import kotlinx.coroutines.launch

class CustomerViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = productRepository.getAllProducts()
        }
    }
}
