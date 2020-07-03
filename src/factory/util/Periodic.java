package factory.util;

public interface Periodic extends Runnable {

    int getDelayMillis();

    void setDelayMillis(int newDelayMillis);
}
