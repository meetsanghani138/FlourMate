package com.marwadiuniversity.flourmate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.marwadiuniversity.flourmate.adapters.CartAdapter
import com.marwadiuniversity.flourmate.adapters.MillAdapter
import com.marwadiuniversity.flourmate.adapters.OrderAdapter
import com.marwadiuniversity.flourmate.adapters.ProductAdapter
import com.marwadiuniversity.flourmate.database.FlourMateDatabase
import com.marwadiuniversity.flourmate.models.Mill
import com.marwadiuniversity.flourmate.models.Order
import com.marwadiuniversity.flourmate.models.OrderItem
import com.marwadiuniversity.flourmate.models.Product
import com.marwadiuniversity.flourmate.models.User
import com.marwadiuniversity.flourmate.network.FirebaseSyncManager
import com.marwadiuniversity.flourmate.repositories.MillRepository
import com.marwadiuniversity.flourmate.repositories.OrderRepository
import com.marwadiuniversity.flourmate.repositories.ProductRepository
import com.marwadiuniversity.flourmate.repositories.UserRepository
import com.marwadiuniversity.flourmate.utils.ImageLoader
import com.marwadiuniversity.flourmate.utils.ImagePickerHelper
import com.marwadiuniversity.flourmate.utils.ImageStorageHelper
import com.marwadiuniversity.flourmate.utils.SessionManager
import java.util.Stack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    sealed class Screen {
        object Splash : Screen()
        object Login : Screen()
        object Register : Screen()
        object ForgotPassword : Screen()
        object CustomerHome : Screen()
        object Mills : Screen()
        data class MillDetails(val millId: Long) : Screen()
        data class Products(val category: String = "All") : Screen()
        object Cart : Screen()
        object Checkout : Screen()
        object OrderSuccess : Screen()
        object Orders : Screen()
        data class OrderDetails(val orderId: Long) : Screen()
        object CustomerProfile : Screen()
        object MillOwnerDashboard : Screen()
        object ManageOrders : Screen()
        object ManageProducts : Screen()
        data class AddProduct(val productId: Long) : Screen()
        object Earnings : Screen()
        object ManageShopPhotos : Screen()
        object MillOwnerProfile : Screen()
        object DeliveryHome : Screen()
        object DeliveryOrders : Screen()
        data class DeliveryDetails(val orderId: Long) : Screen()
        object DeliveryProfile : Screen()
        object DeliveryEarnings : Screen()
        object Search : Screen()
        object Notifications : Screen()
    }

    private lateinit var mainContainer: FrameLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var db: FlourMateDatabase
    private lateinit var userRepository: UserRepository
    private lateinit var millRepository: MillRepository
    private lateinit var productRepository: ProductRepository
    private lateinit var orderRepository: OrderRepository

    private val navigationStack = Stack<Screen>()
    private var onImageProcessedCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainContainer = findViewById(R.id.mainContainer)
        sessionManager = SessionManager(this)
        db = FlourMateDatabase.getDatabase(applicationContext)

        userRepository = UserRepository(db.userDao())
        millRepository = MillRepository(db.millDao())
        productRepository = ProductRepository(db.productDao(), db.cartDao())
        orderRepository = OrderRepository(db.orderDao(), db.orderItemDao(), db.notificationDao())

        // Start Firebase Sync Engine
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.startRealTimeSync(applicationContext)
        }

        // Handle System Back Button for Single Activity
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navigationStack.size > 1) {
                    navigationStack.pop()
                    renderScreen(navigationStack.peek())
                } else {
                    finish()
                }
            }
        })

        // Initial launch screen
        navigateTo(Screen.Splash, clearStack = true)
    }

    fun navigateTo(screen: Screen, clearStack: Boolean = false) {
        if (clearStack) {
            navigationStack.clear()
        }
        navigationStack.push(screen)
        renderScreen(screen)
    }

    private fun renderScreen(screen: Screen) {
        mainContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        when (screen) {
            is Screen.Splash -> renderSplash(inflater)
            is Screen.Login -> renderLogin(inflater)
            is Screen.Register -> renderRegister(inflater)
            is Screen.ForgotPassword -> renderForgotPassword(inflater)
            is Screen.CustomerHome -> renderCustomerHome(inflater)
            is Screen.Mills -> renderMills(inflater)
            is Screen.MillDetails -> renderMillDetails(inflater, screen.millId)
            is Screen.Products -> renderProducts(inflater, screen.category)
            is Screen.Cart -> renderCart(inflater)
            is Screen.Checkout -> renderCheckout(inflater)
            is Screen.OrderSuccess -> renderOrderSuccess(inflater)
            is Screen.Orders -> renderOrders(inflater)
            is Screen.OrderDetails -> renderOrderDetails(inflater, screen.orderId)
            is Screen.CustomerProfile -> renderCustomerProfile(inflater)
            is Screen.MillOwnerDashboard -> renderMillOwnerDashboard(inflater)
            is Screen.ManageOrders -> renderManageOrders(inflater)
            is Screen.ManageProducts -> renderManageProducts(inflater)
            is Screen.AddProduct -> renderAddProduct(inflater, screen.productId)
            is Screen.Earnings -> renderEarnings(inflater)
            is Screen.ManageShopPhotos -> renderManageShopPhotos(inflater)
            is Screen.MillOwnerProfile -> renderMillOwnerProfile(inflater)
            is Screen.DeliveryHome -> renderDeliveryHome(inflater)
            is Screen.DeliveryOrders -> renderDeliveryOrders(inflater)
            is Screen.DeliveryDetails -> renderDeliveryDetails(inflater, screen.orderId)
            is Screen.DeliveryProfile -> renderDeliveryProfile(inflater)
            is Screen.DeliveryEarnings -> renderDeliveryEarnings(inflater)
            is Screen.Search -> renderSearch(inflater)
            is Screen.Notifications -> renderNotifications(inflater)
        }
    }

    // ==========================================
    // 1. SPLASH SCREEN
    // ==========================================
    private fun renderSplash(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_splash, mainContainer, false)
        mainContainer.addView(view)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isLoggedIn()) {
                when (sessionManager.getUserRole()) {
                    "MILL_OWNER" -> navigateTo(Screen.MillOwnerDashboard, clearStack = true)
                    "DELIVERY" -> navigateTo(Screen.DeliveryHome, clearStack = true)
                    else -> navigateTo(Screen.CustomerHome, clearStack = true)
                }
            } else {
                navigateTo(Screen.Login, clearStack = true)
            }
        }, 1500)
    }

    // ==========================================
    // 2. LOGIN SCREEN
    // ==========================================
    private fun renderLogin(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_login, mainContainer, false)
        mainContainer.addView(view)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvRegisterLink = view.findViewById<TextView>(R.id.tvCreateAccount)
        val tvForgotPasswordLink = view.findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Please enter email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Please enter password"
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val user = userRepository.loginUser(email, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        val mill = millRepository.getMillByOwnerId(user.id)
                        sessionManager.saveUserSession(
                            userId = user.id,
                            name = user.name,
                            email = user.email,
                            phone = user.phone,
                            role = user.role,
                            millId = mill?.id ?: 0L,
                            millName = mill?.name ?: user.millName,
                            address = user.millAddress.ifEmpty { user.address }
                        )

                        Toast.makeText(this@MainActivity, "Login Successful! Welcome ${user.name}", Toast.LENGTH_SHORT).show()

                        when (user.role) {
                            "MILL_OWNER" -> navigateTo(Screen.MillOwnerDashboard, clearStack = true)
                            "DELIVERY" -> navigateTo(Screen.DeliveryHome, clearStack = true)
                            else -> navigateTo(Screen.CustomerHome, clearStack = true)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvRegisterLink.setOnClickListener { navigateTo(Screen.Register) }
        tvForgotPasswordLink.setOnClickListener { navigateTo(Screen.ForgotPassword) }
    }

    // ==========================================
    // 3. REGISTER SCREEN
    // ==========================================
    private fun renderRegister(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_register, mainContainer, false)
        mainContainer.addView(view)

        val etFullName = view.findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etMobile = view.findViewById<TextInputEditText>(R.id.etMobile)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val rgRole = view.findViewById<RadioGroup>(R.id.rgRole)
        val llMillOwnerFields = view.findViewById<LinearLayout>(R.id.llMillOwnerFields)
        val etMillName = view.findViewById<TextInputEditText>(R.id.etMillName)
        val etMillAddress = view.findViewById<TextInputEditText>(R.id.etMillAddress)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val tvLoginLink = view.findViewById<TextView>(R.id.tvLoginLink)

        rgRole.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            llMillOwnerFields.visibility = if (checkedId == R.id.rbMillOwner) View.VISIBLE else View.GONE
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) { etFullName.error = "Please enter full name"; return@setOnClickListener }
            if (email.isEmpty()) { etEmail.error = "Please enter email"; return@setOnClickListener }
            if (mobile.isEmpty()) { etMobile.error = "Please enter mobile number"; return@setOnClickListener }
            if (password.length < 4) { etPassword.error = "Password must be at least 4 characters"; return@setOnClickListener }
            if (password != confirmPassword) { etConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }

            val role = when (rgRole.checkedRadioButtonId) {
                R.id.rbMillOwner -> "MILL_OWNER"
                R.id.rbDelivery -> "DELIVERY"
                else -> "CUSTOMER"
            }

            val millName = etMillName.text.toString().trim()
            val millAddress = etMillAddress.text.toString().trim()

            if (role == "MILL_OWNER") {
                if (millName.isEmpty()) { etMillName.error = "Please enter flour mill name"; return@setOnClickListener }
                if (millAddress.isEmpty()) { etMillAddress.error = "Please enter flour mill address"; return@setOnClickListener }
            }

            CoroutineScope(Dispatchers.IO).launch {
                val existingUser = userRepository.loginUser(email, password)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Email is already registered!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val newUser = User(
                    name = fullName,
                    email = email,
                    phone = mobile,
                    passwordHash = password,
                    role = role,
                    millName = millName,
                    millAddress = millAddress
                )

                val userId = userRepository.registerUser(newUser)
                var millId = 0L

                if (role == "MILL_OWNER") {
                    millId = millRepository.createMill(
                        Mill(
                            ownerId = userId,
                            name = millName,
                            address = millAddress,
                            phone = mobile,
                            rating = 4.8
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    sessionManager.saveUserSession(
                        userId = userId,
                        name = fullName,
                        email = email,
                        phone = mobile,
                        role = role,
                        millId = millId,
                        millName = millName,
                        address = if (role == "MILL_OWNER") millAddress else "123 Gandhi Road, Rajkot"
                    )

                    Toast.makeText(this@MainActivity, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                    when (role) {
                        "MILL_OWNER" -> navigateTo(Screen.MillOwnerDashboard, clearStack = true)
                        "DELIVERY" -> navigateTo(Screen.DeliveryHome, clearStack = true)
                        else -> navigateTo(Screen.CustomerHome, clearStack = true)
                    }
                }
            }
        }

        tvLoginLink.setOnClickListener { navigateTo(Screen.Login) }
    }

    // ==========================================
    // 4. FORGOT PASSWORD SCREEN
    // ==========================================
    private fun renderForgotPassword(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_forgot_password, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)
        val btnResetPassword = view.findViewById<MaterialButton>(R.id.btnSendOtp)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnResetPassword.setOnClickListener {
            Toast.makeText(this, "Password reset link sent to email", Toast.LENGTH_SHORT).show()
            navigateTo(Screen.Login)
        }
    }

    // ==========================================
    // 5. CUSTOMER HOME DASHBOARD
    // ==========================================
    private fun renderCustomerHome(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_customer_home, mainContainer, false)
        mainContainer.addView(view)

        val tvWelcomeUser = view.findViewById<TextView>(R.id.tvLocation)
        val tvFeaturedMillName = view.findViewById<TextView>(R.id.tvFeaturedMillName)
        val ivFeaturedMillImage = view.findViewById<ImageView>(R.id.ivFeaturedMillImage)
        val btnViewAllMills = view.findViewById<TextView>(R.id.tvSeeAllMills)
        val cardFeaturedMill = view.findViewById<MaterialCardView>(R.id.cardSampleMill)
        val btnViewMill = view.findViewById<MaterialButton>(R.id.btnViewMill)
        val recyclerPopularProducts = view.findViewById<RecyclerView>(R.id.rvPopularProducts)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        val cardNearbyMills = view.findViewById<MaterialCardView>(R.id.cardNearbyMills)
        val cardMyOrders = view.findViewById<MaterialCardView>(R.id.cardMyOrders)
        val cardCart = view.findViewById<MaterialCardView>(R.id.cardCart)
        val cardTrackOrder = view.findViewById<MaterialCardView>(R.id.cardTrackOrder)
        val cardCustomGrindingBanner = view.findViewById<MaterialCardView>(R.id.cardCustomGrindingBanner)
        val ivNotification = view.findViewById<ImageView>(R.id.ivNotification)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)

        tvWelcomeUser.text = "Hello, ${sessionManager.getUserName()} 👋"

        recyclerPopularProducts.layoutManager = GridLayoutManager(this, 2)
        val productAdapter = ProductAdapter(emptyList(), onAddToCartClick = { product ->
            val userId = sessionManager.getUserId()
            if (userId <= 0) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                navigateTo(Screen.Login)
                return@ProductAdapter
            }
            CoroutineScope(Dispatchers.IO).launch {
                productRepository.addToCart(userId, product)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                }
            }
        }, onItemClick = { product ->
            navigateTo(Screen.MillDetails(product.millId))
        })
        recyclerPopularProducts.adapter = productAdapter

        btnViewAllMills.setOnClickListener { navigateTo(Screen.Mills) }
        cardFeaturedMill.setOnClickListener { navigateTo(Screen.Mills) }
        btnViewMill?.setOnClickListener { navigateTo(Screen.Mills) }
        cardNearbyMills?.setOnClickListener { navigateTo(Screen.Mills) }
        cardMyOrders?.setOnClickListener { navigateTo(Screen.Orders) }
        cardCart?.setOnClickListener { navigateTo(Screen.Cart) }
        cardTrackOrder?.setOnClickListener { navigateTo(Screen.Orders) }
        cardCustomGrindingBanner?.setOnClickListener { navigateTo(Screen.Mills) }
        ivNotification?.setOnClickListener { navigateTo(Screen.Notifications) }
        ivProfile?.setOnClickListener { navigateTo(Screen.CustomerProfile) }
        etSearch.setOnClickListener { navigateTo(Screen.Search) }

        setupCustomerBottomNav(bottomNavigation, R.id.nav_home)

        // Load data
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.fetchCloudData(applicationContext)
            val products = productRepository.getAllProducts()
            val mills = millRepository.getAllMills()
            val featuredMill = mills.firstOrNull()

            withContext(Dispatchers.Main) {
                productAdapter.updateProducts(products)
                if (featuredMill != null) {
                    tvFeaturedMillName.text = featuredMill.name
                    ImageLoader.loadImage(ivFeaturedMillImage, featuredMill.imageUri, R.drawable.ic_flour_grain)
                }
            }
        }
    }

    // ==========================================
    // 6. FLOUR MILLS DIRECTORY
    // ==========================================
    private fun renderMills(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_mills, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val llEmptyMills = view.findViewById<LinearLayout>(R.id.llEmptyMills)
        val etSearchMills = view.findViewById<EditText>(R.id.etSearchMills)
        val recyclerMills = view.findViewById<RecyclerView>(R.id.recyclerMills)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

        recyclerMills.layoutManager = LinearLayoutManager(this)

        var allMillsList: List<Mill> = emptyList()
        val millAdapter = MillAdapter(emptyList()) { mill ->
            navigateTo(Screen.MillDetails(mill.id))
        }
        recyclerMills.adapter = millAdapter

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        fun filterMills(query: String) {
            val filtered = if (query.trim().isEmpty()) {
                allMillsList
            } else {
                allMillsList.filter {
                    it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
                }
            }

            if (filtered.isEmpty()) {
                recyclerMills.visibility = View.GONE
                llEmptyMills.visibility = View.VISIBLE
            } else {
                recyclerMills.visibility = View.VISIBLE
                llEmptyMills.visibility = View.GONE
            }
            millAdapter.updateMills(filtered)
        }

        etSearchMills.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMills(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupCustomerBottomNav(bottomNavigation, R.id.nav_mills)

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.fetchCloudData(applicationContext)
            allMillsList = millRepository.getAllMills()
            withContext(Dispatchers.Main) {
                filterMills(etSearchMills.text.toString())
            }
        }
    }

    // ==========================================
    // 7. MILL DETAILS
    // ==========================================
    private fun renderMillDetails(inflater: LayoutInflater, millId: Long) {
        val view = inflater.inflate(R.layout.activity_mill_details, mainContainer, false)
        mainContainer.addView(view)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val tvMillName = view.findViewById<TextView>(R.id.tvMillName)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val ivHeaderBanner = view.findViewById<ImageView>(R.id.ivHeaderBanner)
        val ivMillIconCenter = view.findViewById<ImageView>(R.id.ivMillIconCenter)
        val btnCall = view.findViewById<MaterialButton>(R.id.btnCall)
        val btnDirections = view.findViewById<MaterialButton>(R.id.btnDirections)
        val recyclerProducts = view.findViewById<RecyclerView>(R.id.recyclerProducts)
        val tvEmptyProducts = view.findViewById<TextView>(R.id.tvEmptyProducts)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerProducts.layoutManager = LinearLayoutManager(this)
        val productAdapter = ProductAdapter(emptyList(), onAddToCartClick = { product ->
            val userId = sessionManager.getUserId()
            if (userId <= 0) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                navigateTo(Screen.Login)
                return@ProductAdapter
            }
            CoroutineScope(Dispatchers.IO).launch {
                productRepository.addToCart(userId, product)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        recyclerProducts.adapter = productAdapter

        CoroutineScope(Dispatchers.IO).launch {
            val mill = millRepository.getMillById(millId)
            val products = productRepository.getProductsByMillId(millId)

            withContext(Dispatchers.Main) {
                if (mill != null) {
                    tvMillName.text = mill.name
                    tvAddress.text = "📍 ${mill.address}"

                    if (mill.imageUri.isNotEmpty()) {
                        ivHeaderBanner.visibility = View.VISIBLE
                        ImageLoader.loadImage(ivHeaderBanner, mill.imageUri, R.drawable.ic_flour_grain)
                        ImageLoader.loadImage(ivMillIconCenter, mill.imageUri, R.drawable.ic_flour_grain)
                    }

                    btnCall.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mill.phone}"))
                        startActivity(intent)
                    }

                    btnDirections.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${mill.address}"))
                        startActivity(intent)
                    }
                }

                if (products.isEmpty()) {
                    recyclerProducts.visibility = View.GONE
                    tvEmptyProducts.visibility = View.VISIBLE
                } else {
                    recyclerProducts.visibility = View.VISIBLE
                    tvEmptyProducts.visibility = View.GONE
                    productAdapter.updateProducts(products)
                }
            }
        }
    }

    // ==========================================
    // 8. PRODUCTS CATEGORY SCREEN
    // ==========================================
    private fun renderProducts(inflater: LayoutInflater, category: String) {
        val view = inflater.inflate(R.layout.activity_products, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val recyclerProducts = view.findViewById<RecyclerView>(R.id.recyclerProducts)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)

        val productAdapter = ProductAdapter(emptyList(), onAddToCartClick = { product ->
            val userId = sessionManager.getUserId()
            CoroutineScope(Dispatchers.IO).launch {
                productRepository.addToCart(userId, product)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        recyclerProducts.adapter = productAdapter
        setupCustomerBottomNav(bottomNavigation, R.id.nav_home)

        CoroutineScope(Dispatchers.IO).launch {
            val all = productRepository.getAllProducts()
            val filtered = if (category == "All") all else all.filter { it.category.equals(category, ignoreCase = true) }
            withContext(Dispatchers.Main) {
                productAdapter.updateProducts(filtered)
            }
        }
    }

    // ==========================================
    // 9. SHOPPING CART
    // ==========================================
    private fun renderCart(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_cart, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val llEmptyCart = view.findViewById<LinearLayout>(R.id.llEmptyCart)
        val cvSummary = view.findViewById<MaterialCardView>(R.id.cvSummary)
        val recyclerCart = view.findViewById<RecyclerView>(R.id.recyclerCart)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val btnCheckout = view.findViewById<MaterialButton>(R.id.btnProceed)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

        recyclerCart.layoutManager = LinearLayoutManager(this)

        var cartAdapter: CartAdapter? = null
        val userId = sessionManager.getUserId()

        fun calculateTotals(items: List<com.marwadiuniversity.flourmate.models.CartItem>) {
            val subtotal = items.sumOf { it.price * it.quantity }
            val deliveryFee = if (items.isEmpty()) 0.0 else 30.0
            val total = subtotal + deliveryFee

            tvTotal?.text = "₹${"%.2f".format(total)}"
        }

        fun loadCartItems() {
            CoroutineScope(Dispatchers.IO).launch {
                val items = productRepository.getCartItems(userId)
                withContext(Dispatchers.Main) {
                    if (items.isEmpty()) {
                        recyclerCart?.visibility = View.GONE
                        cvSummary?.visibility = View.GONE
                        llEmptyCart?.visibility = View.VISIBLE
                    } else {
                        recyclerCart?.visibility = View.VISIBLE
                        cvSummary?.visibility = View.VISIBLE
                        llEmptyCart?.visibility = View.GONE
                        cartAdapter?.updateItems(items)
                        calculateTotals(items)
                    }
                }
            }
        }

        cartAdapter = CartAdapter(
            cartItems = emptyList(),
            onQuantityChange = { item, newQuantity ->
                CoroutineScope(Dispatchers.IO).launch {
                    productRepository.updateCartQuantity(item.id, newQuantity, userId)
                    loadCartItems()
                }
            },
            onDeleteItem = { item ->
                CoroutineScope(Dispatchers.IO).launch {
                    productRepository.removeCartItem(item.id)
                    loadCartItems()
                }
            }
        )
        recyclerCart.adapter = cartAdapter

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnCheckout.setOnClickListener { navigateTo(Screen.Checkout) }
        setupCustomerBottomNav(bottomNavigation, R.id.nav_cart)

        loadCartItems()
    }

    // ==========================================
    // 10. CHECKOUT SCREEN
    // ==========================================
    private fun renderCheckout(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_checkout, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val rgFulfillment = view.findViewById<RadioGroup>(R.id.rgFulfillment)
        val tvFulfillmentLabel = view.findViewById<TextView>(R.id.tvFulfillmentLabel)
        val tvAddressText = view.findViewById<TextView>(R.id.tvAddressText)
        val tvSubtotalVal = view.findViewById<TextView>(R.id.tvSubtotalVal)
        val tvDeliveryFeeVal = view.findViewById<TextView>(R.id.tvDeliveryFeeVal)
        val tvTotalVal = view.findViewById<TextView>(R.id.tvTotalVal)
        val btnPlaceOrder = view.findViewById<MaterialButton>(R.id.btnPlaceOrder)

        val userId = sessionManager.getUserId()
        var currentOrderType = "DELIVERY"
        var currentSubtotal = 0.0
        var currentDeliveryFee = 30.0
        var cartItems: List<com.marwadiuniversity.flourmate.models.CartItem> = emptyList()

        tvAddressText.text = sessionManager.getUserAddress()

        fun updatePricing() {
            currentDeliveryFee = if (currentOrderType == "PICKUP") 0.0 else 30.0
            val total = currentSubtotal + currentDeliveryFee

            tvSubtotalVal.text = "₹${"%.2f".format(currentSubtotal)}"
            tvDeliveryFeeVal.text = "₹${"%.2f".format(currentDeliveryFee)}"
            tvTotalVal.text = "₹${"%.2f".format(total)}"
        }

        rgFulfillment.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            if (checkedId == R.id.rbMillPickup) {
                currentOrderType = "PICKUP"
                tvFulfillmentLabel.text = "Mill Pickup Address"
            } else {
                currentOrderType = "DELIVERY"
                tvFulfillmentLabel.text = "Home Delivery Address"
            }
            updatePricing()
        }

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnPlaceOrder.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val firstItem = cartItems.first()
                val orderNumber = "FM${(100000..999999).random()}"
                val total = currentSubtotal + currentDeliveryFee

                val newOrder = Order(
                    orderNumber = orderNumber,
                    customerId = userId,
                    customerName = sessionManager.getUserName(),
                    customerPhone = sessionManager.getUserPhone(),
                    millId = firstItem.millId,
                    millName = firstItem.millName,
                    pickupAddress = "Flour Mill Campus, Rajkot",
                    deliveryAddress = if (currentOrderType == "PICKUP") "Mill Pickup" else sessionManager.getUserAddress(),
                    totalAmount = total,
                    paymentMethod = "COD",
                    status = "Pending",
                    timestamp = System.currentTimeMillis(),
                    otp = "${(1000..9999).random()}",
                    itemCount = cartItems.sumOf { it.quantity },
                    orderType = currentOrderType,
                    subtotal = currentSubtotal,
                    deliveryFee = currentDeliveryFee
                )

                val orderItems = cartItems.map {
                    OrderItem(
                        orderId = 0L,
                        productId = it.productId,
                        productName = it.productName,
                        price = it.price,
                        quantity = it.quantity
                    )
                }

                orderRepository.placeOrder(newOrder, orderItems)
                productRepository.clearCart(userId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Order Placed Successfully! 🎉", Toast.LENGTH_SHORT).show()
                    navigateTo(Screen.OrderSuccess, clearStack = true)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            cartItems = productRepository.getCartItems(userId)
            currentSubtotal = cartItems.sumOf { it.price * it.quantity }
            withContext(Dispatchers.Main) {
                updatePricing()
            }
        }
    }

    // ==========================================
    // 11. ORDER SUCCESS SCREEN
    // ==========================================
    private fun renderOrderSuccess(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_order_success, mainContainer, false)
        mainContainer.addView(view)

        val btnTrackOrder = view.findViewById<MaterialButton>(R.id.btnTrackOrder)
        val btnContinue = view.findViewById<MaterialButton>(R.id.btnContinue)

        btnTrackOrder.setOnClickListener { navigateTo(Screen.Orders, clearStack = true) }
        btnContinue.setOnClickListener { navigateTo(Screen.CustomerHome, clearStack = true) }
    }

    // ==========================================
    // 12. CUSTOMER MY ORDERS SCREEN
    // ==========================================
    private fun renderOrders(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_orders, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val cgOrderFilters = view.findViewById<ChipGroup>(R.id.cgOrderFilters)
        val llEmptyOrders = view.findViewById<LinearLayout>(R.id.llEmptyOrders)
        val recyclerOrders = view.findViewById<RecyclerView>(R.id.recyclerOrders)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        recyclerOrders.layoutManager = LinearLayoutManager(this)

        var allOrdersList: List<Order> = emptyList()
        val orderAdapter = OrderAdapter(emptyList()) { order ->
            navigateTo(Screen.OrderDetails(order.id))
        }
        recyclerOrders.adapter = orderAdapter

        fun filterOrders(filterText: String) {
            val filtered = when (filterText) {
                "Pending" -> allOrdersList.filter { it.status.equals("Pending", ignoreCase = true) }
                "Preparing" -> allOrdersList.filter { it.status.equals("Preparing", ignoreCase = true) }
                "Out for Delivery" -> allOrdersList.filter { it.status.equals("Out for Delivery", ignoreCase = true) }
                "Ready for Pickup" -> allOrdersList.filter { it.status.equals("Ready for Pickup", ignoreCase = true) }
                "Delivered" -> allOrdersList.filter { it.status.equals("Delivered", ignoreCase = true) || it.status.equals("Completed", ignoreCase = true) }
                else -> allOrdersList
            }

            if (filtered.isEmpty()) {
                recyclerOrders.visibility = View.GONE
                llEmptyOrders.visibility = View.VISIBLE
            } else {
                recyclerOrders.visibility = View.VISIBLE
                llEmptyOrders.visibility = View.GONE
            }
            orderAdapter.updateOrders(filtered)
        }

        cgOrderFilters.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
            if (checkedIds.isEmpty()) {
                filterOrders("All")
            } else {
                val chip = group.findViewById<Chip>(checkedIds[0])
                filterOrders(chip?.text?.toString() ?: "All")
            }
        }

        setupCustomerBottomNav(bottomNavigation, R.id.nav_orders)

        val userId = sessionManager.getUserId()
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.fetchCloudData(applicationContext)
            allOrdersList = orderRepository.getOrdersForCustomer(userId)
            withContext(Dispatchers.Main) {
                filterOrders("All")
            }
        }
    }

    // ==========================================
    // 13. ORDER DETAILS SCREEN
    // ==========================================
    private fun renderOrderDetails(inflater: LayoutInflater, orderId: Long) {
        val view = inflater.inflate(R.layout.activity_order_details, mainContainer, false)
        mainContainer.addView(view)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val tvOrderHeaderId = view.findViewById<TextView>(R.id.tvOrderHeaderId)
        val tvDeliveryAddress = view.findViewById<TextView>(R.id.tvDeliveryAddress)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        CoroutineScope(Dispatchers.IO).launch {
            val order = orderRepository.getOrderById(orderId)
            withContext(Dispatchers.Main) {
                if (order != null) {
                    tvOrderHeaderId.text = "Order #${order.orderNumber} • ${order.status}"
                    tvDeliveryAddress.text = order.deliveryAddress
                }
            }
        }
    }

    // ==========================================
    // 14. CUSTOMER PROFILE SCREEN
    // ==========================================
    private fun renderCustomerProfile(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_customer_profile, mainContainer, false)
        mainContainer.addView(view)

        val cardAvatar = view.findViewById<MaterialCardView>(R.id.cardAvatar)
        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val llEditProfile = view.findViewById<LinearLayout>(R.id.llEditProfile)
        val llLogout = view.findViewById<LinearLayout>(R.id.llLogout)
        val bottomNavigation = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val userId = sessionManager.getUserId()
        var currentUser: User? = null

        fun loadUserData() {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userRepository.getUserById(userId)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        currentUser = user
                        tvUserName.text = user.name
                        tvUserEmail.text = "${user.phone} • ${user.email}"
                        ImageLoader.loadImage(ivProfileImage, user.profileImageUri, R.drawable.ic_person)
                    }
                }
            }
        }

        cardAvatar.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            ImagePickerHelper.showImagePickerDialog(
                activity = this,
                onRemoveSelected = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updated = user.copy(profileImageUri = "")
                        userRepository.updateUser(updated)
                        withContext(Dispatchers.Main) { loadUserData() }
                    }
                }
            )
            onImageProcessedCallback = { savedPath ->
                CoroutineScope(Dispatchers.IO).launch {
                    val updated = user.copy(profileImageUri = savedPath)
                    userRepository.updateUser(updated)
                    withContext(Dispatchers.Main) { loadUserData() }
                }
            }
        }

        llEditProfile.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            val etName = EditText(this).apply { setText(user.name); setPadding(32, 24, 32, 24) }
            val etPhone = EditText(this).apply { setText(user.phone); setPadding(32, 24, 32, 24) }
            val etAddr = EditText(this).apply { setText(user.address); setPadding(32, 24, 32, 24) }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 20, 40, 20)
                addView(etName)
                addView(etPhone)
                addView(etAddr)
            }

            AlertDialog.Builder(this)
                .setTitle("Edit Customer Profile")
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val newName = etName.text.toString().trim()
                    val newPhone = etPhone.text.toString().trim()
                    val newAddr = etAddr.text.toString().trim()

                    CoroutineScope(Dispatchers.IO).launch {
                        val updated = user.copy(name = newName, phone = newPhone, address = newAddr)
                        userRepository.updateUser(updated)
                        sessionManager.saveUserSession(
                            userId = updated.id,
                            name = updated.name,
                            email = updated.email,
                            phone = updated.phone,
                            role = updated.role,
                            millId = sessionManager.getMillId(),
                            millName = sessionManager.getMillName(),
                            address = updated.address
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            loadUserData()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val llDeveloperCredit = view.findViewById<LinearLayout>(R.id.llDeveloperCredit)
        llDeveloperCredit?.setOnClickListener {
            val logo = ImageView(this).apply {
                setImageResource(R.drawable.ic_flourmate_logo)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    gravity = android.view.Gravity.CENTER
                    bottomMargin = 24
                }
            }

            val tvTitle = TextView(this).apply {
                text = "Developer Credit"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.primary_green))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }

            val tvText = TextView(this).apply {
                text = "Developed by Meet Sanghani"
                textSize = 15f
                setTextColor(getColor(R.color.main_text))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 0, 16, 16)
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 36, 48, 24)
                addView(logo)
                addView(tvTitle)
                addView(tvText)
            }

            AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show()
        }

        llLogout.setOnClickListener {
            sessionManager.logout()
            navigateTo(Screen.Login, clearStack = true)
        }

        setupCustomerBottomNav(bottomNavigation, R.id.nav_profile)
        loadUserData()
    }

    // ==========================================
    // 15. MILL OWNER DASHBOARD
    // ==========================================
    private fun renderMillOwnerDashboard(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_mill_owner_dashboard, mainContainer, false)
        mainContainer.addView(view)

        val tvWelcomeMillName = view.findViewById<TextView>(R.id.tvWelcomeMillName)
        val tvStatTotalSales = view.findViewById<TextView>(R.id.tvStatTotalSales)
        val tvStatEarnings = view.findViewById<TextView>(R.id.tvStatEarnings)
        val tvStatPendingOrders = view.findViewById<TextView>(R.id.tvStatPendingOrders)
        val tvStatTodayOrders = view.findViewById<TextView>(R.id.tvStatTodayOrders)
        val rvRecentOrders = view.findViewById<RecyclerView>(R.id.rvRecentOrders)
        val bottomNavigationMill = view.findViewById<BottomNavigationView>(R.id.bottomNavigationMill)

        tvWelcomeMillName.text = "Welcome, ${sessionManager.getMillName()}"
        rvRecentOrders.layoutManager = LinearLayoutManager(this)

        val orderAdapter = OrderAdapter(emptyList()) { order ->
            navigateTo(Screen.ManageOrders)
        }
        rvRecentOrders.adapter = orderAdapter

        setupMillOwnerBottomNav(bottomNavigationMill, R.id.nav_dashboard)

        val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.fetchCloudData(applicationContext)
            val orders = orderRepository.getOrdersForMill(millId)

            val totalRevenue = orders.filter { it.status == "Delivered" || it.status == "Completed" }.sumOf { it.totalAmount }
            val activeOrders = orders.filter { it.status != "Delivered" && it.status != "Completed" && it.status != "Cancelled" }

            withContext(Dispatchers.Main) {
                tvStatTotalSales.text = "₹${"%.2f".format(totalRevenue)}"
                tvStatEarnings.text = "₹${"%.2f".format(totalRevenue)}"
                tvStatPendingOrders.text = activeOrders.size.toString()
                tvStatTodayOrders.text = orders.size.toString()
                orderAdapter.updateOrders(orders.take(5))
            }
        }
    }

    // ==========================================
    // 16. MANAGE ORDERS (MILL OWNER)
    // ==========================================
    private fun renderManageOrders(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_manage_orders, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val cgOrderStates = view.findViewById<ChipGroup>(R.id.cgOrderStates)
        val recyclerManageOrders = view.findViewById<RecyclerView>(R.id.recyclerManageOrders)
        val bottomNavigationMill = view.findViewById<BottomNavigationView>(R.id.bottomNavigationMill)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        recyclerManageOrders.layoutManager = LinearLayoutManager(this)

        var allMillOrders: List<Order> = emptyList()
        val orderAdapter = OrderAdapter(emptyList()) { order ->
            navigateTo(Screen.OrderDetails(order.id))
        }
        recyclerManageOrders.adapter = orderAdapter

        fun filterOrders(filterText: String) {
            val filtered = when (filterText) {
                "Pending" -> allMillOrders.filter { it.status.equals("Pending", ignoreCase = true) }
                "Preparing" -> allMillOrders.filter { it.status.equals("Preparing", ignoreCase = true) }
                "Ready for Pickup" -> allMillOrders.filter { it.status.equals("Ready for Pickup", ignoreCase = true) || it.status.equals("Out for Delivery", ignoreCase = true) }
                else -> allMillOrders
            }
            orderAdapter.updateOrders(filtered)
        }

        cgOrderStates.setOnCheckedStateChangeListener { group: ChipGroup, checkedIds: List<Int> ->
            if (checkedIds.isEmpty()) {
                filterOrders("All")
            } else {
                val chip = group.findViewById<Chip>(checkedIds[0])
                filterOrders(chip?.text?.toString() ?: "All")
            }
        }

        setupMillOwnerBottomNav(bottomNavigationMill, R.id.nav_manage_orders)

        val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseSyncManager.fetchCloudData(applicationContext)
            allMillOrders = orderRepository.getOrdersForMill(millId)
            withContext(Dispatchers.Main) {
                filterOrders("All")
            }
        }
    }

    // ==========================================
    // 17. MANAGE PRODUCTS (MILL OWNER)
    // ==========================================
    private fun renderManageProducts(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_manage_products, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnAddProduct = view.findViewById<MaterialButton>(R.id.btnAddProduct)
        val recyclerManageProducts = view.findViewById<RecyclerView>(R.id.recyclerManageProducts)
        val bottomNavigationMill = view.findViewById<BottomNavigationView>(R.id.bottomNavigationMill)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnAddProduct.setOnClickListener { navigateTo(Screen.AddProduct(0L)) }
        recyclerManageProducts.layoutManager = LinearLayoutManager(this)

        var productAdapter: ProductAdapter? = null
        val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }

        fun loadProducts() {
            CoroutineScope(Dispatchers.IO).launch {
                val products = productRepository.getProductsByMillId(millId)
                withContext(Dispatchers.Main) {
                    productAdapter?.updateProducts(products)
                }
            }
        }

        productAdapter = ProductAdapter(
            products = emptyList(),
            onAddToCartClick = { product -> navigateTo(Screen.AddProduct(product.id)) },
            onItemClick = { product -> navigateTo(Screen.AddProduct(product.id)) },
            onDeleteClick = { product ->
                CoroutineScope(Dispatchers.IO).launch {
                    productRepository.deleteProduct(product)
                    if (product.imageUri.isNotEmpty()) {
                        ImageStorageHelper.deleteImageFile(product.imageUri)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Product deleted successfully!", Toast.LENGTH_SHORT).show()
                        loadProducts()
                    }
                }
            }
        )
        recyclerManageProducts.adapter = productAdapter

        setupMillOwnerBottomNav(bottomNavigationMill, R.id.nav_manage_products)
        loadProducts()
    }

    // ==========================================
    // 18. ADD / EDIT PRODUCT
    // ==========================================
    private fun renderAddProduct(inflater: LayoutInflater, productId: Long) {
        val view = inflater.inflate(R.layout.activity_add_product, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tvAddProductTitle = view.findViewById<TextView>(R.id.tvAddProductTitle)
        val cardUploadImage = view.findViewById<MaterialCardView>(R.id.cardUploadImage)
        val ivProductImage = view.findViewById<ImageView>(R.id.ivProductImage)
        val tvUploadImage = view.findViewById<TextView>(R.id.tvUploadImage)
        val etProductName = view.findViewById<TextInputEditText>(R.id.etProductName)
        val etCategory = view.findViewById<TextInputEditText>(R.id.etCategory)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etStock = view.findViewById<TextInputEditText>(R.id.etStock)
        val cgVariants = view.findViewById<ChipGroup>(R.id.cgVariants)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)
        val btnAddProduct = view.findViewById<MaterialButton>(R.id.btnAddProduct)

        var currentImageUri = ""
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (productId > 0) {
            tvAddProductTitle.text = "Edit Flour Product"
            btnAddProduct.text = "UPDATE PRODUCT"
            CoroutineScope(Dispatchers.IO).launch {
                val prod = productRepository.getProductById(productId)
                withContext(Dispatchers.Main) {
                    if (prod != null) {
                        etProductName.setText(prod.name)
                        etCategory.setText(prod.category)
                        etPrice.setText(prod.price.toString())
                        etStock.setText(prod.stock.toString())
                        etDescription.setText(prod.description)
                        currentImageUri = prod.imageUri
                        ImageLoader.loadImage(ivProductImage, currentImageUri, R.drawable.ic_flour_grain)
                    }
                }
            }
        }

        cardUploadImage.setOnClickListener {
            ImagePickerHelper.showImagePickerDialog(this)
            onImageProcessedCallback = { savedPath ->
                currentImageUri = savedPath
                ImageLoader.loadImage(ivProductImage, currentImageUri, R.drawable.ic_flour_grain)
                tvUploadImage.text = "Photo selected!"
            }
        }

        btnAddProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val category = etCategory.text.toString().trim().ifEmpty { "Wheat" }
            val priceStr = etPrice.text.toString().trim()
            val stockStr = etStock.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (name.isEmpty()) { etProductName.error = "Enter product name"; return@setOnClickListener }
            if (priceStr.isEmpty()) { etPrice.error = "Enter price"; return@setOnClickListener }

            val price = priceStr.toDoubleOrNull() ?: 50.0
            val stock = stockStr.toIntOrNull() ?: 100

            val selectedChipId = cgVariants.checkedChipId
            val variantStr = if (selectedChipId != -1) {
                view.findViewById<Chip>(selectedChipId)?.text?.toString() ?: "1 kg Pack"
            } else {
                "1 kg Pack"
            }

            val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }
            val millName = sessionManager.getMillName()

            CoroutineScope(Dispatchers.IO).launch {
                val cloudImageUri = if (currentImageUri.isNotEmpty()) {
                    FirebaseSyncManager.uploadImageToFirebaseStorage(currentImageUri, "products")
                } else {
                    ""
                }

                val product = Product(
                    id = productId,
                    millId = millId,
                    millName = millName,
                    name = name,
                    category = category,
                    price = price,
                    unitVariant = variantStr,
                    stock = stock,
                    description = description,
                    imageUri = cloudImageUri
                )

                if (productId > 0) {
                    productRepository.updateProduct(product)
                } else {
                    productRepository.addProduct(product)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Product saved successfully!", Toast.LENGTH_SHORT).show()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    // ==========================================
    // 19. EARNINGS SCREEN (MILL OWNER)
    // ==========================================
    private fun renderEarnings(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_earnings, mainContainer, false)
        mainContainer.addView(view)

        val tvTotalNetRevenue = view.findViewById<TextView>(R.id.tvTotalNetRevenue)
        val tvWeeklyEarningsBadge = view.findViewById<TextView>(R.id.tvWeeklyEarningsBadge)
        val bottomNavigationMill = view.findViewById<BottomNavigationView>(R.id.bottomNavigationMill)

        setupMillOwnerBottomNav(bottomNavigationMill, R.id.nav_earnings)

        val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }
        CoroutineScope(Dispatchers.IO).launch {
            val orders = orderRepository.getOrdersForMill(millId)
            val completed = orders.filter { it.status == "Delivered" || it.status == "Completed" }
            val totalRev = completed.sumOf { it.totalAmount }

            withContext(Dispatchers.Main) {
                tvTotalNetRevenue.text = "₹${"%.2f".format(totalRev)}"
                tvWeeklyEarningsBadge.text = "📅 Weekly Earnings: ₹${"%.2f".format(totalRev)}"
            }
        }
    }

    // ==========================================
    // 20. MANAGE SHOP PHOTOS SCREEN
    // ==========================================
    private fun renderManageShopPhotos(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_manage_shop_photos, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnUploadPhoto = view.findViewById<MaterialButton>(R.id.btnUploadPhoto)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnUploadPhoto.setOnClickListener {
            ImagePickerHelper.showImagePickerDialog(this)
            onImageProcessedCallback = { savedPath ->
                Toast.makeText(this, "Shop photo uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // 21. MILL OWNER PROFILE SCREEN
    // ==========================================
    private fun renderMillOwnerProfile(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_mill_owner_profile, mainContainer, false)
        mainContainer.addView(view)

        val cvProfileImage = view.findViewById<MaterialCardView>(R.id.cvProfileImage)
        val ivMillProfileImage = view.findViewById<ImageView>(R.id.ivMillProfileImage)
        val tvMillName = view.findViewById<TextView>(R.id.tvMillName)
        val tvOwnerName = view.findViewById<TextView>(R.id.tvOwnerName)
        val btnManageShopPhotos = view.findViewById<MaterialButton>(R.id.btnManageShopPhotos)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val bottomNavigationMill = view.findViewById<BottomNavigationView>(R.id.bottomNavigationMill)

        val millId = sessionManager.getMillId().let { if (it <= 0) 1L else it }
        val userId = sessionManager.getUserId()
        var currentMill: Mill? = null
        var currentUser: User? = null

        fun loadData() {
            CoroutineScope(Dispatchers.IO).launch {
                val mill = millRepository.getMillById(millId) ?: millRepository.getMillByOwnerId(userId)
                val user = userRepository.getUserById(userId)
                withContext(Dispatchers.Main) {
                    if (user != null) currentUser = user
                    if (mill != null) {
                        currentMill = mill
                        tvMillName.text = mill.name
                        tvOwnerName.text = "${user?.name ?: sessionManager.getUserName()} (Owner)"
                        ImageLoader.loadImage(ivMillProfileImage, mill.imageUri, R.drawable.ic_mills)
                    }
                }
            }
        }

        cvProfileImage.setOnClickListener {
            val mill = currentMill ?: return@setOnClickListener
            ImagePickerHelper.showImagePickerDialog(
                activity = this,
                onRemoveSelected = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updated = mill.copy(imageUri = "")
                        millRepository.updateMill(updated)
                        withContext(Dispatchers.Main) { loadData() }
                    }
                }
            )
            onImageProcessedCallback = { savedPath ->
                CoroutineScope(Dispatchers.IO).launch {
                    val cloudUrl = FirebaseSyncManager.uploadImageToFirebaseStorage(savedPath, "mills")
                    val updated = mill.copy(imageUri = cloudUrl)
                    millRepository.updateMill(updated)
                    withContext(Dispatchers.Main) { loadData() }
                }
            }
        }

        tvMillName.setOnClickListener {
            val mill = currentMill ?: return@setOnClickListener
            val user = currentUser
            val etOwner = EditText(this).apply { setText(user?.name ?: sessionManager.getUserName()); setPadding(32, 24, 32, 24) }
            val etMill = EditText(this).apply { setText(mill.name); setPadding(32, 24, 32, 24) }
            val etPhone = EditText(this).apply { setText(mill.phone); setPadding(32, 24, 32, 24) }
            val etAddr = EditText(this).apply { setText(mill.address); setPadding(32, 24, 32, 24) }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 20, 40, 20)
                addView(etOwner)
                addView(etMill)
                addView(etPhone)
                addView(etAddr)
            }

            AlertDialog.Builder(this)
                .setTitle("Edit Flour Mill Details")
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val newOwner = etOwner.text.toString().trim()
                    val newMill = etMill.text.toString().trim()
                    val newPhone = etPhone.text.toString().trim()
                    val newAddr = etAddr.text.toString().trim()

                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedMill = mill.copy(name = newMill, address = newAddr, phone = newPhone)
                        millRepository.updateMill(updatedMill)
                        if (user != null) {
                            val updatedUser = user.copy(name = newOwner, phone = newPhone, millName = newMill, millAddress = newAddr)
                            userRepository.updateUser(updatedUser)
                        }
                        sessionManager.saveUserSession(
                            userId = userId,
                            name = newOwner,
                            email = sessionManager.getUserEmail(),
                            phone = newPhone,
                            role = "MILL_OWNER",
                            millId = updatedMill.id,
                            millName = updatedMill.name,
                            address = updatedMill.address
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Mill details updated!", Toast.LENGTH_SHORT).show()
                            loadData()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnManageShopPhotos.setOnClickListener { navigateTo(Screen.ManageShopPhotos) }

        val btnDeveloperCredit = view.findViewById<MaterialButton>(R.id.btnDeveloperCredit)
        btnDeveloperCredit?.setOnClickListener {
            val logo = ImageView(this).apply {
                setImageResource(R.drawable.ic_flourmate_logo)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    gravity = android.view.Gravity.CENTER
                    bottomMargin = 24
                }
            }

            val tvTitle = TextView(this).apply {
                text = "Developer Details 👨‍💻"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.primary_green))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }

            val tvDetails = TextView(this).apply {
                text = "👨‍💻 Developer Name: Meet Sanghani\n\n👨‍🏫 Guided By: Prof. Jigar Dave\n\n🏫 University: Marwadi University"
                textSize = 14f
                setTextColor(getColor(R.color.main_text))
                setPadding(16, 0, 16, 16)
                setLineSpacing(8f, 1f)
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 36, 48, 24)
                addView(logo)
                addView(tvTitle)
                addView(tvDetails)
            }

            AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show()
        }

        btnLogout.setOnClickListener {
            sessionManager.logout()
            navigateTo(Screen.Login, clearStack = true)
        }

        setupMillOwnerBottomNav(bottomNavigationMill, R.id.nav_mill_profile)
        loadData()
    }

    // ==========================================
    // 22. DELIVERY HOME / DASHBOARD
    // ==========================================
    private fun renderDeliveryHome(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_delivery_home, mainContainer, false)
        mainContainer.addView(view)

        val recyclerDeliveryTasks = view.findViewById<RecyclerView>(R.id.rvDeliveryRequests)
        val bottomNavigationDelivery = view.findViewById<BottomNavigationView>(R.id.bottomNavigationDelivery)

        recyclerDeliveryTasks.layoutManager = LinearLayoutManager(this)

        val partnerId = sessionManager.getUserId()
        val orderAdapter = OrderAdapter(emptyList()) { order ->
            navigateTo(Screen.DeliveryDetails(order.id))
        }
        recyclerDeliveryTasks.adapter = orderAdapter

        setupDeliveryBottomNav(bottomNavigationDelivery, R.id.nav_delivery_home)

        CoroutineScope(Dispatchers.IO).launch {
            val orders = orderRepository.getOrdersForDeliveryPartner(partnerId)
            withContext(Dispatchers.Main) {
                orderAdapter.updateOrders(orders)
            }
        }
    }

    // ==========================================
    // 23. DELIVERY ORDERS SCREEN
    // ==========================================
    private fun renderDeliveryOrders(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_delivery_orders, mainContainer, false)
        mainContainer.addView(view)

        val recyclerDeliveryOrders = view.findViewById<RecyclerView>(R.id.rvDeliveries)
        val bottomNavigationDelivery = view.findViewById<BottomNavigationView>(R.id.bottomNavigationDelivery)

        recyclerDeliveryOrders.layoutManager = LinearLayoutManager(this)

        val partnerId = sessionManager.getUserId()
        val orderAdapter = OrderAdapter(emptyList()) { order ->
            navigateTo(Screen.DeliveryDetails(order.id))
        }
        recyclerDeliveryOrders.adapter = orderAdapter

        setupDeliveryBottomNav(bottomNavigationDelivery, R.id.nav_delivery_orders)

        CoroutineScope(Dispatchers.IO).launch {
            val orders = orderRepository.getOrdersForDeliveryPartner(partnerId)
            withContext(Dispatchers.Main) {
                orderAdapter.updateOrders(orders)
            }
        }
    }

    // ==========================================
    // 24. DELIVERY DETAILS SCREEN
    // ==========================================
    private fun renderDeliveryDetails(inflater: LayoutInflater, orderId: Long) {
        val view = inflater.inflate(R.layout.activity_delivery_details, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tvOrderHeader = view.findViewById<TextView>(R.id.tvOrderHeader)
        val tvCustomerName = view.findViewById<TextView>(R.id.tvCustomerName)
        val tvDeliveryAddress = view.findViewById<TextView>(R.id.tvDeliveryAddress)
        val btnCall = view.findViewById<MaterialButton>(R.id.btnCall)
        val btnConfirmDelivery = view.findViewById<MaterialButton>(R.id.btnConfirmDelivery)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        CoroutineScope(Dispatchers.IO).launch {
            val order = orderRepository.getOrderById(orderId)
            withContext(Dispatchers.Main) {
                if (order != null) {
                    tvOrderHeader.text = "Delivery #${order.orderNumber}"
                    tvCustomerName.text = order.customerName
                    tvDeliveryAddress.text = order.deliveryAddress

                    btnCall.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                        startActivity(intent)
                    }

                    btnConfirmDelivery.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            orderRepository.updateOrderStatus(order.id, "Delivered", order.customerId)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Order marked Delivered!", Toast.LENGTH_SHORT).show()
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 25. DELIVERY PROFILE SCREEN
    // ==========================================
    private fun renderDeliveryProfile(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_delivery_profile, mainContainer, false)
        mainContainer.addView(view)

        val cvProfileImage = view.findViewById<MaterialCardView>(R.id.cvProfileImage)
        val ivDeliveryProfileImage = view.findViewById<ImageView>(R.id.ivDeliveryProfileImage)
        val tvDeliveryName = view.findViewById<TextView>(R.id.tvDeliveryName)
        val tvVehicleInfo = view.findViewById<TextView>(R.id.tvVehicleInfo)
        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val bottomNavigationDelivery = view.findViewById<BottomNavigationView>(R.id.bottomNavigationDelivery)

        val userId = sessionManager.getUserId()
        var currentUser: User? = null

        fun loadData() {
            CoroutineScope(Dispatchers.IO).launch {
                val user = userRepository.getUserById(userId)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        currentUser = user
                        tvDeliveryName.text = user.name
                        tvVehicleInfo.text = user.millName.ifEmpty { "Registered Delivery Bike Partner" }
                        ImageLoader.loadImage(ivDeliveryProfileImage, user.profileImageUri, R.drawable.ic_delivery_bike)
                    }
                }
            }
        }

        cvProfileImage.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            ImagePickerHelper.showImagePickerDialog(this)
            onImageProcessedCallback = { savedPath ->
                CoroutineScope(Dispatchers.IO).launch {
                    val updated = user.copy(profileImageUri = savedPath)
                    userRepository.updateUser(updated)
                    withContext(Dispatchers.Main) { loadData() }
                }
            }
        }

        btnEditProfile.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            val etName = EditText(this).apply { setText(user.name); setPadding(32, 24, 32, 24) }
            val etPhone = EditText(this).apply { setText(user.phone); setPadding(32, 24, 32, 24) }
            val etVehicle = EditText(this).apply { setText(user.millName.ifEmpty { "Delivery Bike" }); setPadding(32, 24, 32, 24) }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 20, 40, 20)
                addView(etName)
                addView(etPhone)
                addView(etVehicle)
            }

            AlertDialog.Builder(this)
                .setTitle("Edit Partner Profile")
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val newName = etName.text.toString().trim()
                    val newPhone = etPhone.text.toString().trim()
                    val newVehicle = etVehicle.text.toString().trim()

                    CoroutineScope(Dispatchers.IO).launch {
                        val updated = user.copy(name = newName, phone = newPhone, millName = newVehicle)
                        userRepository.updateUser(updated)
                        sessionManager.saveUserSession(
                            userId = updated.id,
                            name = updated.name,
                            email = updated.email,
                            phone = updated.phone,
                            role = updated.role,
                            millId = 0L,
                            millName = newVehicle,
                            address = updated.address
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Partner profile updated!", Toast.LENGTH_SHORT).show()
                            loadData()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val btnDeveloperCredit = view.findViewById<MaterialButton>(R.id.btnDeveloperCredit)
        btnDeveloperCredit?.setOnClickListener {
            val logo = ImageView(this).apply {
                setImageResource(R.drawable.ic_flourmate_logo)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    gravity = android.view.Gravity.CENTER
                    bottomMargin = 24
                }
            }

            val tvTitle = TextView(this).apply {
                text = "Developer Credit"
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.primary_green))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 16)
            }

            val tvText = TextView(this).apply {
                text = "Developed by Meet Sanghani"
                textSize = 15f
                setTextColor(getColor(R.color.main_text))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 0, 16, 16)
            }

            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 36, 48, 24)
                addView(logo)
                addView(tvTitle)
                addView(tvText)
            }

            AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show()
        }

        btnLogout.setOnClickListener {
            sessionManager.logout()
            navigateTo(Screen.Login, clearStack = true)
        }

        setupDeliveryBottomNav(bottomNavigationDelivery, R.id.nav_delivery_profile)
        loadData()
    }

    // ==========================================
    // 26. DELIVERY EARNINGS SCREEN
    // ==========================================
    private fun renderDeliveryEarnings(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_delivery_earnings, mainContainer, false)
        mainContainer.addView(view)

        val bottomNavigationDelivery = view.findViewById<BottomNavigationView>(R.id.bottomNavigationDelivery)

        setupDeliveryBottomNav(bottomNavigationDelivery, R.id.nav_delivery_earnings)
    }

    // ==========================================
    // 27. SEARCH SCREEN
    // ==========================================
    private fun renderSearch(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_search, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val etSearchQuery = view.findViewById<EditText>(R.id.etSearchQuery)
        val recyclerSearchResults = view.findViewById<RecyclerView>(R.id.recyclerSearchResults)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        recyclerSearchResults.layoutManager = LinearLayoutManager(this)

        val productAdapter = ProductAdapter(emptyList(), onAddToCartClick = { product ->
            val userId = sessionManager.getUserId()
            CoroutineScope(Dispatchers.IO).launch {
                productRepository.addToCart(userId, product)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        recyclerSearchResults.adapter = productAdapter

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val q = s.toString().trim()
                if (q.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val results = productRepository.searchProducts(q)
                        withContext(Dispatchers.Main) { productAdapter.updateProducts(results) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ==========================================
    // 28. NOTIFICATIONS SCREEN
    // ==========================================
    private fun renderNotifications(inflater: LayoutInflater) {
        val view = inflater.inflate(R.layout.activity_notifications, mainContainer, false)
        mainContainer.addView(view)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    // ==========================================
    // BOTTOM NAVIGATION HELPERS
    // ==========================================
    private fun setupCustomerBottomNav(nav: BottomNavigationView?, selectedId: Int) {
        if (nav == null) return
        nav.setOnItemSelectedListener(null)
        nav.selectedItemId = selectedId
        nav.setOnItemSelectedListener { item ->
            if (selectedId != item.itemId) {
                when (item.itemId) {
                    R.id.nav_home -> navigateTo(Screen.CustomerHome, clearStack = true)
                    R.id.nav_mills -> navigateTo(Screen.Mills)
                    R.id.nav_orders -> navigateTo(Screen.Orders)
                    R.id.nav_cart -> navigateTo(Screen.Cart)
                    R.id.nav_profile -> navigateTo(Screen.CustomerProfile)
                }
            }
            true
        }
    }

    private fun setupMillOwnerBottomNav(nav: BottomNavigationView?, selectedId: Int) {
        if (nav == null) return
        nav.setOnItemSelectedListener(null)
        nav.selectedItemId = selectedId
        nav.setOnItemSelectedListener { item ->
            if (selectedId != item.itemId) {
                when (item.itemId) {
                    R.id.nav_dashboard -> navigateTo(Screen.MillOwnerDashboard, clearStack = true)
                    R.id.nav_manage_orders -> navigateTo(Screen.ManageOrders)
                    R.id.nav_manage_products -> navigateTo(Screen.ManageProducts)
                    R.id.nav_earnings -> navigateTo(Screen.Earnings)
                    R.id.nav_mill_profile -> navigateTo(Screen.MillOwnerProfile)
                }
            }
            true
        }
    }

    private fun setupDeliveryBottomNav(nav: BottomNavigationView?, selectedId: Int) {
        if (nav == null) return
        nav.setOnItemSelectedListener(null)
        nav.selectedItemId = selectedId
        nav.setOnItemSelectedListener { item ->
            if (selectedId != item.itemId) {
                when (item.itemId) {
                    R.id.nav_delivery_home -> navigateTo(Screen.DeliveryHome, clearStack = true)
                    R.id.nav_delivery_orders -> navigateTo(Screen.DeliveryOrders)
                    R.id.nav_delivery_earnings -> navigateTo(Screen.DeliveryEarnings)
                    R.id.nav_delivery_profile -> navigateTo(Screen.DeliveryProfile)
                }
            }
            true
        }
    }

    // Handle Image Picker Results
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerHelper.handleActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data,
            activity = this,
            prefix = "flourmate_${System.currentTimeMillis()}"
        ) { savedPath ->
            onImageProcessedCallback?.invoke(savedPath)
        }
    }
}