package de.unhandledexceptions.codersclash.bot.entities;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.util.Set;

/**
 * @author oskar
 * github.com/oskardevkappa/
 * <p>
 * 20.07.2018
 */

public class PieChart {

    private Set<PieTile> tiles;
    private String title;
    private DefaultPieDataset dataset;
    private JFreeChart chart;

    public PieChart(String title)
    {
        this.title = title;
        this.tiles = new ListOrderedSet<>();
    }

    public void create()
    {

        this.dataset = createDataset();
        this.chart = createChart();
    }

    private DefaultPieDataset createDataset()
    {

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (PieTile tile: tiles)
        {
            dataset.setValue(tile.getKey(), tile.getCount());
        }
        return dataset;
    }

    private JFreeChart createChart()
    {
        return ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                true);
    }

    public void setTiles(Set<PieTile> tiles)
    {
        this.tiles = tiles;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public DefaultPieDataset getDataset()
    {
        return dataset;
    }

    public JFreeChart getChart()
    {
        return chart;
    }

    public Set<PieTile> getTiles()
    {
        return tiles;
    }

    public String getTitle()
    {
        return title;
    }
}
