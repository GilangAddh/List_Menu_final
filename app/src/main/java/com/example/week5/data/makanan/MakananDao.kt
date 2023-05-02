package com.example.week5.data.makanan

import androidx.room.*

@Dao
interface MakananDao {
    @Query("SELECT * FROM makanan WHERE nama_makanan LIKE :namaMakanan")
    suspend fun searchMakanan(namaMakanan: String) : List<Makanan>

    @Insert
    suspend fun addMakanan(makanan: Makanan)

    @Update(entity = Makanan::class)
    suspend fun updateMakanan(makanan: Makanan)

    @Delete
    suspend fun deleteMakanan(makanan: Makanan)

    @Query("SELECT * FROM makanan ORDER BY id DESC")
    suspend fun getAllMakanan(): List<Makanan>
}