namespace rr {  

public class pQuickSprite
{

    public static void sort(vissprite_t[] c)
    {
        int i;
        int j;
        var left = 0;
        var right = c.Length - 1;
        var stack_pointer = -1;
        var stack = new int[128];
        vissprite_t<?> swap;
        vissprite_t<?> temp;
        while (true)
        {
            if (right - left <= 7)
            {
                for (j = left + 1; j <= right; j++)
                {
                    swap = c[j];
                    i = j - 1;
                    while (i >= left && c[i].scale > swap.scale)
                    {
                        c[i + 1] = c[i--];
                    }
                    c[i + 1] = swap;
                }
                if (stack_pointer == -1)
                    break;
                right = stack[stack_pointer--];
                left = stack[stack_pointer--];
            } else
            {
                var median = left + right >> 1;
                i = left + 1;
                j = right;
                swap = c[median];
                c[median] = c[i];
                c[i] = swap;
                /* make sure: c[left] <= c[left+1] <= c[right] */
                if (c[left].scale > c[right].scale)
                {
                    swap = c[left];
                    c[left] = c[right];
                    c[right] = swap;
                }
                if (c[i].scale > c[right].scale)
                {
                    swap = c[i];
                    c[i] = c[right];
                    c[right] = swap;
                }
                if (c[left].scale > c[i].scale)
                {
                    swap = c[left];
                    c[left] = c[i];
                    c[i] = swap;
                }
                temp = c[i];
                while (true)
                {
                    do
                    {
                        i++;
                    }
                    while (c[i].scale < temp.scale);
                    do
                    {
                        j--;
                    }
                    while (c[j].scale > temp.scale);
                    if (j < i)
                        break;
                    swap = c[i];
                    c[i] = c[j];
                    c[j] = swap;
                }
                c[left + 1] = c[j];
                c[j] = temp;
                if (right - i + 1 >= j - left)
                {
                    stack[++stack_pointer] = i;
                    stack[++stack_pointer] = right;
                    right = j - 1;
                } else
                {
                    stack[++stack_pointer] = left;
                    stack[++stack_pointer] = j - 1;
                    left = i;
                }
            }
        }
    }
}
    