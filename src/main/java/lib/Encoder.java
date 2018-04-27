package lib;

import java.io.IOException;

public interface Encoder {
    void Encode(String message) throws IOException;
    String Decode() throws IOException;
    String GetName();
    void SetImage(String path);
    double GetCapacityFactor();
}
