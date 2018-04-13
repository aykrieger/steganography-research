package lib;

/**
 * BitBuilder constructs a message from bits
 * Since java does not allow bits, bytes are substituted
 */
public class BitBuilder {
    private StringBuilder builder;
    private Byte currentByte;
    private int bitsUsedInCurrentByte;
    private final int BITS_IN_A_BYTE = 8;

    public BitBuilder() {
        builder = new StringBuilder();
        currentByte = 0x00;
        bitsUsedInCurrentByte = 0;
    }

    /**
     *@return True if the end delimiter of the message is reached and false otherwise
     */
    public boolean append(byte b) {
        int tempByte = currentByte << 1;

        //add only the last bit from b to the current byte
        tempByte = tempByte | (b & 0x01);
        currentByte = (byte) tempByte;

        bitsUsedInCurrentByte ++;

        // if the byte has been fully formed
        if (bitsUsedInCurrentByte >= BITS_IN_A_BYTE) {

            int unsignedByteValue = currentByte & 0xFF;
            builder.append((char) unsignedByteValue);
            bitsUsedInCurrentByte = 0;

            // check if the end has been reached
            if (currentByte == BitIterator.END_DELIMITER) {
                return true;
            }
            currentByte = 0x00;
        }

        return false;
    }

    public String toString() {
        return builder.toString();
    }
}
