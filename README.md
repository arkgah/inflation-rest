# inflation-rest
A spring boot REST web service for calculating CPI

## Spring boot REST веб-сервис для расчёта индекса инфляции (CPI)

Используется:
* Web
* Data JPA
* Validation
* PostgreSQL
* Security
* JWT
* i18n
* Test

Для работы с сервисом прилагаются коллекция запросов и переменные окружения для Postman

### Инициализация

1. Создать рабочую и тестовые DB
2. Указать настройки соединений с DB, соответственно, в application.properties и application-test.properties
3. Перед первым запуском раскомментировать указанные в application.properties строки. Создастся демонстрационная схема с
   данными.
4. Перед последующими запусками закомментировать указанные в application.properties строки

### Работа с сервисом

1. Импортировать в Postman прилагаемую коллекцию запросов и переменные окружения.
2. Указать свои настройки (адрес, порт) в переменной окружения
3. Для доступа к конечным точкам (endpoints), необходимо авторизоваться через Postman-запрос Auth/Login.  
   Для демонстрационных целей, после инициализационного запуска, создаются 2 пользователя с login: user и admin.
   Пароль у них 12345678.
   Для новых пользователей, создаваемых через /Auth/New user, производится контроль качества пароля.
4. После успешной авторизации на сервисе, в Postman автоматически задаётся JWT и можно выполнять запросы.
5. Часть endpoints, как в демонстрационных, так и практических целях, имеет доступ только для пользователей с ролью
   ROLE_ADMIN

### Тестирование

1. Для интеграционного тестирования используется тестовая база PostgreSQL. Можно было бы использовать H2 DB (в качестве
   упражнения, или иных целей).
2. На данный момент создан пример модульного тестирования одного из сервисов. Остальные имплементировать по аналогии.
3. Также имеется интеграционное тестирование одного из контроллеров.