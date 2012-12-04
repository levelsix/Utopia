package com.lvl6.ui.admin.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.googlecode.wicketcharts.highcharts.options.Options;
import com.googlecode.wicketcharts.highcharts.options.SeriesOptions;
import com.googlecode.wicketcharts.highcharts.options.SimpleSeriesOptions;
import com.googlecode.wicketcharts.highcharts.options.Title;
import com.googlecode.wicketcharts.highcharts.options.XAxis;
import com.googlecode.wicketcharts.highcharts.options.YAxis;
import com.lvl6.cassandra.RollupEntry;

public class TimeSeriesLineChartOptions extends Options {

	private static final long serialVersionUID = 1L;

	public TimeSeriesLineChartOptions(String title, String yAxisTitle, List<RollupEntry> entries) {
		super();
		List<String> times = new ArrayList<String>();
		List<Number> values = new ArrayList<Number>();
		for(RollupEntry ent : entries) {
			times.add(ent.getColumnDisplayName());
			values.add(ent.getValue());
		}
		setTitle(new Title(title));
		XAxis xAxis = new XAxis();
		xAxis.setTitle(new Title("Time"));
		xAxis.setCategories(times);
		YAxis yAxis = new YAxis();
		yAxis.setTitle(new Title(yAxisTitle));
		setxAxis(xAxis);
		setyAxis(yAxis);
		SeriesOptions<Number> series = new SimpleSeriesOptions();
		series.setData(values);
		setSeries(Arrays.asList(series));
	}
	
	

}
