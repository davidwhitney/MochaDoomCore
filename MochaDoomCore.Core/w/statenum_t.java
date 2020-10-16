namespace w {  

public enum statenum_t
{

    NoState(-1),
    StatCount(0),
    ShowNextLoc(1);

    private int value;

    statenum_t(int val)
    {
        value = val;
    }

    public int getValue()
    {
        return value;
    }


}
