package rr.drawfuns;

/**
 * Either draws a column or a span
 *
 * @author velktron
 */

public interface ColumnFunction<T, V>
{
    void invoke();

    void invoke(ColVars<T, V> dcvars);

    /**
     * A set of flags that help identifying the type of function
     */
    int getFlags();
}