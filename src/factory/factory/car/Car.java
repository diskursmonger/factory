package factory.factory.car;

import factory.factory.car.parts.Accessory;
import factory.factory.car.parts.Body;
import factory.factory.car.parts.Motor;
import factory.util.UniqueObject;

public class Car extends UniqueObject {
    private final Body body;
    private final Motor motor;
    private final Accessory accessory;

    public Car(
            Body body,
            Motor motor,
            Accessory accessory) {
        this.body = body;
        this.motor = motor;
        this.accessory = accessory;
    }

    @Override
    public String toString() {
        return String.format("Auto %d (%s, %s, %s)", this.getId(), this.body, this.motor, this.accessory);
    }
}
