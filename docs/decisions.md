# Decisões técnicas

## Tempo controlável

Clock é injetado. Os testes avançam o tempo sem sleep, verificando que uma tarefa só volta a ser adquirida ao atingir seu prazo. Este é um núcleo de fila consultado por polling, não um scheduler que executa tarefas sozinho.

## Estados e concorrência

READY vira RUNNING durante claim sob lock. A operação devolve snapshot imutável; o consumidor executa o trabalho fora do monitor. Sucesso leva a SUCCEEDED. Falha volta a READY com atraso fixo ou chega a DEAD ao consumir o limite. A ordenação usa prazo e id para desempate determinístico.

## Conclusões atrasadas

O número da tentativa é um token exigido em succeed/fail. Uma resposta da tentativa anterior não pode concluir a tentativa atual. Não há promessa de entrega exatamente uma vez: consumidores devem tornar efeitos externos idempotentes.

## Limitações

Sem persistência, recuperação de processos ou timeout de RUNNING. Se um consumidor morrer, a tarefa fica RUNNING indefinidamente nesta versão. As próximas extensões seriam lease com expiração, armazenamento transacional e métricas, ainda não implementadas.
