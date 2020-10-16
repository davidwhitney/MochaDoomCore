namespace rr {  

public interface IDetailAware
{

    int HIGH_DETAIL = 0;
    int LOW_DETAIL = 1;

    void setDetail(int detailshift);
}
