package org.application.common.constants;

public class EmailMessages {
    public static final String WELCOME_USER_SUBJECT = "Добро пожаловать в Bank Card Service!";

    public static final String UPDATE_USER_SUBJECT = "Обновление личных данных";

    public static final String DELETE_USER_SUBJECT = "Ждём Вас снова в Bank Card Service!";

    public static final String OPENED_CARD_SUBJECT = "Благодарим за открытие нового продукта!";

    public static final String CLOSED_CARD_SUBJECT = "Ваш продукт успешно закрыт!";

    public static final String CARD_EXPIRATION_SUBJECT = "Истекает срок действия Вашего продукта";

    public static final String CARD_REISSUE_SUBJECT = "Истёк срок действия продукта";

    public static String greetUser(String name, String patronymic) {
        return String.format("""
                Уважаемый(-ая) %s %s!

                Благодарим Вас за регистрацию в нашем банке.
                Желаем Вам успешного развития с продуктами нашего банка!""", name, patronymic);
    }

    public static String updateUser(String name, String patronymic) {
        return String.format("""
                Уважаемый(-ая) %s %s!

                Ваши данные были успешно обновлены.
                Если это были не Вы, срочно свяжитесь с нашим банком!""", name, patronymic);
    }

    public static String deleteUser(String name, String patronymic) {
        return String.format("""
                Уважаемый(-ая) %s %s!

                Благодарим Вас за доверие к нашему банку!
                Будем рады видеть Вас снова!""", name, patronymic);
    }

    public static String openCard(String name, String patronymic, String cardNumber) {
        StringBuilder emailCardNumber = new StringBuilder(cardNumber);
        emailCardNumber.replace(8, 16, "********");
        return String.format("""
                Уважаемый(-ая) %s %s!

                Ваша карта %s уже доступна для использования.
                Благодарим Вас за доверие к нашему банку!
                Желаем Вам много хороших покупок по новой карте!""", name, patronymic, emailCardNumber);
    }

    public static String closeCard(String name, String patronymic, String cardNumber) {
        StringBuilder emailCardNumber = new StringBuilder(cardNumber);
        emailCardNumber.replace(8, 16, "********");
        return String.format("""
                Уважаемый(-ая) %s %s!

                Ваша карта %s успешно закрыта.
                Благодарим Вас за использование карты!
                Если захотите открыть новую карту, мы всегда рядом!""", name, patronymic, emailCardNumber);
    }

    public static String notifyExpiration(String name, String patronymic, String cardNumber) {
        StringBuilder emailCardNumber = new StringBuilder(cardNumber);
        emailCardNumber.replace(8, 16, "********");
        return String.format("""
                        Уважаемый(-ая) %s %s!

                        Спешим сообщить, что срок действия Вашей карты %s истекает через 7 дней.
                        Вместо неё по истечении срока будет выпущена новая карта. Уведомление придёт к Вам на почту.""",
                name, patronymic, emailCardNumber);
    }

    public static String openNewProduct(String name, String patronymic, String oldCardNumber, String newCardNumber) {
        StringBuilder oldCardNumberSB = new StringBuilder(oldCardNumber);
        StringBuilder newCardNumberSB = new StringBuilder(newCardNumber);
        oldCardNumberSB.replace(8, 16, "********");
        newCardNumberSB.replace(8, 16, "********");
        return String.format("""
                        Уважаемый(-ая) %s %s!

                        Спешим сообщить, что у Вашей карты %s истёк срок действия. Более она недоступна.
                        Вместо неё автоматически выпущена новая карта %s. Ожидайте уведомление на почте.""",
                name, patronymic, oldCardNumberSB, newCardNumberSB);
    }
}
