## 📌 Общие правила работы с ветками

### 🔒 Защита ветки `main`
- ❌ Нельзя удалять ветку `main`
- ❌ Нельзя выполнять `force push` в `main`
- ✅ Все изменения в `main` возможны только через Pull Request
- ❌ Прямой push в `main` запрещён
- ✅ Каждый PR должен пройти код-ревью

## 🛠️ Гайд: создание, работа и мёрдж ветки

### 1. Клонирование проекта
```bash
git clone git@github.com:MaximusPokeZ/Final-project-BD-6-sem.git
cd Final-project-BD-6-sem
```

###  Обновить локальную копию main перед созданием новой ветки
```bash
git checkout main
git pull origin main
```

### Создание новой ветки
```bash
git checkout -b feature/имя-задачи
```

### Коммит и пуш
```bash
git add .
git commit -m "sometext"
git push origin "твоя-ветка"
```

### Создание PR

base: main
compare: твоя ветка

### Слияние ветки с main

### Через GitHub
Открыть Pull Request --> Нажать Merge pull request --> Подтвердить слияние --> Удалить ветку после слияния


### После слияния
Удалить локальную ветку, если она больше не нужна:
```bash
git branch -d feature/название-задачи
```

### Если ветка уже удалена на GitHub, можно очистить локальные ссылки:
```bash
git fetch -p
```
