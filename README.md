# Elden Atlas

Aplicativo Android sobre os bosses de Elden Ring, desenvolvido como trabalho escolar em Kotlin.

## Funcionalidades

- Cadastro e login com e-mail e senha
- Busca de bosses pelo nome
- Detalhes com imagem, descrição, localização, vida e recompensas
- Favoritos salvos no aparelho e separados por usuário
- Sessão mantida ao fechar e abrir o aplicativo

## Tecnologias

Kotlin, Jetpack Compose, Firebase Authentication, Retrofit, Room, Navigation Compose e Coil.

Os dados dos bosses são consultados na Elden Ring FanAPI:
https://docs.eldenring.fanapis.com/docs/bosses

## Como executar

1. Abra o projeto no Android Studio.
2. Configure um aplicativo Android no Firebase com o pacote `com.lucasn.eldenatlas`.
3. Coloque o arquivo `google-services.json` na pasta `app`.
4. Ative o login por e-mail e senha no Firebase Authentication.
5. Sincronize o Gradle e execute no emulador ou celular.

## Organização

- `MainActivity.kt`: telas e navegação.
- `AtlasModel.kt`: controle dos dados, autenticação e favoritos.
- `Data.kt`: modelos, chamadas da API e banco de dados local.