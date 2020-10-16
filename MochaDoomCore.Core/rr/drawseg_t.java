package rr;

//
// ?
//

public class drawseg_t
{

    /**
     * MAES: was pointer. Not array?
     */
    public seg_t curline;
    public int x1;
    public int x2;
    /**
     * fixed_t
     */
    public int scale1;
    public int scale2;
    public int scalestep;
    /**
     * 0=none, 1=bottom, 2=top, 3=both
     */
    public int silhouette;
    /**
     * do not clip sprites above this (fixed_t)
     */
    public int bsilheight;
    /**
     * do not clip sprites below this (fixed_t)
     */
    public int tsilheight;
    /**
     * Indexes to lists for sprite clipping,
     * all three adjusted so [x1] is first value.
     */
    private int psprtopclip;
    private int psprbottomclip;
    private int pmaskedtexturecol;
    /**
     * Pointers to the actual lists
     */

    private short[] sprtopclip;
    private short[] sprbottomclip;
    private short[] maskedtexturecol;

    public drawseg_t()
    {

    }

    ///////////////// Accessor methods to simulate mid-array pointers ///////////

    public void setSprTopClip(short[] array, int index)
    {
        sprtopclip = array;
        psprtopclip = index;
    }

    public void setSprBottomClip(short[] array, int index)
    {
        sprbottomclip = array;
        psprbottomclip = index;
    }

    public void setMaskedTextureCol(short[] array, int index)
    {
        maskedtexturecol = array;
        pmaskedtexturecol = index;
    }

    public short getSprTopClip(int index)
    {
        return sprtopclip[psprtopclip + index];
    }

    public short getSprBottomClip(int index)
    {
        return sprbottomclip[psprbottomclip + index];
    }

    public short getMaskedTextureCol(int index)
    {
        return maskedtexturecol[pmaskedtexturecol + index];
    }

    public short[] getSprTopClipList()
    {
        return sprtopclip;
    }

    public short[] getSprBottomClipList()
    {
        return sprbottomclip;
    }

    public short[] getMaskedTextureColList()
    {
        return maskedtexturecol;
    }

    public int getSprTopClipPointer()
    {
        return psprtopclip;
    }

    public void setSprTopClipPointer(int index)
    {
        psprtopclip = index;
    }

    public int getSprBottomClipPointer()
    {
        return psprbottomclip;
    }

    public void setSprBottomClipPointer(int index)
    {
        psprbottomclip = index;
    }

    public int getMaskedTextureColPointer()
    {
        return pmaskedtexturecol;
    }

    public void setMaskedTextureColPointer(int index)
    {
        pmaskedtexturecol = index;
    }

    public boolean nullSprTopClip()
    {
        return sprtopclip == null;
    }

    public boolean nullSprBottomClip()
    {
        return sprbottomclip == null;
    }

    public boolean nullMaskedTextureCol()
    {
        return maskedtexturecol == null;
    }

}
