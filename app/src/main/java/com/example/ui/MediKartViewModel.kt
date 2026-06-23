package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediKartViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MediKartDatabase.getDatabase(application)
    val dao = db.dao()
    val repository = MediKartRepository(dao)

    // Active Role state
    private val _currentRole = MutableStateFlow("Customer") // Customer, Pharmacy, Delivery Partner, Admin
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Auth States
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInPhone = MutableStateFlow("+92 300 7654321")
    val loggedInPhone: StateFlow<String> = _loggedInPhone.asStateFlow()

    private val _loggedInName = MutableStateFlow("Ibrahim Chatha")
    val loggedInName: StateFlow<String> = _loggedInName.asStateFlow()

    // Address Management
    private val _addresses = MutableStateFlow(listOf(
        "House 24, Street 2, Ward 5, Alipur Chatha",
        "Shop 5, Main Bazar, Alipur Chatha",
        "Chatha Medical Complex, Gujranwala Road, Alipur Chatha"
    ))
    val addresses: StateFlow<List<String>> = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow("House 24, Street 2, Ward 5, Alipur Chatha")
    val selectedAddress: StateFlow<String> = _selectedAddress.asStateFlow()

    // Premium state
    private val _premiumActive = MutableStateFlow(false)
    val premiumActive: StateFlow<Boolean> = _premiumActive.asStateFlow()

    // Family members
    private val _familyMembers = MutableStateFlow(listOf("Self", "Mother", "Father", "Spouse", "Child"))
    val familyMembers: StateFlow<List<String>> = _familyMembers.asStateFlow()

    // Search & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Flow Data from Repository
    val medicines: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pharmacies: StateFlow<List<Pharmacy>> = repository.allPharmacies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val priceLogs: StateFlow<List<PriceLog>> = repository.allPriceLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart states
    private val _cart = MutableStateFlow<Map<Int, Int>>(emptyMap()) // MedicineId -> Quantity
    val cart: StateFlow<Map<Int, Int>> = _cart.asStateFlow()

    // Active order simulation state for Delivery Partner
    private val _currentDeliveryOrder = MutableStateFlow<Order?>(null)
    val currentDeliveryOrder: StateFlow<Order?> = _currentDeliveryOrder.asStateFlow()

    private val _simulatedGpsLocation = MutableStateFlow("Base Camp (Main Bazar)")
    val simulatedGpsLocation: StateFlow<String> = _simulatedGpsLocation.asStateFlow()

    // Prescription simulation paths
    private val _uploadedPrescriptionPath = MutableStateFlow<String?>(null)
    val uploadedPrescriptionPath: StateFlow<String?> = _uploadedPrescriptionPath.asStateFlow()

    // Notification Alerts Stack
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        // Seed initial data if database is empty
        viewModelScope.launch {
            repository.allPharmacies.first().let { currentPharmacies ->
                if (currentPharmacies.isEmpty()) {
                    seedDatabase()
                }
            }
        }
    }

    private suspend fun seedDatabase() {
        val seededPharmacies = listOf(
            Pharmacy(1, "Al-Shafa Pharmacy", "G.T. Road Close to Chowk, Alipur Chatha", 4.8f, true, "+92 300 1112222"),
            Pharmacy(2, "Chatha Medical Store", "Main Bazar Road, Alipur Chatha", 4.5f, true, "+92 300 3334444"),
            Pharmacy(3, "Bismillah Pharmacy", "Kashmir Chowk, Alipur Chatha", 4.2f, false, "+92 300 5556666")
        )
        for (pharmacy in seededPharmacies) {
            repository.insertPharmacy(pharmacy)
        }

        val seededMedicines = listOf(
            Medicine(1, "Panadol 500mg (Paracetamol)", "Pain Relief", 15.0, 1, "Al-Shafa Pharmacy", true, System.currentTimeMillis() - 7200000, false, "Standard paracetamol pain relief and fever reducer tablets."),
            Medicine(2, "Arinac Forte Tablets", "Cold & Flu", 45.0, 1, "Al-Shafa Pharmacy", true, System.currentTimeMillis() - 3600000, false, "Ibuprofen & Pseudoephedrine Hydrochloride formula for flu and sinus congestion."),
            Medicine(3, "Amoxil 250mg Capsules", "Antibiotics", 120.0, 2, "Chatha Medical Store", true, System.currentTimeMillis() - 14400000, true, "Broad-spectrum penicillin antibiotic for systemic infections. Rx Required."),
            Medicine(4, "Surbex-Z Multivitamin", "Vitamins", 320.0, 2, "Chatha Medical Store", true, System.currentTimeMillis() - 28800000, false, "Zinc, Vitamin B complex, Vitamin E and Vitamin C supplement."),
            Medicine(5, "Loprin 75mg", "Heart & BP", 80.0, 3, "Bismillah Pharmacy", true, System.currentTimeMillis() - 500000, true, "Low dose aspirin for cardiovascular protection. Daily administration. Rx Required."),
            Medicine(6, "Septran DS Tablets", "Antibiotics", 110.0, 3, "Bismillah Pharmacy", true, System.currentTimeMillis() - 6200000, false, "Co-trimoxazole antibacterial double strength tablets."),
            Medicine(7, "Calpol Pediatric Syrup", "Pediatric Care", 95.0, 1, "Al-Shafa Pharmacy", true, System.currentTimeMillis() - 3000000, false, "Gentle paracetamol syrup formulated specifically for infants & children."),
            Medicine(8, "Gaviscon Liquid 120ml", "Digestion", 240.0, 2, "Chatha Medical Store", true, System.currentTimeMillis() - 4250000, false, "Fast-acting relief from heartburn, acid reflux, and general indigestion."),
            Medicine(9, "Glucophage 500mg", "Diabetes", 180.0, 1, "Al-Shafa Pharmacy", true, System.currentTimeMillis() - 1000000, true, "Metformin Hydrochloride for type 2 diabetes glycemic control. Rx Required.")
        )
        for (med in seededMedicines) {
            repository.insertMedicine(med)
        }

        // Preseed some reminders for instant view
        repository.insertReminder(Reminder(
            id = 1,
            medicineName = "Glucophage 500mg",
            time = "08:30 AM",
            frequency = "Daily",
            personName = "Father"
        ))
        repository.insertReminder(Reminder(
            id = 2,
            medicineName = "Panadol 500mg",
            time = "02:00 PM",
            frequency = "Daily",
            personName = "Self"
        ))
    }

    // Role switcher
    fun setRole(role: String) {
        _currentRole.value = role
    }

    // Login simulation
    fun login(phone: String, name: String) {
        _loggedInPhone.value = phone
        _loggedInName.value = name
        _isLoggedIn.value = true
        showNotification("Welcome back, $name! Logged in.")
    }

    fun logout() {
        _isLoggedIn.value = false
        _cart.value = emptyMap()
        _uploadedPrescriptionPath.value = null
    }

    // Addresses
    fun addAddress(newAddress: String) {
        if (newAddress.isNotBlank() && !_addresses.value.contains(newAddress)) {
            _addresses.value = _addresses.value + newAddress
            _selectedAddress.value = newAddress
        }
    }

    fun setSelectedAddress(address: String) {
        _selectedAddress.value = address
    }

    // Subscription
    fun togglePremium() {
        _premiumActive.value = !_premiumActive.value
        if (_premiumActive.value) {
            showNotification("MediKart Plus Activated! Unlimited Free Standard Deliveries.")
        } else {
            showNotification("MediKart Plus Deactivated.")
        }
    }

    // Search filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    // Cart Actions
    fun addToCart(medicineId: Int) {
        val current = _cart.value.toMutableMap()
        current[medicineId] = (current[medicineId] ?: 0) + 1
        _cart.value = current
    }

    fun removeFromCart(medicineId: Int) {
        val current = _cart.value.toMutableMap()
        val count = current[medicineId] ?: 0
        if (count > 1) {
            current[medicineId] = count - 1
        } else {
            current.remove(medicineId)
        }
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun uploadPrescriptionMock() {
        _uploadedPrescriptionPath.value = "res/drawable/prescription_sample.png"
        showNotification("Prescription uploaded successfully. Securely encrypted.")
    }

    fun removePrescriptionPath() {
        _uploadedPrescriptionPath.value = null
    }

    // Places customer order
    fun placeOrder(isEmergency: Boolean = false, paymentMethod: String = "Cash on Delivery") {
        val cartItems = _cart.value
        if (cartItems.isEmpty() && _uploadedPrescriptionPath.value == null) return

        viewModelScope.launch {
            val medList = medicines.value
            val fee = if (_premiumActive.value) 0.0 else 40.0 // Standard delivery is PKR 40

            if (cartItems.isNotEmpty()) {
                // Generate individual orders or grouped list. Let's insert orders into DB
                for ((id, q) in cartItems) {
                    val med = medList.find { it.id == id } ?: continue
                    val priceWithSubscriptionDiscount = if (_premiumActive.value) med.price * 0.9 else med.price

                    val order = Order(
                        customerPhone = _loggedInPhone.value,
                        customerName = _loggedInName.value,
                        deliveryAddress = _selectedAddress.value,
                        medicineId = med.id,
                        medicineName = med.name,
                        quantity = q,
                        totalPrice = priceWithSubscriptionDiscount * q,
                        pharmacyName = med.pharmacyName,
                        status = "Pending",
                        orderTime = System.currentTimeMillis(),
                        deliveryFee = fee,
                        isEmergency = isEmergency,
                        paymentMethod = paymentMethod,
                        prescriptionPhotoPath = if (med.isPrescriptionRequired) "res/drawable/prescription_sample.png" else null
                    )
                    repository.insertOrder(order)
                }
            } else {
                // Pure prescription order
                val order = Order(
                    customerPhone = _loggedInPhone.value,
                    customerName = _loggedInName.value,
                    deliveryAddress = _selectedAddress.value,
                    medicineId = -1,
                    medicineName = "Prescription Assessment (Manual Dispatch)",
                    quantity = 1,
                    totalPrice = 0.0,
                    pharmacyName = "Al-Shafa Pharmacy (Default Assessor)",
                    status = "Pending",
                    orderTime = System.currentTimeMillis(),
                    deliveryFee = fee,
                    isEmergency = isEmergency,
                    paymentMethod = paymentMethod,
                    prescriptionPhotoPath = "res/drawable/prescription_sample.png"
                )
                repository.insertOrder(order)
            }

            _cart.value = emptyMap()
            _uploadedPrescriptionPath.value = null
            showNotification(if (isEmergency) "EMERGENCY request dispatched! Pharmacy and delivery riders alerted." else "Order placed successfully! Pharmacy is reviewing.")
        }
    }

    // Reorder flow
    fun reorder(oldOrder: Order) {
        viewModelScope.launch {
            val newOrder = oldOrder.copy(
                orderId = 0,
                status = "Pending",
                orderTime = System.currentTimeMillis()
            )
            repository.insertOrder(newOrder)
            showNotification("Previous order of ${oldOrder.medicineName} successfully reordered!")
        }
    }

    // Reminders
    fun addReminder(medName: String, time: String, familyMember: String) {
        viewModelScope.launch {
            val rem = Reminder(
                medicineName = medName,
                time = time,
                frequency = "Daily",
                personName = familyMember
            )
            repository.insertReminder(rem)
            showNotification("Medication reminder set for $familyMember ($medName at $time).")
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            showNotification("Reminder deleted.")
        }
    }

    // Pharmacy Flow
    fun updateMedicinePriceAndAvailability(medId: Int, newPrice: Double, available: Boolean) {
        viewModelScope.launch {
            val medList = medicines.value
            val med = medList.find { it.id == medId } ?: return@launch
            val oldPrice = med.price

            val updated = med.copy(
                price = newPrice,
                availability = available,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateMedicine(updated)

            // Log the price revision for admin visibility
            val log = PriceLog(
                medicineId = medId,
                medicineName = med.name,
                pharmacyName = med.pharmacyName,
                oldPrice = oldPrice,
                newPrice = newPrice,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertPriceLog(log)
            showNotification("Price for ${med.name} updated to PKR $newPrice.")
        }
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch {
            val updated = order.copy(status = newStatus)
            repository.updateOrder(updated)
            showNotification("Order #${order.orderId} status changed to $newStatus.")
        }
    }

    // Delivery Partner Simulation Flows
    fun selectDeliveryOrder(order: Order) {
        _currentDeliveryOrder.value = order
        _simulatedGpsLocation.value = "Pharmacy (${order.pharmacyName})"
    }

    fun advanceDeliverySimulation() {
        val order = _currentDeliveryOrder.value ?: return
        viewModelScope.launch {
            when (_simulatedGpsLocation.value) {
                "Pharmacy (${order.pharmacyName})" -> {
                    _simulatedGpsLocation.value = "Transit (G.T. Road / Near Municipal Committee)"
                    updateOrderStatus(order, "Out for Delivery")
                }
                "Transit (G.T. Road / Near Municipal Committee)" -> {
                    _simulatedGpsLocation.value = "Arrived at ${order.deliveryAddress}"
                }
                "Arrived at ${order.deliveryAddress}" -> {
                    _simulatedGpsLocation.value = "Completed Delivery"
                    updateOrderStatus(order, "Delivered")
                    _currentDeliveryOrder.value = null
                }
            }
        }
    }

    // Admin Actions
    fun togglePharmacyVerification(pharmacy: Pharmacy) {
        viewModelScope.launch {
            val updated = pharmacy.copy(isVerified = !pharmacy.isVerified)
            repository.updatePharmacy(updated)
            showNotification("${pharmacy.name} status updated. Verified: ${updated.isVerified}")
        }
    }

    fun triggerBroadcastNotification(msg: String) {
        showNotification("BROADCAST: $msg")
    }

    // Notifications alert systems
    fun showNotification(msg: String) {
        _notificationMessage.value = msg
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
