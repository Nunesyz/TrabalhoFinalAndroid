# Elden Atlas — base do trabalho

Projeto Android Kotlin com Compose. Escopo reduzido aos requisitos informados na conversa, sem reproduzir todas as telas do Figma. A rubrica original do professor não foi disponibilizada nesta etapa; esta entrega não garante nota.

## Abrir e configurar

1. Extraia este ZIP e abra a pasta EldenAtlas no Android Studio.
2. Use JDK 17 e instale o Android SDK 35 pelo SDK Manager. Aguarde o Gradle Sync (precisa de internet).
3. Em https://console.firebase.google.com crie um projeto e registre um aplicativo Android com pacote `com.lucasn.eldenatlas`.
4. Baixe `google-services.json` e coloque em `EldenAtlas/app/google-services.json`.
5. No Firebase, abra Authentication → Sign-in method e habilite E-mail/senha.
6. Sincronize novamente o Gradle e execute em um emulador ou celular Android 7 ou superior.
7. No app, toque em Criar uma conta. Use e-mail válido e senha com no mínimo 6 caracteres.

Não há conta pronta nem senha embutida. Sem a configuração do Firebase, o app mostra uma orientação e não libera acesso. Não precisa de Firestore, servidor próprio ou chave para a FanAPI. As dependências estão fixadas; não é necessário migrar para versões mais recentes para abrir o projeto.

## O que está implementado

| Requisito informado | Implementação |
| --- | --- |
| API pública | Retrofit consulta bosses reais na FanAPI; pesquisa e paginação |
| Login e sessão | Cadastro/login Firebase, restauração da sessão pelo SDK e logout |
| Persistência local | Room guarda detalhes completos dos favoritos por UID |
| Navegação | Navigation Compose: catálogo, favoritos e detalhes; acesso antes do login |

Tema escuro com dourado, estados de carregamento/erro/vazio e substituição textual para imagem ausente. Dados da API permanecem em inglês. Favoritos podem ser consultados offline depois de salvos. Não há sincronização entre aparelhos. Sair da conta preserva os favoritos locais, mas outra conta não os visualiza.

Fora desta base: armas, builds, jornada, derrotas/tentativas, perfil e telas adicionais. São expansões, não necessárias para demonstrar os quatro requisitos conhecidos.

## Roteiro de verificação e apresentação

- Cadastrar uma conta, sair e entrar de novo.
- Tentar senha incorreta: deve aparecer erro, sem abrir catálogo.
- Buscar Margit e abrir detalhes; limpar a busca para retornar ao catálogo.
- Avançar e voltar páginas.
- Salvar um favorito e abrir Meus favoritos.
- Fechar completamente e reabrir o app: sessão e favorito devem continuar.
- Desligar internet: catálogo mostra erro, favorito salvo ainda abre seus textos (imagem pode depender de cache).
- Remover favorito e reabrir: ele deve continuar removido.
- Sair, cadastrar outra conta e confirmar que ela não vê favoritos da primeira.
- Voltar à primeira conta e conferir seus dados.
- Depois de sair, usar Voltar não deve reabrir uma tela autenticada.

## Arquivos

- Data.kt: modelos da API, interface Retrofit, entidade/DAO/banco Room.
- AtlasModel.kt: autenticação, consulta, estado de tela e favoritos.
- MainActivity.kt: tema e telas Compose.

## Validação e limites

O endpoint público respondeu HTTP 200 durante a preparação em 04/11/2026. Estrutura do ZIP e manifest foram inspecionados. Este ambiente não possui Android SDK nem emulador: o projeto NÃO foi compilado nem executado aqui. Faça o roteiro acima no Android Studio antes da entrega. Sem google-services.json e Authentication habilitado o login não funciona. O serviço externo pode ficar indisponível.

Documentação da API: https://docs.eldenring.fanapis.com/docs/bosses


Migrado para o projeto criado no colégio: pacote, catálogo de versões, Gradle Wrapper, ícones e recursos originais preservados. Não inclui local.properties ou caches do computador.
