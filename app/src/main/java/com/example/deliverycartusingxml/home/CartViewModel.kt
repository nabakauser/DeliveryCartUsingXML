package com.example.deliverycartusingxml.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartViewModel: ViewModel() {

    var purchaseCost = MutableLiveData<String>()
    var paymentMethod = MutableLiveData<String>()
}