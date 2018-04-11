package lib;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class BitIteratorAndBuilderTest {

    @Test
    public void create_and_print_nominal() throws IOException {
        String expected = "\u0082";
        String message = "\u0082" + BitIterator.END_DELIMITER;

        BitIterator bitMessage;
        try {
            bitMessage = new BitIterator(message);
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("Could not encode message: " + e.getMessage());
        }

        BitBuilder bitOutputBuilder = new BitBuilder();

        int count = 0;
        ArrayList<Byte> bitList = new ArrayList<>();
        while (bitMessage.hasNext()) {
            Byte currentByte = bitMessage.next();
            bitList.add(currentByte);
            count++;
            bitOutputBuilder.append(currentByte);

        }

        String outputMes = bitOutputBuilder.toString();
        assertTrue(expected.equals(outputMes));
    }
}
