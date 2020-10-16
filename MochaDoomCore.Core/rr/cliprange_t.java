namespace rr {  

public class cliprange_t
{

    public int first;
    public int last;

    public cliprange_t(int first, int last)
    {
        this.first = first;
        this.last = last;
    }
    public cliprange_t()
    {

    }

    public void copy(cliprange_t from)
    {
        first = from.first;
        last = from.last;
    }
}
