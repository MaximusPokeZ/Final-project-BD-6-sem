package org.example.impls;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class GeneretorMessagesJson {
    private static final String[] MODELS = {"Lastochka", "Ivolga", "Moskva"};
    private static final Random random = new Random();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        dateFormat.getCalendar().setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private static final DateTimeFormatter CH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

    public String getNewMessage() {
        String model = MODELS[random.nextInt(MODELS.length)];

        return String.format(
                "{ \"timestamp\": \"%s\", " +
                        "\"model\": \"%s\", " +
                        "\"coordX1\": %.2f, " +
                        "\"coordX2\": %.2f, " +
                        "\"state\": null }",//active non-active
                dateFormat.format(new Date()),
                model,
                random.nextDouble() * 100,
                random.nextDouble() * 100
        );
    }

    private String generateId(String model) {
        return model.hashCode() + "-" + random.nextInt(1000);
    }
}
