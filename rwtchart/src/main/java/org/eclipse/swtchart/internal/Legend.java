/*******************************************************************************
 * Copyright (c) 2008, 2020 SWTChart project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * yoshitaka - initial API and implementation
 * Frank Buloup - Internationalization
 *******************************************************************************/
package org.eclipse.swtchart.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.Constants;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ICircularSeries;
import org.eclipse.swtchart.ILegend;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.internal.series.LineSeries;
import org.eclipse.swtchart.internal.series.Series;

/**
 * A legend for chart.
 */
public class Legend extends Canvas implements ILegend, PaintListener
{
   private static final String HEADER_ID = "$$header$$";
   private static final String VALUE_PLACEHOLDER = "000.000 M";

	/** the plot chart */
	private Chart chart;
	/** the state indicating the legend visibility */
	private boolean visible;
	/** the position of legend */
	private int position;
	/** the margin */
	private static final int MARGIN = 5;
   /** the margin for extended info columns */
   private static final int EXT_COL_MARGIN = 7;
	/** the width of area to draw symbol */
	private static final int SYMBOL_WIDTH = 20;
	/** the line width */
	private static final int LINE_WIDTH = 2;
	/** the default foreground */
	private static final Color DEFAULT_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	/** the default background */
	private static final Color DEFAULT_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	/** the default font */
	private Font defaultFont;
	/** the default font size */
	private static final int DEFAULT_FONT_SIZE = Constants.SMALL_FONT_SIZE;
	/** the default position */
	private static final int DEFAULT_POSITION = SWT.BOTTOM;
	/** the map between series id and cell bounds */
	private Map<String, Rectangle> cellBounds;
   /** offset for drawing extended info */
   private int extendedInfoOffset = 0;
   /** extended legend flag */
   private boolean extended = true;
   /** font used for column headers */
   private Font headerFont = null;

	/**
	 * Constructor.
	 *
	 * @param chart
	 *            the chart
	 * @param style
	 *            the style
	 */
	public Legend(Chart chart, int style) {

		super(chart, style | SWT.DOUBLE_BUFFERED);
		this.chart = chart;
		visible = true;
		position = DEFAULT_POSITION;
		cellBounds = new HashMap<String, Rectangle>();
		defaultFont = new Font(Display.getDefault(), "Tahoma", DEFAULT_FONT_SIZE, SWT.NORMAL); //$NON-NLS-1$
		setFont(defaultFont);
		setForeground(DEFAULT_FOREGROUND);
		setBackground(DEFAULT_BACKGROUND);
		addPaintListener(this);
	}

	@Override
	public void setVisible(boolean visible) {

		if(this.visible == visible) {
			return;
		}
		this.visible = visible;
		chart.updateLayout();
	}

	@Override
	public boolean isVisible() {

		return visible;
	}

   /**
    * @see org.eclipse.swtchart.ILegend#isExtended()
    */
   @Override
   public boolean isExtended()
   {
      return extended;
   }

   /**
    * @see org.eclipse.swtchart.ILegend#setExtended(boolean)
    */
   @Override
   public void setExtended(boolean extended)
   {
      this.extended = extended;
   }

   /**
    * Update header font
    */
   private void updateHeaderFont()
   {
      if (headerFont != null)
         headerFont.dispose();
      
      FontData fd = getFont().getFontData()[0];
      fd.setStyle(SWT.BOLD);
      headerFont = new Font(getDisplay(), fd);
   }
   
	@Override
	public void setFont(Font font) {

		if(font == null) {
			super.setFont(defaultFont);
		} else {
			super.setFont(font);
		}
      updateHeaderFont();
		chart.updateLayout();
	}

	@Override
	public void setForeground(Color color) {

		if(color == null) {
			super.setForeground(DEFAULT_FOREGROUND);
		} else {
			super.setForeground(color);
		}
	}

