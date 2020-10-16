package w;

import java.io.DataOutputStream;
import java.io.IOException;

public interface IWritableDoomObject
{

    void write(DataOutputStream dos) throws IOException;
}
