namespace m {  

using java.io.IOException;

public interface ISyncLogger
{

    void debugStart()  ;

    void debugEnd();

    void sync(String format, Object... args);
}