	@Override
	public void setBackground(Color color) {

		if(color == null) {
			super.setBackground(DEFAULT_BACKGROUND);
		} else {
			super.setBackground(color);
		}
	}

	@Override
	public int getPosition() {

		return position;
	}

	@Override
	public void setPosition(int value) {

		if(value == SWT.LEFT || value == SWT.RIGHT || value == SWT.TOP || value == SWT.BOTTOM) {
			position = value;
		} else {
			position = DEFAULT_POSITION;
		}
		chart.updateLayout();
	}

	@Override
	public Rectangle getBounds(String seriesId) {

		if(seriesId == null) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return cellBounds.get(seriesId.trim());
	}

	@Override
	public void dispose() {

		super.dispose();
		if(!defaultFont.isDisposed()) {
			defaultFont.dispose();
		}
	}

	/**
	 * Sorts the given series array. For instance, if there are two stack series
	 * in horizontal orientation, the top of stack series should appear at top
	 * of legend.
	 * <p>
	 * If there are multiple x axes, the given series array will be sorted with
	 * x axis first. And then, the series in each x axis will be sorted with
	 * {@link Legend#sort(List, boolean, boolean)}.
	 *
	 * @param seriesArray
	 *            the series array
	 * @return the sorted series array
	 */
	private ISeries<?>[] sort(ISeries<?>[] seriesArray) {

		// create a map between axis id and series list
		Map<Integer, List<ISeries<?>>> map = new HashMap<Integer, List<ISeries<?>>>();
		for(ISeries<?> series : seriesArray) {
			int axisId = series.getXAxisId();
			List<ISeries<?>> list = map.get(axisId);
			if(list == null) {
				list = new ArrayList<ISeries<?>>();
			}
			list.add(series);
			map.put(axisId, list);
		}
		// sort an each series list
		List<ISeries<?>> sortedArray = new ArrayList<ISeries<?>>();
		boolean isVertical = chart.getOrientation() == SWT.VERTICAL;
		for(Entry<Integer, List<ISeries<?>>> entry : map.entrySet()) {
			boolean isCategoryEnabled = chart.getAxisSet().getXAxis(entry.getKey()).isCategoryEnabled();
			sortedArray.addAll(sort(entry.getValue(), isCategoryEnabled, isVertical));
		}
		return sortedArray.toArray(new ISeries[sortedArray.size()]);
	}

	/**
	 * Sorts the given series list which belongs to a certain x axis.
	 * <ul>
	 * <li>The stacked series will be gathered, and the order of stack series
	 * will be reversed.</li>
	 * <li>In the case of vertical orientation, the order of whole series will
	 * be reversed.</li>
	 * </ul>
	 *
	 * @param seriesList
	 *            the series list which belongs to a certain x axis
	 * @param isCategoryEnabled
	 *            true if category is enabled
	 * @param isVertical
	 *            true in the case of vertical orientation
	 * @return the sorted series array
	 */
	private static List<ISeries<?>> sort(List<ISeries<?>> seriesList, boolean isCategoryEnabled, boolean isVertical) {

		List<ISeries<?>> sortedArray = new ArrayList<ISeries<?>>();
		// gather the stacked series reversing the order of stack series
		int insertIndex = -1;
		for(int i = 0; i < seriesList.size(); i++) {
			if(isCategoryEnabled && ((Series<?>)seriesList.get(i)).isValidStackSeries()) {
				if(insertIndex == -1) {
					insertIndex = i;
				} else {
					sortedArray.add(insertIndex, seriesList.get(i));
					continue;
				}
			}
			sortedArray.add(seriesList.get(i));
		}
		// reverse the order of whole series in the case of vertical orientation
		if(isVertical) {
			Collections.reverse(sortedArray);
		}
		return sortedArray;
	}

