public class BitBoard {
    private final int width, height;
    private final int chunkCount;
    private final int totalBits;
    private final long[] bits;

    public BitBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.totalBits = width * height;
        this.chunkCount = (totalBits + 63) / 64;
        this.bits = new long[chunkCount];
    }

    public BitBoard(BitBoard other) {
        this.width = other.width;
        this.height = other.height;
        this.totalBits = other.totalBits;
        this.chunkCount = other.chunkCount;
        this.bits = other.bits.clone();
    }

    public int getIndex(int row, int col) {
        return row * width + col;
    }

    public void setBit(int row, int col) {
        int idx = getIndex(row, col);
        bits[idx / 64] |= (1L << (idx % 64));
    }

    public boolean getBit(int row, int col) {
        int idx = getIndex(row, col);
        return (bits[idx / 64] & (1L << (idx % 64))) != 0;
    }

    public void clearBit(int row, int col) {
        int idx = getIndex(row, col);
        bits[idx / 64] &= ~(1L << (idx % 64));
    }

    public long[] getBits() {
        return bits;
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public BitBoard and(BitBoard other) {
        BitBoard result = new BitBoard(width, height);
        for (int i = 0; i < chunkCount; i++) {
            result.bits[i] = this.bits[i] & other.bits[i];
        }
        return result;
    }

    public boolean collides(BitBoard other) {
        for (int i = 0; i < chunkCount; i++) {
            if ((this.bits[i] & other.bits[i]) != 0) return true;
        }
        return false;
    }
}
