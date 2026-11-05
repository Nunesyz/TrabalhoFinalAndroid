package com.lucasn.eldenatlas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFC6AB68), background = Color(0xFF141412), surface = Color(0xFF22221E))) {
    Surface(Modifier.fillMaxSize()) { Atlas() }
   }
  }
 }
}

@Composable
fun Atlas(model: AtlasModel = viewModel()) {
 if (!model.configured) {
  Column(Modifier.safeDrawingPadding().padding(24.dp)) {
   Text("Elden Atlas", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge)
   Text("Falta conectar o Firebase. Adicione google-services.json à pasta app e habilite E-mail/senha no Firebase Authentication. Veja o README do projeto.")
  }
  return
 }
 if (model.user == null) { Login(model); return }
 key(model.user?.uid) {
  val nav = rememberNavController()
  LaunchedEffect(Unit) { model.load() }
  NavHost(nav, startDestination = "catalog", modifier = Modifier.safeDrawingPadding()) {
   composable("catalog") {
    Catalog(model, false, { nav.navigate("favorites") }, { nav.navigate("detail/${it.id}") })
   }
   composable("favorites") {
    Catalog(model, true, { nav.popBackStack() }, { nav.navigate("detail/${it.id}") })
   }
   composable("detail/{id}") { entry ->
    val id = entry.arguments?.getString("id")
    val boss = (model.bosses + model.favorites).firstOrNull { it.id == id } ?: model.detail?.takeIf { it.id == id }
    LaunchedEffect(id) { if (boss == null && id != null) model.loadDetail(id) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
     item { TextButton(onClick = { nav.popBackStack() }) { Text("Voltar") } }
     if (boss == null) {
      item { Text("Reabra este boss pelo catálogo ou pelos favoritos.") }
     } else {
      item { Text(boss.name ?: "Boss", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge) }
      item { BossImage(boss) }
      item { Text(boss.description ?: "Descrição não informada.") }
      item { Text("Local: ${boss.location ?: "Não informado"}") }
      item { Text("Vida: ${boss.healthPoints ?: "Não informada"}") }
      item { Text("Recompensas", style = MaterialTheme.typography.titleLarge) }
      item { Text(boss.drops?.joinToString("\n")?.ifBlank { "Não informadas" } ?: "Não informadas") }
      item { Button(onClick = { model.toggle(boss) }) { Text(if (model.favorites.any { it.id == boss.id }) "Remover dos favoritos" else "Salvar favorito") } }
      item { model.error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
     }
    }
   }
  }
 }
}

@Composable
private fun Login(model: AtlasModel) {
 var email by rememberSaveable { mutableStateOf("") }
 var password by remember { mutableStateOf("") }
 var register by rememberSaveable { mutableStateOf(false) }
 LazyColumn(Modifier.fillMaxSize().imePadding().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
  item { Spacer(Modifier.height(32.dp)); Text("ELDEN ATLAS", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary) }
  item { Text("Conheça seus adversários. Guarde sua próxima jornada.") }
  item { Text(if (register) "Criar conta" else "Entrar", style = MaterialTheme.typography.titleLarge) }
  item { OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
  item { OutlinedTextField(password, { password = it }, label = { Text("Senha") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) }
  item { model.authError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
  item { Button(onClick = { model.login(email, password, register) }, enabled = !model.authenticating, modifier = Modifier.fillMaxWidth()) { Text(if (model.authenticating) "Aguarde..." else if (register) "Cadastrar" else "Entrar") } }
  item { TextButton(onClick = { register = !register }, enabled = !model.authenticating) { Text(if (register) "Já tenho conta" else "Criar uma conta") } }
 }
}

@Composable
private fun Catalog(model: AtlasModel, saved: Boolean, switch: () -> Unit, open: (Boss) -> Unit) {
 var search by rememberSaveable { mutableStateOf(model.query) }
 val list = if (saved) model.favorites else model.bosses
 LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
  item { Text(if (saved) "Guarde o essencial." else "Diante da névoa.", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary) }
  item { Row { TextButton(onClick = switch) { Text(if (saved) "Voltar ao catálogo" else "Favoritos (${model.favorites.size})") }; TextButton(onClick = model::logout) { Text("Sair") } } }
  if (!saved) {
   item { OutlinedTextField(search, { search = it }, label = { Text("Nome do boss (em inglês)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
   item { Button(onClick = { model.load(search) }, enabled = !model.loading) { Text("Buscar") } }
   item {
    if (model.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    model.error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(onClick = { model.load(search) }) { Text("Tentar novamente") } }
   }
  }
  if (list.isEmpty() && (saved || !model.loading)) item { Text(if (saved) "Nenhum favorito ainda. Abra um boss e toque em Salvar favorito." else "Nenhum boss encontrado.") }
  items(list, key = { it.id }) { boss ->
   Card(onClick = { open(boss) }, modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
     AsyncImage(model = boss.image, contentDescription = boss.name, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
     Text(boss.name ?: "Boss", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.titleLarge)
     Text(boss.location ?: "Local não informado")
     Text("Ver detalhes →", color = MaterialTheme.colorScheme.primary)
    }
   }
  }
  if (!saved) item {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    TextButton(onClick = { model.load(targetPage = model.page - 1) }, enabled = !model.loading && model.page > 0) { Text("Anterior") }
    Text("Página ${model.page + 1}", modifier = Modifier.padding(top = 12.dp))
    TextButton(onClick = { model.load(targetPage = model.page + 1) }, enabled = !model.loading && model.hasNext) { Text("Próxima") }
   }
  }
 }
}

@Composable
private fun BossImage(boss: Boss) {
 var failed by remember(boss.id) { mutableStateOf(false) }
 if (boss.image.isNullOrBlank() || failed) Text("Imagem indisponível")
 else AsyncImage(model = boss.image, contentDescription = boss.name, modifier = Modifier.fillMaxWidth().height(220.dp), onError = { failed = true })
}
