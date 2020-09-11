/*******************************************************************************
 * Copyright (c) 2008, 2019 SWTChart project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * yoshitaka - initial API and implementation
 * Christoph Läubrich - add support for datamodel
 * Frank Buloup - Internationalization
 *******************************************************************************/
package org.eclipse.swtchart;

import org.eclipse.swt.graphics.Color;

/**
 * Line series.
 */
public interface ILineSeries<T> extends ISeries<T> {

	/**
	 * A plot symbol type.
	 */
	public enum PlotSymbolType {
		/** none */
		NONE("None"), //$NON-NLS-1$
		/** circle */
		CIRCLE("Circle"), //$NON-NLS-1$
		/** square */
		SQUARE("Square"), //$NON-NLS-1$
		/** diamond */
		DIAMOND("Diamond"), //$NON-NLS-1$
		/** triangle */
		TRIANGLE("Triangle"), //$NON-NLS-1$
		/** inverted triangle */
		INVERTED_TRIANGLE("Inverted Triangle"), //$NON-NLS-1$
		/** cross */
		CROSS("Cross"), //$NON-NLS-1$
		/** plus */
		PLUS("Plus"), //$NON-NLS-1$
		/** emoji */
		EMOJI("Emoji"); //$NON-NLS-1$

		/** the label for plot symbol */
		public final String label;

		/**
		 * Constructor.
		 *
		 * @param label
		 *            plot symbol label
		 */
		private PlotSymbolType(String label) {
			this.label = label;
		}
	}

	/**
	 * Gets the extended symbol type
	 * 
	 * @return the symbol type (a character, an emoticon or an emoji)
	 */
	default String getExtendedPlotSymbolType() {

		return "😂";
	}

	/**
	 * Sets the extended symbol tyoe
	 * 
	 * @param type
	 *            the type to set
	 */
	void setExtendedPlotSymbolType(String type);

	/**
	 * Gets the symbol type.
	 *
	 * @return the symbol type
	 */
	PlotSymbolType getSymbolType();

	/**
	 * Sets the symbol type. If null is given, default type
	 * <tt>PlotSymbolType.CIRCLE</tt> will be set.
	 *
	 * @param type
	 *            the symbol type
	 */
	void setSymbolType(PlotSymbolType type);

	/**
	 * Gets the symbol size in pixels.
	 *
	 * @return the symbol size
	 */
	int getSymbolSize();

	/**
	 * Sets the symbol size in pixels. The default size is 4.
	 *
	 * @param size
	 *            the symbol size
	 */
	void setSymbolSize(int size);

	/**
	 * Gets the symbol color.
	 *
	 * @return the symbol color
	 */
	Color getSymbolColor();

	/**
	 * Sets the symbol color. If null is given, default color will be set.
	 *
	 * @param color
	 *            the symbol color
	 */
	void setSymbolColor(Color color);

	/**
	 * Gets the symbol colors.
	 *
	 * @return the symbol colors, or empty array if no symbol colors are set.
	 */
	Color[] getSymbolColors();

	/**
	 * Sets the symbol colors. Typically, the number of symbol colors is the same as the
	 * number of plots. If the number of symbol colors is less than the number of plots,
	 * the rest of plots will have the common color which is set with
	 * <tt>setSymbolColor(Color)</tt>.
	 *
	 * @param colors
	 *            the symbol colors. If <tt>null</tt> or empty array is given, the color
	 *            which is set with <tt>setSymbolColor(Color)</tt> will be commonly used
	 *            for all plots.
	 */
	void setSymbolColors(Color[] colors);

	/**
	 * Gets line style.
	 *
	 * @return line style.
	 */
	LineStyle getLineStyle();

	/**
	 * Sets line style. If null is given, default line style will be set.
	 *
	 * @param style
	 *            line style
	 */
	void setLineStyle(LineStyle style);

	/**
	 * Gets the line color.
	 *
	 * @return the line color
	 */
	Color getLineColor();

	/**
	 * Sets line color. If null is given, default color will be set.
	 *
	 * @param color
	 *            the line color
	 */
	void setLineColor(Color color);

	/**
	 * Gets the line width.
	 *
	 * @return the line width
	 */
	int getLineWidth();

	/**
	 * Sets the width of line connecting data points and also line drawing
	 * symbol if applicable (i.e. <tt>PlotSymbolType.CROSS</tt> or
	 * <tt>PlotSymbolType.PLUS</tt>). The default width is 1.
	 *
	 * @param width
	 *            the line width
	 */
	void setLineWidth(int width);

	/**
	 * Enables the area chart.
	 *
	 * @param enabled
	 *            true if enabling area chart
	 */
	void enableArea(boolean enabled);

	/**
	 * Gets the state indicating if area chart is enabled.
	 *
	 * @return true if area chart is enabled
	 */
	boolean isAreaEnabled();

	/**
	 * Enables the step chart.
	 *
	 * @param enabled
	 *            true if enabling step chart
	 */
	void enableStep(boolean enabled);

	/**
	 * Gets the state indicating if step chart is enabled.
	 *
	 * @return true if step chart is enabled
	 */
	boolean isStepEnabled();

	/**
	 * Gets the anti-aliasing value for drawing line. The default value is
	 * <tt>SWT.DEFAULT<tt>.
	 *
	 * @return the anti-aliasing value which can be <tt>SWT.DEFAULT</tt>,
	 *         <tt>SWT.ON</tt> or <tt>SWT.OFF</tt>.
	 */
	int getAntialias();

	/**
	 * Sets the anti-aliasing value for drawing line.
	 * <p>
	 * If number of data points is too large, the series is drawn as a
	 * collection of dots rather than lines. In this case, the anti-alias
	 * doesn't really make effect, and just causes performance degradation.
	 * Therefore, client code may automatically enable/disable the anti-alias
	 * for each series depending on the number of data points, or alternatively
	 * may let end-user configure it.
	 *
	 * @param antialias
	 *            the anti-aliasing value which can be <tt>SWT.DEFAULT</tt>,
	 *            <tt>SWT.ON</tt> or <tt>SWT.OFF</tt>.
	 */
	void setAntialias(int antialias);
}
