
<p align="center">

  <img width="128" height="128" src="https://github.com/MironovNikita/card-service/blob/main/res/logo.png">

</p>

# 💳 Card Service
Данное приложение представляет собой сервис по работе с банковскими картами. Пользователи имеют возможность зарегистрироваться в приложении, обновлять свои данные, открывать и закрывать банковские карты, просматривать свои открытые и закрытые банковские продукты. Также приложение вовремя оповестит о скором окончании срока действия карты, а в случае если срок действия карты уже окончен, карта будет перевыпущена с новым номером и CVV в автоматическом режиме с обязательным оповещением пользователя.

## 📝 Описание
Приложение разделено на три модуля:
- основной сервис [**bank-service**](https://github.com/MironovNikita/card-service/tree/main/bank-service) (непосредственно работает с базой данных);
- сервис уведомлений [**notification-service**](https://github.com/MironovNikita/card-service/tree/main/notification-service) (отвечает за рассылку уведомлений пользователям);
- база данных приложения (**PostgreSQL 15**).

### 💱 Bank-service 🏧
Данный модуль работает с базой данных и отвечает за создание пользователей, банковских карт и дальнейшую работу с ними.

В программе **пользователь** представлен сущностью [**User**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/User.java). Она содержит следующие данные пользователя:
- **`id`** - уникальный идентификатор пользователя;
- **`surname`** - фамилия;
- **`name`** - имя;
- **`patronymic`** - отчество;
- **`email`** - email-адрес;
- **`birthday`** - дата рождения;
- **`phone`** - номер телефона;
- **`password`** - пароль.

При этом уникальность пользователя определяется не только на основании его идентификатора. Номер телефона и email-адрес также должны быть индивидуальны.

Но работа с сущностью [**User**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/User.java) скрыта от пользователя и осуществляется внутри приложения. Из внешнего мира приложение принимает данные в виде [**UserDto**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/UserDto.java). Её принципиальное отличие заключается в том, что поля Dto-объекта подвергаются _валидации_ - осуществляется проверка корректности поступивших данных на уровне [**UserController**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserController.java).

Валидация разделена на два уровня:
- при создании пользователя ([**Create.class**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/validation/Create.java));
- при обновлении пользователя ([**Update.class**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/validation/Update.java)).

В случае создания ***все*** поля являются ***обязательными*** для заполнения. При этом на поля также наложены следующие ограничения:
- **`surname`** - не должна превышать 70 символов;
- **`name`** - не должно превышать 70 символов;
- **`patronymic`** - не должно превышать 70 символов;
- **`email`** - должен быть минимум 5, максимум - 50 символов и соответствовать email-формату (`test@mail.com`);
- **`birthday`** - должен быть "из прошлого", т.е. как минимум не превышать текущую дату;
- **`phone`** - должен быть российского формата (содержать в себе 11 цифр и начинаться с 7 или 8 - аннотация [**`@Phone`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/validation/Phone.java));
- **`password`** - должен содержать от 10 до 20 символов.

Приложение позволяет осуществить следующие [**операции**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserController.java) с входящими данными:
- создать пользователя (**`create`**);
- обновить пользователя (**`update`**);
- получить пользователя по ID (**`get`**);
- удалить пользователя по ID (**`delete`**);

Пользовательское API выглядит следующим образом:

![userAPI](https://github.com/MironovNikita/card-service/blob/main/res/bank/userAPI.png)

Подробнее с API можно ознакомиться в [**документации**](https://github.com/MironovNikita/card-service/blob/main/documentation/swagger/bank-service-spec.json) Swagger.

В случае успешной валидации данных они передаются в работу [**UserService**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/service/UserServiceImpl.java). Он также проверяет данные на корректность -  определяет возможность работы базы данных с прибывшими данными, обрабатывает их и делегирует их обработку далее в [**репозиторий**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserRepository.java).

Важно отметить, что пользовательские пароли хранятся в базе данных в зашифрованном виде. За это отвечает класс [**`PasswordHandler`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/security/PasswordHandler.java).

[**Репозиторий**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserRepository.java) имплементирует **`JpaRepository`**, что упрощает работу с базой данных. Помимо стандартных методов, предоставляемых с помощью данной имплементации также были добавлены следующие методы:
- **`boolean existsByEmail(String email)`**;

- **`boolean existsByPhone(String phone)`**.

Они необходимы для успешного поддержания бизнес-логики по уникальности номеров телефонов и email-адресов пользователей.

Так как приложение не работает с сущностью [**User**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/User.java) напрямую, то возвращаемые данные [**сервисом**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/service/UserServiceImpl.java) и [**контроллером**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserController.java) определяются в [**UserSafeDto**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/UserSafeDto.java). Подобный формат данных назван безопасным, потому что он не передаёт пароль пользователя даже в зашифрованном виде. В остальном он ничем не отличается от основной сущности. Для осуществления маппинга применяется [**UserMapper**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/UserMapper.java) (зависимость _mapstruct_).

В целом схема работы с данными пользователя выглядит следующим образом:

<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/bank/userData.png">

</p>

В конечном итоге записи пользователей в базе данных выглядят следующим образом:

![userDB](https://github.com/MironovNikita/card-service/blob/main/res/bank/userDB.png)

Пример корректного *JSON-формата* для создания пользователя выглядит следующий образом:
```java
{
  "surname": "Иванов",
  "name": "Иван",
  "patronymic": "Иванович",
  "email": "ivanov1@yandex.com",
  "birthday": "1990-08-10",
  "phone": "79523639558",
  "password": "password123"
}
```
В случае успешного запроса в теле ответа будут следующие данные:
```java
{
    "id": 1,
    "surname": "Иванов",
    "name": "Иван",
    "patronymic": "Иванович",
    "email": "ivanov1@yandex.com",
    "birthday": "1990-08-10",
    "phone": "79523639558"
}
```
###
**Банковские карты** представлены сущностью [**Card**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/entity/Card.java). Она содержит следующие данные:
- **`id`** - уникальный идентификатор карты;
- **`owner`** - собственник карты;
- **`number`** - уникальный номер карты;
- **`issueDate`** - дата выпуска карты;
- **`expirationDate`** - дата окончания срока действия карты;
- **`cvv`** - CVV-код безопасности, который **никому** нельзя говорить 😉;
- **`opened`** - статус карты (открыта или закрыта).

При этом уникальность карты определяется её номером. Карт с одинаковым номером даже для одного и того же пользователя не предусмотрено (генерируется автоматически).

Любые данные о сущности [**Card**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/entity/Card.java) не поступают извне. Все данные либо считываются из базы данных (имя и фамилия владельца), либо генерируются автоматически. Работа с банковскими картами осуществляется через запросы, обрабатываемые с помощью [**CardController**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/CardController.java).

Так как все данные генерируются в самой программе, то валидация данных в данном случае не нужна. Приложение позволяет осуществлять следующие операции по банковским картам:
- открыть карту (**`open`**);
- получить список всех карт пользователя (**`getAll`**);
- получить список всех открытых карт пользователя (**`getOpened`**);
- получить список всех закрытых карт пользователя (**`getClosed`**);
- закрыть карту (**`close`**).

Пользовательское API для работы с картами выглядит следующим образом:

![cardAPI](https://github.com/MironovNikita/card-service/blob/main/res/bank/cardAPI.png)

Подробнее с API можно ознакомиться в [**документации**](https://github.com/MironovNikita/card-service/blob/main/documentation/swagger/bank-service-spec.json) Swagger.

#### ⚠️ Важно ⚠️
Удаление банковских карт пользователями ***не предусмотрено*** даже в случае закрытия карты. Они удаляются автоматически только в том случае, если _пользователь удаляется_ из системы окончательно.

За генерацию данных банковских карт отвечают следующие классы:
- [**`CardDataGenerator`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/security/CardDataGenerator.java) - генерирует номер карты и CVV (который мы всё ещё **никому** не говорим 😉);
- [**`CardDataEncryptor`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/security/CardDataEncryptor.java) - шифрует номер карты и CVV для хранения в базе данных.

### [**`CardDataGenerator`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/security/CardDataGenerator.java)
Как упоминалось выше, данный класс отвечает за генерацию номера карты и CVV кода. Сам класс имеет две константы - **`BIN`** и **`NUM_LENGTH`**. Первая отвечает за банковский индивидуальный номер, вторая - за количество цифр в номере карты. Как правило, **`BIN`** - это первые 4 цифры в номере любой карты.

Сам номер карты генерируется на основании **алгоритма Луна**. Он используется для проверки корректности номеров банковских карт. Назван в честь **Hans Peter Luhn**, который описал его в патенте в 1954 году.

Основные шаги алгоритма:
1. **_Удвоение чётных цифр_**. Начиная с последней цифры номера карты и двигаясь к первой, удваиваются все цифры на чётных позициях (считая с конца).

2. **_Суммирование цифр_**. Если результат удвоения больше 9, вычитаем из него 9. Все числа, полученные в результате удвоения и преобразования, а также неудваиваемые числа, складываются.

3. ***Проверка кратности 10***. Полученная сумма должна быть кратна 10 для того, чтобы номер карты считался корректным.

```java
Пример:
Пусть у нас есть номер карты 4000 1234 5678 9010.

Удвоение чётных цифр: 0, 0, 0, 0, 2, 2, 6, 4, 5, 6, 14, 0, 18, 8, 2, 0.

Суммирование цифр: 0 + 0 + 0 + 0 + 2 + 2 + 6 + 4 + 5 + 6 + 1 + 4 + 0 + 8 + 8 + 2 + 0 = 58.

58 не кратно 10, следовательно, номер карты некорректен.
```
Алгоритм Луна позволяет быстро и легко проверить правильность номера карты до передачи его для обработки банковской системой. Это помогает обнаружить опечатки или ошибки при вводе номера карты пользователем.

### [**`CardDataEncryptor`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/common/security/CardDataEncryptor.java)
Как указано выше, данный класс отвечает за шифрование номера карты и CVV. Он имеет две константы: **`AES`** и **`KEY_FILE_PATH`**, а также содержит класс **`SecretKeySpec`**.

**`AES`** - симметричный алгоритм шифрования, является одним из самых распространенных и безопасных алгоритмов шифрования, используемых в настоящее время.

**`KEY_FILE_PATH`** - путь к файлу, содержащему ключ шифрования, с помощью которого оно осуществляется. В случае отсутствия файла по указанному пути, он будет сформирован случайным образом.

**`SecretKeySpec`** - это класс, который представляет собой ключ для использования в криптографических операциях, таких как шифрование и расшифрование данных. Он используется вместе с различными криптографическими алгоритмами для создания ключей. Он принимает массив байтов в качестве ключа и строку, которая представляет алгоритм шифрования, который этот ключ будет использовать.

За работу вышеуказанных классов отвечает [**CardService**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/service/CardServiceImpl.java). Он содержит в себе логику создания карт, а также указывает, в каких случаях нужно произвести шифрование и дешифрование данных банковских карт. За работу с базой данных в вопросе банковских карт отвечает [**CardRepository**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/CardRepository.java). Он имплементирует **`JpaRepository`**, что упрощает работу с базой данных. Помимо стандартных методов, предоставляемых с помощью данной имплементации также были добавлены следующие методы:
- **`findAllByOwnerId(Long ownerId)`** - позволяет найти все карты пользователя по его идентификатору;
- **`Card findCardByNumber(String number)`** - позволяет найти определённую карту по её номеру;
- **`List<Card> findAllByExpirationDate(LocalDate expirationDate)`** - позволяет найти список карт по дате истечения срока действия.

При этом в случае каких-либо запросов от пользователя, касающихся его банковских карт, он получает ответ, в теле которого содержатся одна или несколько банковских карт в _JSON-формате_. Однако, пользователю не возвращается сущность [**Card**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/entity/Card.java). Вместо этого в программе предусмотрен [**CardSafeDto**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/entity/CardSafeDto.java). Также вместо полных данных пользователя в данной сущности содержится [**UserCardDto**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/user/entity/UserCardDto.java), включающий в свой состав только имя и фамилию собственника карты.

Подобный формат данных назван безопасным, так как он содержит все поля, что и **Card**, за исключением CVV-кода и даты выпуска карты, а также скрывает лишние данные о пользователе. Стоит также отметить, что данные направляются пользователям в расшифрованном виде. Для осуществления маппинга применяется [**CardMapper**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/CardMapper.java) (зависимость _mapstruct)_.

В целом схема работы пользователя с банковскими картами выглядит следующим образом:

<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/bank/cardData.png">

</p>

В конечном итоге записи банковских карт в базе данных выглядят следующим образом:

![cardDB](https://github.com/MironovNikita/card-service/blob/main/res/bank/cardDB.png)

Пример корректного _JSON-формата_ для банковской карты выглядит следующий образом:
```java
{
    "id": 4,
    "owner": {
        "name": "Ксения",
        "surname": "Романова"
    },
    "number": "4395143354727403",
    "expirationDate": "2027-03-13",
    "opened": true
}
```

###
Помимо вышеуказанных возможностей приложение также позволяет уведомлять пользователей по электронной почте об окончании срока действия карты и о её перевыпуске. За данный функционал отвечает класс [**`CardExpirationHandler`**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/card/management/CardExpirationHandler.java).
Он содержит два метода:
- **`checkCardExpiration()`** - проверяет все карты в базе данных на окончание срока действия (за 1 неделю от текущей даты);
- **`reissueCard()`** - проверяет все карты в базе данных и в случае, если карта истекает сегодня, закрывает существующую карту и генерирует новую для этого же пользователя.

Сами задачи, которые выполняются в данных методах, выполняются по расписанию и осуществляются **в 3 часа ночи** - в тот момент, когда на приложение и на базу данных будет наименьшая нагрузка. Данный функционал реализован с помощью аннотации **`@Scheduled(cron = "0 0 3 * * *")`**. Она используется для создания запланированных задач, которые выполняются периодически или по расписанию. Параметр `cron` позволяет задавать расписание выполнения задачи.

Описание параметров аннотации:
1. **Секунды (0)**. Задаёт конкретную секунду, в которую будет запускаться задача. 
2. **Минуты (0)**. Задаёт конкретную минуту, в которую будет запускаться задача.
3. **Часы (3)**. Задаёт конкретный час, в который будет запускаться задача.
4. **Дни месяца (звёздочка)**. Задаёт день месяца, в который будет запускаться задача.
5. **Месяцы (звёздочка)**. Задаёт месяц, в который будет запускаться задача.
6. **Дни недели (звёздочка)**. Задаёт день недели, в который будет запускаться задача.

####

Для того, чтобы в приложении были доступны операции по расписанию, необходимо над классом, загружающим **`SpringBootApplication`** указать аннотацию **`@EnableScheduling`**. Она используется для включения поддержки выполнения запланированных задач в приложении. При использовании этой аннотации Spring будет сканировать компоненты приложения для обнаружения методов, помеченных аннотацией **`@Scheduled`**, и автоматически создавать задания на их выполнение в соответствии с расписанием.

Так как классы, отвечающие за отправку email-уведомлений находятся в другом модуле, взаимодействие между модулями осуществляется посредством [**WebClientService**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/java/org/application/client/WebClientService.java). Данный класс представляет собой клиент, с помощью которого направляются запросы по url-адресу модуля уведомлений.

Пример уведомления создания пользователя:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/createUserEmail.png">

</p>

Пример уведомления обновления данных пользователя:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/updateUserEmail.png">

</p>

Пример открытия карты пользователя:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/openCardEmail.png">

</p>

Пример закрытия карты пользователя:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/closeCardEmail.png">

</p>

Пример уведомления о скором окончании срока действия карты:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/soonCardEmail.png">

</p>

Пример уведомления о перевыпуске карты:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/notification/reissueCardEmail1.png">

</p>

### 📬 Notification-service 📩
Данный модуль работает с отправкой email-уведомлений.

Отправка email осуществляется с помощью [**EmailController**](https://github.com/MironovNikita/card-service/blob/main/notification-service/src/main/java/org/application/email/EmailController.java). Данный класс содержит в себе всего 1 метод - **`sendMail`**. Он принимает на вход email-адрес, кому необходимо отправить письмо, а также структуру email-сообщения - [**EmailStructure**](https://github.com/MironovNikita/card-service/blob/main/notification-service/src/main/java/org/application/email/entity/EmailStructure.java).

Непосредственно за отправку email-уведомлений отвечает [**EmailService**](https://github.com/MironovNikita/card-service/blob/main/notification-service/src/main/java/org/application/email/service/EmailServiceImpl.java). Он имеет 2 финальных поля: **`JavaMailSender mailSender`** и **`String fromMail`**. Первый параметр - это интерфейс в Spring Framework, предназначенный для отправки электронной почты из приложений. Он предоставляет удобный способ для программного доступа к SMTP-серверу для отправки электронных сообщений. Второй параметр - email-адрес, с которого будут отправляться email-сообщения. Он указан в [**application.properties**](https://github.com/MironovNikita/card-service/blob/main/notification-service/src/main/resources/application.properties).

API для работы с уведомлениями по email выглядят следующим образом:

![notificationAPI](https://github.com/MironovNikita/card-service/blob/main/res/notification/notificationAPI.png)

Подробнее с API можно ознакомиться в [**документации**](https://github.com/MironovNikita/card-service/blob/main/documentation/swagger/notification-service-spec.json) Swagger.

## 🧐 Доверяй, но проверяй ✅

### JUnit & Mockito
Для проверки работоспособности программы были написаны тесты, находящиеся:
- для **`bank-service`** [**здесь**](https://github.com/MironovNikita/card-service/tree/main/bank-service/src/test/java/org/application);
- для **`notification-service`** [**здесь**](https://github.com/MironovNikita/card-service/tree/main/notification-service/src/test/java/org/application/email).

Результаты выполнения тестов для **`bank-service`**:

![bankTest](https://github.com/MironovNikita/card-service/blob/main/res/bank/bankTest.png)

Результаты выполнения тестов для **`notification-service`**:

![notificationTest](https://github.com/MironovNikita/card-service/blob/main/res/notification/notificationTest.png)

### 🚀 Postman-тесты 📊
Дополнительно для тестирования приложения были созданы Postman-тесты. Они предназначены для автоматизации проверки веб-сервисов и API. Они используются для уверенности в том, что веб-сервисы возвращают ожидаемые результаты и работают правильно в различных сценариях. 

Тесты пользовательского функционала выглядят следующим образом:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/userPostman.png">

</p>

Тесты функционала банковских карт выглядят следующим образом:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/cardPostman.png">

</p>

Тесты функционала отправки email-уведомлений выглядят следующим образом:
<p align="center">

  <img src="https://github.com/MironovNikita/card-service/blob/main/res/emailPostman.png">

</p>

Пример тестов для запроса по созданию пользователя:
```java
pm.test("Код ответа должен быть 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Есть ответ при создании пользователя", function () {
    pm.response.to.be.withBody;
    pm.response.to.be.json;
});

pm.test("Проверка поля 'id' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData.id, '"id" должен быть 1').to.eql(1);
});

pm.test("Проверка поля 'surname' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('surname');
    pm.expect(jsonData.surname, '"surname" должна быть "Иванов"').to.eql('Иванов');
});

pm.test("Проверка поля 'name' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData.name, '"name" должно быть "Иван"').to.eql('Иван');
});

pm.test("Проверка поля 'patronymic' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('patronymic');
    pm.expect(jsonData.patronymic, '"patronymic" должно быть "Иванович"').to.eql('Иванович');
});

pm.test("Проверка поля 'email' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('email');
    pm.expect(jsonData.email, '"email" должно быть "ivanov1@yandex.com"').to.eql('ivanov1@yandex.com');
});

pm.test("Проверка поля 'birthday' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('birthday');
    pm.expect(jsonData.birthday, '"birthday" должен быть "1990-08-10"').to.eql('1990-08-10');
});

pm.test("Проверка поля 'phone' у пользователя", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('phone');
    pm.expect(jsonData.phone, '"phone" должен быть "79523639558"').to.eql('79523639558');
});
```

Пример тестов для запроса по открытию банковской карты:
```java
pm.test("Код ответа должен быть 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Есть ответ при создании банковской карты", function () {
    pm.response.to.be.withBody;
    pm.response.to.be.json;
});

pm.test("Проверка поля 'id' у банковской карты", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData.id, '"id" должен быть 1').to.eql(1);
});

pm.test("Проверка полей 'owner' у банковской карты", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('owner');
    pm.expect(jsonData.owner.name, '"name" должно быть "Сергей"').to.eql("Сергей");
    pm.expect(jsonData.owner.surname, '"surname" должно быть "Семенчук"').to.eql("Семенчук");
});

pm.test("Проверка поля 'number' у банковской карты", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('number');
    pm.expect(jsonData.number.length, "'number' должен содержать ровно 16 символов").to.eql(16);
    pm.expect(jsonData.number.startsWith("4395"), "'number' должен начинаться с цифр '4395'").to.be.true;
});

pm.test("Проверка наличия поля 'expirationDate' у банковской карты", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('expirationDate');
});

pm.test("Проверка поля 'opened' у банковской карты", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('opened');
    pm.expect(jsonData.opened, '"opened" должен быть true').to.eql(true);
});
```

Подробнее с коллекцией тестов Postman можно ознакомиться [**здесь**](https://github.com/MironovNikita/card-service/blob/main/documentation/postman/Bank%20Card%20API.postman_collection.json).

### 🐳 Docker-контейнеры 🐋
**Docker** - это платформа для разработки, доставки и запуска приложений в контейнерах. Контейнеры представляют изолированные среды, включающие в себя все необходимые зависимости, библиотеки и файлы приложения, что обеспечивает их легкость и консистентность при развертывании на различных средах.

Запуск приложения настроен с помощью контейнеров через **Docker**. Модули **``**, **``**, **``** запускаются в отдельных Docker-контейнерах каждый. Для успешного запуска приложения требуется сделать сборку проекта через команду Maven `mvn clean install`, а затем зайти через консоль в папку проекта и ввести команду `docker compose up`.

Запущенные контейнеры:

![containers](https://github.com/MironovNikita/card-service/blob/main/res/docker/containers.png)

Созданные образы:

![images](https://github.com/MironovNikita/card-service/blob/main/res/docker/images.png)

Результаты выполнения Postman-тестов:

![containersTests](https://github.com/MironovNikita/card-service/blob/main/res/docker/containersTests.png)

Уведомления на почту в результате тестирования контейнеров:

![containersEmails1](https://github.com/MironovNikita/card-service/blob/main/res/docker/containersEmails1.png)

![containersEmails2](https://github.com/MironovNikita/card-service/blob/main/res/docker/containersEmails2.png)

#### ⚠️ Важно ⚠️
Для запуска приложения локально через IDEA необходимо раскомментировать следующие строки в [**application.properties**](https://github.com/MironovNikita/card-service/blob/main/bank-service/src/main/resources/application.properties):
- webclient.baseurl=http://localhost:8081 -> комментируем webclient.baseurl=http://notification-service:8081;
- spring.datasource.url=jdbc:postgresql://localhost:5432/bankDB -> комментируем spring.datasource.url=${SPRING_DATASOURCE_URL}.

Первые строки отвечают за корректный запуск на локальной машине через IDEA, вторые - через Docker. В первом случае - это URL-адрес webclient, во втором случае - адрес доступа к базе данных.

Подробнее о настройках Docker можно ознакомиться [**здесь**](https://github.com/MironovNikita/card-service/blob/main/docker-compose.yml).

## 📋 pom.xml и его зависимости ⚙️
Все зависимости указаны в главном [**pom.xml**](https://github.com/MironovNikita/card-service/blob/main/pom.xml).

Список основных зависимостей:
- **spring-boot-starter-data-jpa** - зависимость для работы с JPA (Java Persistence API) в приложении Spring Boot;
- **spring-boot-starter-test** - зависимость для модульного тестирования в Spring Boot приложении;
- **spring-boot-starter-web** - зависимость для создания веб-приложений с использованием Spring Boot;
- **spring-boot-starter-mail** - зависимость для отправки почтовых сообщений в Spring Boot приложении;
- **spring-boot-starter-webflux** - зависимость для создания реактивных веб-приложений с использованием Spring Boot;
- **spring-security-crypto** - зависимость для работы с криптографическими операциями в Spring Security. Предоставляет инструменты для шифрования, дешифрования и хеширования данных, а также для работы с ключами шифрования;
- **org.postgresql:postgresql** - зависимость для работы с PostgreSQL базой данных;
- **com.h2database:h2** - встроенная база данных H2 для разработки и тестирования приложений;
- **org.projectlombok:lombok** - библиотека Lombok для упрощения разработки приложений. Позволяет автоматически создавать геттеры, сеттеры, конструкторы и другие методы через аннотации;
- **org.hibernate.validator:hibernate-validator** - библиотека для валидации данных в приложениях с использованием аннотаций;
- **com.fasterxml.jackson.core:jackson-databind** - библиотека для преобразования объектов в JSON и обратно;
- **org.mapstruct:mapstruct** - библиотека для удобного маппинга объектов в приложениях;
- **com.squareup.okhttp3:mockwebserver** - библиотека для тестирования HTTP клиентов в приложениях;
- **jakarta.el:jakarta.el-api** - зависимость, позволяющая встраивать выражения в текст веб-страницы. EL используется, например, в технологиях JavaServer Pages (JSP) и JavaServer Faces (JSF) для упрощения доступа к данным и выполнения операций на стороне сервера;
- **org.springdoc:springdoc-openapi-starter-webmvc-ui** - зависимость для генерации документации OpenAPI (ранее Swagger) для Spring приложений.

Результаты сборки проекта:
```java
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for CardService 0.0.1-SNAPSHOT:
[INFO] 
[INFO] CardService ........................................ SUCCESS [  1.078 s]
[INFO] bank-service ....................................... SUCCESS [ 16.582 s]
[INFO] notification-service ............................... SUCCESS [  4.902 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  22.824 s
[INFO] Finished at: 2024-03-14T00:44:37+03:00
[INFO] ------------------------------------------------------------------------

```

