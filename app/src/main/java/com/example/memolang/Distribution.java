package com.example.memolang;

import java.util.ArrayList;

public class Distribution
{
    private static class Suspect
    {
        int x, y;
        double s;

        public Suspect(int x, int y)
        {
            this.x = x;
            this.y = y;
            this.s = (double) x / y;
        }
    }

    public static int cardsH(int x, int y, int c)
    {
        ArrayList<Suspect> list = new ArrayList<Suspect>();
        for (int i = 1; i <= c; i++)
        {
            if (c % i == 0)
            {
                list.add(new Suspect(i, c / i));
            }
            else
            {
                list.add(new Suspect(i, c / i + 1));
            }
        }

        double sn = (double) x / y;
        int k = 0;
        double min = Math.abs(list.get(0).s - sn);

        for (int i = 1; i < list.size(); i++)
        {
            if (Math.abs(list.get(i).s - sn) < min)
            {
                min = Math.abs(list.get(i).s - sn);
                k = i;
            }
        }
        return list.get(k).y;
    }
}
