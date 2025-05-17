package util;

public class Car {
    public char id;
    public boolean isHorizontal;
    public int length;

    // Posisi mobil sebagai bitmask
    public long[] bitmask;

    public Car(char id, boolean isHorizontal, int length, long[] bitmask) {
        this.id = id;
        this.isHorizontal = isHorizontal;
        this.length = length;
        this.bitmask = bitmask;
    }

    // Buat copy mobil (untuk generate state baru)
    public Car copy() {
        return new Car(id, isHorizontal, length, bitmask.clone());
    }
}
