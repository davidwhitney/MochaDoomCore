package p;

public interface ISightChecker
{

    void setZStartTopBOttom(int zstart, int top, int bottom);

    void setSTrace(mobj_t t1, mobj_t t2);

    boolean CrossBSPNode(int bspnum);

}
