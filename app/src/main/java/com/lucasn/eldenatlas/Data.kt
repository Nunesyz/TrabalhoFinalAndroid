package com.lucasn.eldenatlas

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query as ApiQuery

data class Boss(
 val id: String,
 val name: String?,
 val image: String?,
 val description: String?,
 val location: String?,
 val healthPoints: String?,
 val drops: List<String>?
)

data class BossResponse(
 val success: Boolean,
 val data: List<Boss>?
)

data class BossDetailResponse(
 val success: Boolean,
 val data: Boss?
)

interface EldenApi {
 @GET("bosses/{id}")
 suspend fun boss(
  @Path("id") id: String
 ): BossDetailResponse

 @GET("bosses")
 suspend fun bosses(
  @ApiQuery("page") page: Int,
  @ApiQuery("name") name: String?,
  @ApiQuery("limit") limit: Int = 20
 ): BossResponse
}

@Entity(primaryKeys = ["owner", "id"])
data class Favorite(
 val owner: String,
 val id: String,
 val json: String
)

@Dao
interface FavoriteDao {
 @Query("SELECT * FROM Favorite WHERE owner = :owner")
 fun observe(owner: String): Flow<List<Favorite>>

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun save(favorite: Favorite)

 @Query("DELETE FROM Favorite WHERE owner = :owner AND id = :id")
 suspend fun remove(owner: String, id: String)
}

@Database(
 entities = [Favorite::class],
 version = 1,
 exportSchema = false
)
abstract class AtlasDatabase : RoomDatabase() {
 abstract fun favorites(): FavoriteDao
}