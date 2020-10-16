package rr.drawfuns;

/**
 * Either draws a column or a span
 *
 * @author velktron
 */

public interface SpanFunction<T, V>
{
    void invoke();

    void invoke(SpanVars<T, V> dsvars);

}