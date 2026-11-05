package com.lucasn.eldenatlas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AtlasModel(app: Application) : AndroidViewModel(app) {
 private val gson = Gson()
 private val dao = Room.databaseBuilder(app, AtlasDatabase::class.java, "atlas.db").build().favorites()
 private val api = Retrofit.Builder().baseUrl("https://eldenring.fanapis.com/api/").addConverterFactory(GsonConverterFactory.create()).build().create(EldenApi::class.java)
 private val auth = FirebaseApp.initializeApp(app)?.let { FirebaseAuth.getInstance() }
 val configured = auth != null
 var user by mutableStateOf(auth?.currentUser); private set
 var bosses by mutableStateOf<List<Boss>>(emptyList()); private set
 var favorites by mutableStateOf<List<Boss>>(emptyList()); private set
 var loading by mutableStateOf(false); private set
 var authenticating by mutableStateOf(false); private set
 var error by mutableStateOf<String?>(null); private set
 var authError by mutableStateOf<String?>(null); private set
 var page by mutableStateOf(0); private set
 var hasNext by mutableStateOf(false); private set
 var query by mutableStateOf(""); private set
 private var observation: Job? = null
 private var request: Job? = null
 init { observeFavorites() }
 private fun observeFavorites() {
  observation?.cancel(); favorites = emptyList()
  val uid = user?.uid ?: return
  observation = viewModelScope.launch { dao.observe(uid).collect { rows -> favorites = rows.map { gson.fromJson(it.json, Boss::class.java) } } }
 }
 fun login(email: String, password: String, register: Boolean) {
  val service = auth ?: return
  if (email.isBlank() || password.length < 6) { authError = "Informe seu e-mail e uma senha de pelo menos 6 caracteres."; return }
  if (authenticating) return
  viewModelScope.launch {
   authenticating = true; authError = null
   try {
    if (register) service.createUserWithEmailAndPassword(email.trim(), password).await() else service.signInWithEmailAndPassword(email.trim(), password).await()
    user = service.currentUser; observeFavorites()
   } catch (e: CancellationException) { throw e } catch (e: Exception) { authError = "Não foi possível entrar. Confira os dados, a conexão e se a conta já existe." }
   finally { authenticating = false }
  }
 }
 fun logout() { request?.cancel(); auth?.signOut(); user = null; observeFavorites(); bosses = emptyList(); error = null; authError = null; query = ""; page = 0 }
 fun load(search: String = query, targetPage: Int = 0) {
  request?.cancel()
  request = viewModelScope.launch {
   loading = true; error = null
   try {
    val result = api.bosses(targetPage, search.trim().ifEmpty { null })
    check(result.success)
    bosses = result.data.orEmpty(); page = targetPage; query = search; hasNext = bosses.size == 20
   } catch (e: CancellationException) { throw e } catch (e: Exception) { error = "Não foi possível carregar a API. Confira a internet e tente novamente." }
   finally { loading = false }
  }
 }
 var detail by mutableStateOf<Boss?>(null); private set
 fun loadDetail(id: String) {
  detail = null
  viewModelScope.launch {
   try { val response = api.boss(id); if (response.success) detail = response.data }
   catch (e: CancellationException) { throw e }
   catch (e: Exception) { error = "Não foi possível carregar os detalhes. Volte ao catálogo e tente novamente." }
  }
 }
 fun toggle(boss: Boss) {
  val uid = user?.uid ?: return
  viewModelScope.launch {
   try { if (favorites.any { it.id == boss.id }) dao.remove(uid, boss.id) else dao.save(Favorite(uid, boss.id, gson.toJson(boss))) }
   catch (e: Exception) { error = "Não foi possível salvar o favorito." }
  }
 }
}