	/**
	 * Update the layout data.
	 */
	public void updateLayoutData()
	{
		if (!visible)
		{
			setLayoutData(new ChartLayoutData(0, 0));
			return;
		}
		
      extendedInfoOffset = 0;

		int width = 0;
		int height = 0;
		
		ISeries<?>[] seriesArray = sort(chart.getSeriesSet().getSeries());
		Rectangle r = chart.getClientArea();
		Rectangle titleBounds = ((Title)chart.getTitle()).getBounds();
		int titleHeight = titleBounds.y + titleBounds.height;
		int cellHeight = Util.getExtentInGC(getFont(), null).y;
      final int cellExtraWidth = extended ? (Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x + EXT_COL_MARGIN) * 4 : 0;

		if(position == SWT.RIGHT || position == SWT.LEFT) {
			int columns = 1;
         int yPosition = extended ? (cellHeight + MARGIN * 2) : MARGIN;
			int maxCellWidth = 0;
			for(ISeries<?> series : seriesArray) {
				if(!series.isVisibleInLegend()) {
					continue;
				}
				if (series instanceof ICircularSeries)
				{
					if(((ICircularSeries<?>)series).getLabels() != null) {
						String[] labels = ((ICircularSeries<?>)series).getLabels();
						for(int i = 0; i != labels.length; i++) {
							int textWidth = Util.getExtentInGC(getFont(), labels[i]).x;
							int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
							maxCellWidth = Math.max(maxCellWidth, cellWidth);
							if(yPosition + cellHeight < r.height - titleHeight - MARGIN || yPosition == MARGIN) {
								yPosition += cellHeight + MARGIN;
							} else {
								columns++;
								yPosition = cellHeight + MARGIN * 2;
							}
							cellBounds.put(labels[i], new Rectangle(maxCellWidth * (columns - 1), yPosition - cellHeight - MARGIN, cellWidth, cellHeight));
							height = Math.max(yPosition, height);
						}
						width = maxCellWidth * columns;
						continue;
					}
				}

				String label = getLegendLabel(series);
				int textWidth = Util.getExtentInGC(getFont(), label).x;
				int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
				maxCellWidth = Math.max(maxCellWidth, cellWidth);
            if (extendedInfoOffset < cellWidth + EXT_COL_MARGIN)
            {
               extendedInfoOffset = cellWidth + EXT_COL_MARGIN;
            }
				if(yPosition + cellHeight < r.height - titleHeight - MARGIN || yPosition == MARGIN) {
					yPosition += cellHeight + MARGIN;
				} else {
					columns++;
					yPosition = cellHeight + MARGIN * 2;
				}
				cellBounds.put(series.getId(), new Rectangle(maxCellWidth * (columns - 1), yPosition - cellHeight - MARGIN, cellWidth, cellHeight));
				height = Math.max(yPosition, height);
			}
			width = maxCellWidth * columns;
		} else if(position == SWT.TOP || position == SWT.BOTTOM) {
			int rows = 1;
			int xPosition = 0;
			for(ISeries<?> series : seriesArray) {
				if(!series.isVisibleInLegend()) {
					continue;
				}
				if(series instanceof ICircularSeries)
				{
					if(((ICircularSeries<?>)series).getLabels() != null) {
						String[] labels = ((ICircularSeries<?>)series).getLabels();
						for(int i = 0; i != labels.length; i++) {
							int textWidth = Util.getExtentInGC(getFont(), labels[i]).x;
							int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
							if(xPosition + cellWidth < r.width || xPosition == 0) {
								xPosition += cellWidth;
							} else {
								rows++;
								xPosition = cellWidth;
							}
							cellBounds.put(labels[i], new Rectangle(xPosition - cellWidth, (cellHeight + MARGIN) * (rows - 1) + MARGIN, cellWidth, cellHeight));
							width = Math.max(xPosition, width);
						}
						height = (cellHeight + MARGIN) * rows + MARGIN;
						continue;
					}
				}
				String label = getLegendLabel(series);
				int textWidth = Util.getExtentInGC(getFont(), label).x;
				int cellWidth = textWidth + SYMBOL_WIDTH + MARGIN * 3;
				if (!extended && (xPosition + cellWidth < r.width || xPosition == 0))
				{
					xPosition += cellWidth;
				} 
				else
				{
					rows++;
					xPosition = cellWidth;
				}
				cellBounds.put(series.getId(), new Rectangle(xPosition - cellWidth, (cellHeight + MARGIN) * (rows - 1) + MARGIN, cellWidth, cellHeight));
				width = Math.max(xPosition, width);
            if (extendedInfoOffset < cellWidth + EXT_COL_MARGIN)
            {
               extendedInfoOffset = cellWidth + EXT_COL_MARGIN;
            }
			}
			height = (cellHeight + MARGIN) * rows + MARGIN;
		}

      if (extended)
      {
         width += cellExtraWidth;
         height += cellHeight + MARGIN; 
         cellBounds.put(HEADER_ID, new Rectangle(0, MARGIN, cellExtraWidth, cellHeight));
         
         // Update all cells because right border could be set incorrectly if
         // extendedInfoOffset was updated multiple times during calculation
         for(Entry<String, Rectangle> e : cellBounds.entrySet())
         {
            if (e.getKey().equals(HEADER_ID))
               continue;
            e.getValue().width = width;
         }
      }
      
      setLayoutData(new ChartLayoutData(width, height));
	}

