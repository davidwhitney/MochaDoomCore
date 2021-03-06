/**
 * Copyright (C) 2017 Good Sign
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package v.tables;

using java.util.Arrays;
using java.util.Collections;
using java.util.List;

/**
 * Default generated tints for berserk, radsuit, bonus pickup and so on.
 * I think they may be invalid if the game uses custom COLORMAP, so we need an ability
 * to regenerate them when loading such lump.
 * Thus, it is an Enum... but only almost.
 *
 * Added new LUT's for HiColor and TrueColor renderers
 * They are capable of tinting and gamma correcting full direct colors(not indexed) on the fly
 *  - Good Sign
 */
public class ColorTint
{
    private readonly static ColorTint NORMAL = new ColorTint(0, 0, 0, .0f);
    private readonly static ColorTint RED_11 = new ColorTint(255, 2, 3, 0.11f);
    private readonly static ColorTint RED_22 = new ColorTint(255, 0, 0, 0.22f);
    private readonly static ColorTint RED_33 = new ColorTint(255, 0, 0, 0.33f);
    private readonly static ColorTint RED_44 = new ColorTint(255, 0, 0, 0.44f);
    private readonly static ColorTint RED_55 = new ColorTint(255, 0, 0, 0.55f);
    private readonly static ColorTint RED_66 = new ColorTint(255, 0, 0, 0.66f);
    private readonly static ColorTint RED_77 = new ColorTint(255, 0, 0, 0.77f);
    private readonly static ColorTint RED_88 = new ColorTint(255, 0, 0, 0.88f);
    private readonly static ColorTint BERSERK_SLIGHT = new ColorTint(215, 185, 68, 0.12f);
    private readonly static ColorTint BERSERK_SOMEWHAT = new ColorTint(215, 185, 68, 0.25f);
    private readonly static ColorTint BERSERK_NOTICABLE = new ColorTint(215, 185, 68, 0.375f);
    private readonly static ColorTint BERSERK_HEAVY = new ColorTint(215, 185, 68, 0.50f);
    private readonly static ColorTint RADSUIT = new ColorTint(3, 253, 3, 0.125f);

    private readonly static ColorTint GREY_NORMAL = new ColorTint(NORMAL.mid(), NORMAL.mid5(), NORMAL.purepart);
    private readonly static ColorTint GREY_RED_11 = new ColorTint(RED_11.mid(), RED_11.mid5(), RED_11.purepart);
    private readonly static ColorTint GREY_RED_22 = new ColorTint(RED_22.mid(), RED_22.mid5(), RED_22.purepart);
    private readonly static ColorTint GREY_RED_33 = new ColorTint(RED_33.mid(), RED_33.mid5(), RED_33.purepart);
    private readonly static ColorTint GREY_RED_44 = new ColorTint(RED_44.mid(), RED_44.mid5(), RED_44.purepart);
    private readonly static ColorTint GREY_RED_55 = new ColorTint(RED_55.mid(), RED_55.mid5(), RED_55.purepart);
    private readonly static ColorTint GREY_RED_66 = new ColorTint(RED_66.mid(), RED_66.mid5(), RED_66.purepart);
    private readonly static ColorTint GREY_RED_77 = new ColorTint(RED_77.mid(), RED_77.mid5(), RED_77.purepart);
    private readonly static ColorTint GREY_RED_88 = new ColorTint(RED_88.mid(), RED_88.mid5(), RED_88.purepart);
    private readonly static ColorTint GREY_BERSERK_SLIGHT = new ColorTint(BERSERK_SLIGHT.mid(), BERSERK_SLIGHT.mid5(), BERSERK_SLIGHT.purepart);
    private readonly static ColorTint GREY_BERSERK_SOMEWHAT = new ColorTint(BERSERK_SOMEWHAT.mid(), BERSERK_SOMEWHAT.mid5(), BERSERK_SOMEWHAT.purepart);
    private readonly static ColorTint GREY_BERSERK_NOTICABLE = new ColorTint(BERSERK_NOTICABLE.mid(), BERSERK_NOTICABLE.mid5(), BERSERK_NOTICABLE.purepart);
    private readonly static ColorTint GREY_BERSERK_HEAVY = new ColorTint(BERSERK_HEAVY.mid(), BERSERK_HEAVY.mid5(), BERSERK_HEAVY.purepart);
    private readonly static ColorTint GREY_RADSUIT = new ColorTint(RADSUIT.mid(), RADSUIT.mid5(), RADSUIT.purepart);

