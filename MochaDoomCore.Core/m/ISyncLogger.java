package m;

import java.io.IOException;

public interface ISyncLogger
{

    void debugStart() throws IOException;

    void debugEnd();

    void sync(String format, Object... args);
}

