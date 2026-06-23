package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val pharmacyId: Int,
    val pharmacyName: String,
    val availability: Boolean,
    val lastUpdated: Long,
    val isPrescriptionRequired: Boolean,
    val description: String = ""
)

@Entity(tableName = "pharmacies")
data class Pharmacy(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String,
    val rating: Float,
    val isVerified: Boolean,
    val contactNo: String
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val customerPhone: String,
    val customerName: String,
    val deliveryAddress: String,
    val medicineId: Int,
    val medicineName: String,
    val quantity: Int,
    val totalPrice: Double,
    val pharmacyName: String,
    val status: String, // "Pending", "Accepted", "Out for Delivery", "Delivered"
    val orderTime: Long,
    val deliveryFee: Double,
    val isEmergency: Boolean,
    val paymentMethod: String, // "Cash on Delivery" or "Digital Payment"
    val prescriptionPhotoPath: String? = null // if custom prescription uploaded
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineName: String,
    val time: String, // e.g. "08:00 AM"
    val frequency: String, // "Daily", "Weekly"
    val personName: String, // Family account member
    val activeString: Boolean = true
)

@Entity(tableName = "price_logs")
data class PriceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val medicineName: String,
    val pharmacyName: String,
    val oldPrice: Double,
    val newPrice: Double,
    val updatedAt: Long
)

@Dao
interface MediKartDao {
    // Medicines
    @Query("SELECT * FROM medicines")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE availability = 1")
    fun getAvailableMedicines(): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    // Pharmacies
    @Query("SELECT * FROM pharmacies")
    fun getAllPharmacies(): Flow<List<Pharmacy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacy(pharmacy: Pharmacy)

    @Update
    suspend fun updatePharmacy(pharmacy: Pharmacy)

    // Orders
    @Query("SELECT * FROM orders ORDER BY orderTime DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY orderTime DESC")
    fun getOrdersByCustomer(phone: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    // Reminders
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    // Price Logs
    @Query("SELECT * FROM price_logs ORDER BY updatedAt DESC")
    fun getAllPriceLogs(): Flow<List<PriceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceLog(log: PriceLog)
}

@Database(entities = [Medicine::class, Pharmacy::class, Order::class, Reminder::class, PriceLog::class], version = 1, exportSchema = false)
abstract class MediKartDatabase : RoomDatabase() {
    abstract fun dao(): MediKartDao

    companion object {
        @Volatile
        private var INSTANCE: MediKartDatabase? = null

        fun getDatabase(context: Context): MediKartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediKartDatabase::class.java,
                    "medikart_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class MediKartRepository(private val dao: MediKartDao) {
    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()
    val availableMedicines: Flow<List<Medicine>> = dao.getAvailableMedicines()
    val allPharmacies: Flow<List<Pharmacy>> = dao.getAllPharmacies()
    val allOrders: Flow<List<Order>> = dao.getAllOrders()
    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    val allPriceLogs: Flow<List<PriceLog>> = dao.getAllPriceLogs()

    fun getOrdersForCustomer(phone: String): Flow<List<Order>> = dao.getOrdersByCustomer(phone)

    suspend fun insertMedicine(medicine: Medicine) = dao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: Medicine) = dao.updateMedicine(medicine)
    suspend fun insertPharmacy(pharmacy: Pharmacy) = dao.insertPharmacy(pharmacy)
    suspend fun updatePharmacy(pharmacy: Pharmacy) = dao.updatePharmacy(pharmacy)
    suspend fun insertOrder(order: Order): Long = dao.insertOrder(order)
    suspend fun updateOrder(order: Order) = dao.updateOrder(order)
    suspend fun insertReminder(reminder: Reminder) = dao.insertReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = dao.deleteReminder(reminder)
    suspend fun insertPriceLog(log: PriceLog) = dao.insertPriceLog(log)
}
