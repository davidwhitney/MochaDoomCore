namespace utils {  

using java.io.PrintStream;
using java.io.PrintWriter;
using java.util.concurrent.Callable;
using java.util.function.*;

public enum Throwers
{
    ;

    @SafeVarargs
    public static <T> Callable<T>
    callable(ThrowingCallable<T> r, Class<? : Throwable>... cl)  
    {
        return () -> {
            try
            {
                return r.call();
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static Runnable
    runnable(ThrowingRunnable r, Class<? : Throwable>... cl)  
    {
        return () -> {
            try
            {
                r.run();
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Consumer<T>
    consumer(ThrowingConsumer<T> c, Class<? : Throwable>... cl)  
    {
        return t -> {
            try
            {
                c.accept(t);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2> BiConsumer<T1, T2>
    biConsumer(ThrowingBiConsumer<T1, T2> c, Class<? : Throwable>... cl)  
    {
        return (t1, t2) -> {
            try
            {
                c.accept(t1, t2);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Predicate<T>
    predicate(ThrowingPredicate<T> p, Class<? : Throwable>... cl)  
    {
        return t -> {
            try
            {
                return p.test(t);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2> BiPredicate<T1, T2>
    biPredicate(ThrowingBiPredicate<T1, T2> p, Class<? : Throwable>... cl)  
    {
        return (t1, t2) -> {
            try
            {
                return p.test(t1, t2);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T, R> Function<T, R>
    function(ThrowingFunction<T, R> f, Class<? : Throwable>... cl)  
    {
        return t -> {
            try
            {
                return f.apply(t);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T1, T2, R> BiFunction<T1, T2, R>
    biFunction(ThrowingBiFunction<T1, T2, R> f, Class<? : Throwable>... cl)  
    {
        return (t1, t2) -> {
            try
            {
                return f.apply(t1, t2);
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    @SafeVarargs
    public static <T> Supplier<T>
    supplier(ThrowingSupplier<T> s, Class<? : Throwable>... cl)  
    {
        return () -> {
            try
            {
                return s.get();
            }
            catch (Throwable e)
            {
                if (classifyMatching(e, cl))
                {
                    throw doThrow(e);
                } else
                {
                    throw doThrowE(e);
                }
            }
        };
    }

    /**
     * Throw checked exception as runtime exception preserving stack trace The class of exception will be changed so it
     * will only trigger catch statements for new type
     *
     * @param e exception to be thrown
     * @return impossible
     * @ 
     */
    public static RuntimeException doThrow(Throwable e)  
    {
        throw new Throwed(e);
    }

    /**
     * Throw checked exception as runtime exception preserving stack trace The class of exception will not be changed.
     * In example, an InterruptedException would then cause a Thread to be interrupted
     *
     * @param <E>
     * @param e   exception to be thrown
     * @return impossible
     * @  (in runtime)
     */
    @SuppressWarnings("unchecked")
    private static <E : Throwable> RuntimeException doThrowE(Throwable e)  
    {
        throw (E) e;
    }

    @SafeVarargs
    private static bool classifyMatching(Throwable ex, Class<? : Throwable>... options)
    {
        for (Class<? : Throwable> o : options)
        {
            if (o.isInstance(ex))
            {
                return true;
            }
        }

        return false;
    }

    public interface ThrowingCallable<T>
    {

        T call()  ;
    }

    public interface ThrowingRunnable
    {

        void run()  ;
    }

    public interface ThrowingConsumer<T>
    {

        void accept(T t)  ;
    }

    public interface ThrowingBiConsumer<T1, T2>
    {

        void accept(T1 t1, T2 t2)  ;
    }

    public interface ThrowingPredicate<T>
    {

        bool test(T t)  ;
    }

    public interface ThrowingBiPredicate<T1, T2>
    {

        bool test(T1 t1, T2 t2)  ;
    }

    public interface ThrowingFunction<T, R>
    {

        R apply(T t)  ;
    }

    public interface ThrowingBiFunction<T1, T2, R>
    {

        R apply(T1 t1, T2 t2)  ;
    }

    public interface ThrowingSupplier<T>
    {

        T get()  ;
    }

    public static class Throwed : RuntimeException
    {

        private static readonly long serialVersionUID = 5802686109960804684L;
        public  Throwable t;

        private Throwed(Throwable t)
        {
            super(null, null, true, false);
            this.t = t;
        }

        
        public synchronized Throwable fillInStackTrace()
        {
            return t.fillInStackTrace();
        }

        
        public synchronized Throwable getCause()
        {
            return t.getCause();
        }

        
        public String getLocalizedMessage()
        {
            return t.getLocalizedMessage();
        }

        
        public String getMessage()
        {
            return t.getMessage();
        }

        
        public StackTraceElement[] getStackTrace()
        {
            return t.getStackTrace();
        }

        
        public void setStackTrace(StackTraceElement[] stackTrace)
        {
            t.setStackTrace(stackTrace);
        }

        
        public synchronized Throwable initCause(Throwable cause)
        {
            return t.initCause(cause);
        }

        
        @SuppressWarnings("CallToPrintStackTrace")
        public void printStackTrace()
        {
            t.printStackTrace();
        }

        
        public void printStackTrace(PrintStream s)
        {
            t.printStackTrace(s);
        }

        
        public void printStackTrace(PrintWriter s)
        {
            t.printStackTrace(s);
        }

        
        public String toString()
        {
            return t.toString();
        }
    }
}