	/**
	 * Gets the legend label.
	 * 
	 * @param series
	 *            the series
	 * @return the legend label
	 */
	private static String getLegendLabel(ISeries<?> series) {

		String description = series.getDescription();
		if(description == null) {
			return series.getId();
		}
		return description;
	}

	/**
	 * Draws the symbol of series.
	 *
	 * @param gc
	 *            the graphics context
	 * @param series
	 *            the series
	 * @param r
	 *            the rectangle to draw the symbol of series
	 */
	protected void drawSymbol(GC gc, Series<?> series, Rectangle r) {

		if(!visible) {
			return;
		}
		if(series instanceof ILineSeries) {
			// draw plot line
			gc.setForeground(((ILineSeries<?>)series).getLineColor());
			gc.setLineWidth(LINE_WIDTH);
			int lineStyle = Util.getIndexDefinedInSWT(((ILineSeries<?>)series).getLineStyle());
			int x = r.x;
			int y = r.y + r.height / 2;
			if(lineStyle != SWT.NONE)
			{
				// FIXME: gc.setLineStyle(lineStyle);
				gc.drawLine(x, y, x + SYMBOL_WIDTH, y);
			}
			// draw series symbol
			Color color = ((ILineSeries<?>)series).getSymbolColor();
			Color[] colors = ((ILineSeries<?>)series).getSymbolColors();
			if(colors != null && colors.length > 0) {
				color = colors[0];
			}
			((LineSeries<?>)series).drawSeriesSymbol(gc, x + SYMBOL_WIDTH / 2, y, color);
		} else if(series instanceof IBarSeries) {
			// draw riser
			gc.setBackground(((IBarSeries<?>)series).getBarColor());
			int size = SYMBOL_WIDTH / 2;
			int x = r.x + size / 2;
			int y = (int)(r.y - size / 2d + r.height / 2d);
			gc.fillRectangle(x, y, size, size);
		}
	}

   /**
    * Draw extended info (min, max, average value)
    * 
    * @param gc
    * @param series
    * @param r
    */
   private void drawExtendedInfo(GC gc, Series<?> series, Rectangle r)
   {
      int shift = Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x + EXT_COL_MARGIN;
      int x = r.x + extendedInfoOffset + MARGIN * 2;
      
      gc.drawText(chart.isUseMultipliers() ? chart.roundDecimalValue(series.getCurY(), 0.005, 3) : Double.toString(series.getCurY()), x, r.y, true);
      x += shift;

      gc.drawText(chart.isUseMultipliers() ? chart.roundDecimalValue(series.getMinY(), 0.005, 3) : Double.toString(series.getMinY()), x, r.y, true);
      x += shift;

      gc.drawText(chart.isUseMultipliers() ? chart.roundDecimalValue(series.getAvgY(), 0.005, 3) : Double.toString(series.getAvgY()), x, r.y, true);
      x += shift;

      gc.drawText(chart.isUseMultipliers() ? chart.roundDecimalValue(series.getMaxY(), 0.005, 3) : Double.toString(series.getMaxY()), x, r.y, true);
   }

