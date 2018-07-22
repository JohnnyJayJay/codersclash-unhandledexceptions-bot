package de.unhandledexceptions.codersclash.bot.entities;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 20.07.2018
 */

public class PieTile {

    private PieChart chart;
    private String key;
    private int count;

    public PieTile(String key, int count)
    {
        this.key = key;
        this.count = count;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public PieChart getChart()
    {
        return chart;
    }

    public void setChart(PieChart chart)
    {
        this.chart = chart;
    }
}
