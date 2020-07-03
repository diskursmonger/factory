package factory.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SalesFormatter extends Formatter {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public String format(LogRecord record) {
        return String.format("[%s] %s\n", dateFormat.format(new Date(record.getMillis())), this.formatMessage(record));
    }
}