	/**
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	public void paintControl(PaintEvent e) {

		if(!visible) {
			return;
		}
		GC gc = e.gc;
		gc.setFont(getFont());
		ISeries<?>[] seriesArray = chart.getSeriesSet().getSeries();
		if(seriesArray.length == 0) {
			return;
		}
		// draw frame
		gc.fillRectangle(0, 0, getSize().x - 1, getSize().y - 1);
		// FIXME: gc.setLineStyle(SWT.LINE_SOLID);
		gc.setLineWidth(1);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		gc.drawRectangle(0, 0, getSize().x - 1, getSize().y - 1);

      // Draw column headers
      if (extended)
      {
         gc.setBackground(getBackground());
         gc.setForeground(getForeground());
         gc.setFont(headerFont);
         
         final int shift = Util.getExtentInGC(getFont(), VALUE_PLACEHOLDER).x + EXT_COL_MARGIN;
         
         Rectangle r = cellBounds.get(HEADER_ID);
         int x = r.x + extendedInfoOffset + MARGIN * 2;

         gc.drawText("Curr", x, r.y, true);
         x += shift;

         gc.drawText("Min", x, r.y, true);
         x += shift;

         gc.drawText("Avg", x, r.y, true);
         x += shift;

         gc.drawText("Max", x, r.y, true);
         
         gc.setFont(getFont());
      }

		// draw content
		for(int i = 0; i < seriesArray.length; i++)
		{
			if (!seriesArray[i].isVisibleInLegend())
				continue;

			if (seriesArray[i] instanceof ICircularSeries)
			{
				ICircularSeries<?> pieSeries = (ICircularSeries<?>)seriesArray[i];
				String[] labels = pieSeries.getLabels();
				Color[] color = pieSeries.getColors();
				for(int j = 0; j != labels.length; j++) {
					Rectangle r = cellBounds.get(labels[j]);
					if(r != null) {
						String labelPie = labels[j];
						Color colorPie = color[j];
						if(labelPie != null && colorPie != null) {
							drawPieSymbol(gc, labelPie, colorPie, new Rectangle(r.x + MARGIN, r.y + MARGIN, SYMBOL_WIDTH, r.height - MARGIN * 2));
							gc.setBackground(getBackground());
							gc.setForeground(getForeground());
							gc.drawText(labelPie, r.x + SYMBOL_WIDTH + MARGIN * 2, r.y, true);
						}
					}
				}
			}
			else
			{
				// draw plot line, symbol etc
				String id = seriesArray[i].getId();
				Rectangle r = cellBounds.get(id);
				drawSymbol(gc, (Series<?>)seriesArray[i], new Rectangle(r.x + MARGIN, r.y + MARGIN, SYMBOL_WIDTH, r.height - MARGIN * 2));
				// draw label
				String label = getLegendLabel(seriesArray[i]);
				gc.setBackground(getBackground());
				gc.setForeground(getForeground());
				gc.drawText(label, r.x + SYMBOL_WIDTH + MARGIN * 2, r.y, true);

	         if (extended)
	         {
	            drawExtendedInfo(gc, (Series<?>)seriesArray[i], r);
	         }
			}
		}
	}

	private void drawPieSymbol(GC gc, String string, Color color, Rectangle r) {

		gc.setBackground(color);
		int size = SYMBOL_WIDTH / 2;
		int x = r.x + size / 2;
		int y = (int)(r.y - size / 2d + r.height / 2d);
		gc.fillArc(x, y, size, size, 0, 360);
	}
}
