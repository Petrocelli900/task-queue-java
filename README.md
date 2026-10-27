# task-queue-java

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Verification](https://github.com/Petrocelli900/task-queue-java/actions/workflows/verify.yml/badge.svg)

Fila de tarefas com aquisição atômica, retentativa com atraso fixo, limite de tentativas e rejeição de conclusões atrasadas.

Projeto de portfólio focado em regras de negócio e testes reproduzíveis. Não depende de frameworks ou serviços externos.

## Executar

Pré-requisito: JDK 17 ou superior. No Linux/macOS ou Git Bash:

```bash
git clone https://github.com/Petrocelli900/task-queue-java.git
cd task-queue-java
sh verify.sh
```

No Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac --release 17 -Xlint:all -Werror -d out src/main/java/dev/petrocelli/*.java src/test/java/dev/petrocelli/*.java
java -ea -cp out dev.petrocelli.TaskQueueTest
```

O programa de teste também serve como exemplo executável e termina com `TaskQueue: OK` se todos os cenários passarem. Falhas lançam exceção e retornam status diferente de zero.

## Exemplo de uso

```java
var queue = new TaskQueue(Clock.systemUTC(), 3, Duration.ofSeconds(10));
queue.submit("email-42", "Enviar confirmação fictícia");
var task = queue.claim().orElseThrow();
queue.succeed(task.id(), task.attempts());
```

## Organização

- `src/main/java/dev/petrocelli/TaskQueue.java`: modelo e operações de domínio.
- `src/test/java/dev/petrocelli/TaskQueueTest.java`: cenários executáveis sem bibliotecas externas.
- `docs/decisions.md`: decisões, compromissos e possibilidades de evolução.
- `.github/workflows/verify.yml`: compilação rigorosa e execução dos testes em cada push e PR.

## Escopo

Implementação em memória para estudo. Reiniciar o processo perde os dados. Não inclui API HTTP, autenticação, banco de dados ou garantias distribuídas. Não representa um serviço pronto para produção.

## Desenvolvimento

Implementação inicial criada com assistência de IA. Os testes documentam o comportamento esperado; a evolução deve preservar os invariantes descritos nas decisões técnicas.

[Perfil do autor](https://github.com/Petrocelli900)