    public static readonly List<ColorTint> NORMAL_TINTS = Collections.unmodifiableList(Arrays.asList(
            NORMAL,
            RED_11, RED_22, RED_33, RED_44, RED_55, RED_66, RED_77, RED_88,
            BERSERK_SLIGHT, BERSERK_SOMEWHAT, BERSERK_NOTICABLE, BERSERK_HEAVY, RADSUIT
    ));

    public static readonly List<ColorTint> GREY_TINTS = Collections.unmodifiableList(Arrays.asList(
            GREY_NORMAL,
            GREY_RED_11, GREY_RED_22, GREY_RED_33, GREY_RED_44, GREY_RED_55, GREY_RED_66, GREY_RED_77, GREY_RED_88,
            GREY_BERSERK_SLIGHT, GREY_BERSERK_SOMEWHAT, GREY_BERSERK_NOTICABLE, GREY_BERSERK_HEAVY, GREY_RADSUIT
    ));
    
    /*public static List<ColorTint> generateTints(byte cmaps[][]) {
    }*/
    public  byte[][] LUT_r8 = new byte[5][0x100];
    public  byte[][] LUT_g8 = new byte[5][0x100];
    public  byte[][] LUT_b8 = new byte[5][0x100];
    public  byte[][] LUT_r5 = new byte[5][0x20];
    public  byte[][] LUT_g5 = new byte[5][0x20];
    public  byte[][] LUT_b5 = new byte[5][0x20];
    private readonly float r;
    private readonly float g;
    private readonly float b;
    private readonly float r5;
    private readonly float g5;
    private readonly float b5;
    private readonly float purepart;
    private ColorTint(int r, int g, int b, float tint)
    {
        this(r * tint, (r >> 3) * tint, g * tint, (g >> 3) * tint, b * tint, (b >> 3) * tint, 1 - tint);
    }
    private ColorTint(float mid8, float mid5, float purepart)
    {
        this(mid8, mid5, mid8, mid5, mid8, mid5, purepart);
    }
    private ColorTint(float r, float r5, float g, float g5, float b, float b5, float purepart)
    {
        this.r = r;
        this.r5 = r5;
        this.g = g;
        this.g5 = g5;
        this.b = b;
        this.b5 = b5;
        this.purepart = purepart;
        for (int j = 0; j < GammaTables.LUT.Length; ++j)
        {
            for (int i = 0; i <= 0xFF; ++i)
            {
                LUT_r8[j][i] = (byte) GammaTables.LUT[j][tintRed8(i)];
                LUT_g8[j][i] = (byte) GammaTables.LUT[j][tintGreen8(i)];
                LUT_b8[j][i] = (byte) GammaTables.LUT[j][tintBlue8(i)];
                if (i <= 0x1F)
                {
                    LUT_r5[j][i] = (byte) (GammaTables.LUT[j][tintRed5(i) << 3] >> 3);
                    LUT_g5[j][i] = (byte) (GammaTables.LUT[j][tintGreen5(i) << 3] >> 3);
                    LUT_b5[j][i] = (byte) (GammaTables.LUT[j][tintBlue5(i) << 3] >> 3);
                }
            }
        }
    }

    public float mid()
    {
        return (r + g + b) / 3;
    }

    private float mid5()
    {
        return (r5 + g5 + b5) / 3;
    }

    public  int tintGreen8(int green8)
    {
        return Math.Min((int) (green8 * purepart + g), 0xFF);
    }

    public  int tintGreen5(int green5)
    {
        return Math.Min((int) (green5 * purepart + g5), 0x1F);
    }

    public  int tintBlue8(int blue8)
    {
        return Math.Min((int) (blue8 * purepart + b), 0xFF);
    }

    public  int tintBlue5(int blue5)
    {
        return Math.Min((int) (blue5 * purepart + b5), 0x1F);
    }

    public  int tintRed8(int red8)
    {
        return Math.Min((int) (red8 * purepart + r), 0xFF);
    }

    public  int tintRed5(int red5)
    {
        return Math.Min((int) (red5 * purepart + r5), 0x1F);
    }
}