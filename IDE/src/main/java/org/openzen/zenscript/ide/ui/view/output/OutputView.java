/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

import java.util.ArrayList;
import java.util.List;

import listeners.ListenerHandle;
import live.LiveList;
import live.LiveObject;
import live.MutableLiveObject;

import org.openzen.drawablegui.DComponent;
import org.openzen.drawablegui.DComponentContext;
import org.openzen.drawablegui.DFontMetrics;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DSizing;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.Destructible;
import org.openzen.drawablegui.draw.DDrawnShape;
import org.openzen.drawablegui.draw.DDrawnText;
import org.openzen.drawablegui.style.DStyleClass;

/**
 * @author Hoofdgebruiker
 */
public class OutputView implements DComponent {
	private final MutableLiveObject<DSizing> sizing = DSizing.create();
	private final DStyleClass styleClass;
	private final LiveList<OutputLine> lines;
	private final List<List<DDrawnText>> outputText = new ArrayList<>();
	private DComponentContext context;
	private DIRectangle bounds;
	private OutputViewStyle style;
	private DFontMetrics fontMetrics;
	private ListenerHandle<LiveList.Listener<OutputLine>> linesListener;
	private DDrawnShape shape;

	public OutputView(DStyleClass styleClass, LiveList<OutputLine> lines) {
		this.styleClass = styleClass;
		this.lines = lines;
	}

	@Override
	public void mount(DComponentContext parent) {
		if (context != null)
			unmount();

		context = parent.getChildContext("outputview", styleClass);
		style = context.getStyle(OutputViewStyle::new);
		fontMetrics = context.getFontMetrics(style.font);

		for (OutputLine line : lines)
			outputText.add(draw(line));

		linesListener = lines.addListener(new LinesListener());
		updateSizing();
	}

	@Override
	public void unmount() {
		context = null;

		if (shape != null) {
			shape.close();
			shape = null;
		}

		for (List<DDrawnText> line : outputText)
			Destructible.close(line);

		outputText.clear();
		linesListener.close();
	}

	@Override
	public LiveObject<DSizing> getSizing() {
		return sizing;
	}

	@Override
	public DIRectangle getBounds() {
		return bounds;
	}

	@Override
	public void setBounds(DIRectangle bounds) {
		this.bounds = bounds;

		DIRectangle available = style.margin.apply(bounds);
		shape = context.shadowPath(0, style.shape.instance(available), DTransform2D.IDENTITY, style.backgroundColor, style.shadow);

		layout(0);
	}

	@Override
	public int getBaselineY() {
		return -1;
	}

	@Override
	public void close() {
		unmount();
	}

	private void updateSizing() {
		sizing.setValue(new DSizing(100, lines.getLength() * fontMetrics.getLineHeight() + style.margin.getVertical() + style.border.getPaddingVertical()));
	}

	private List<DDrawnText> draw(OutputLine line) {
		List<DDrawnText> lineText = new ArrayList<>();
		for (OutputSpan span : line.spans)
			lineText.add(context.drawText(1, style.font, span.getColor(), 0, 0, span.getText()));
		return lineText;
	}

	private void layout(int fromIndex) {
		for (int i = fromIndex; i < outputText.size(); i++)
			layout(i, outputText.get(i));
	}

	private void layout(int index, List<DDrawnText> line) {
		int x = bounds.x + style.margin.left + style.border.getPaddingTop();
		int y = bounds.y + style.margin.top + style.border.getPaddingTop() + index * fontMetrics.getLineHeight() + fontMetrics.getAscent();
		for (DDrawnText text : line) {
			text.setPosition(x, y);
			x += text.getBounds().width;
		}
	}

	private class LinesListener implements LiveList.Listener<OutputLine> {
		@Override
		public void onInserted(int index, OutputLine value) {
			outputText.add(index, draw(value));
			layout(index);
			updateSizing();
		}

		@Override
		public void onChanged(int index, OutputLine oldValue, OutputLine newValue) {
			List<DDrawnText> line = draw(newValue);
			layout(index, line);
			Destructible.close(outputText.set(index, line));
		}

		@Override
		public void onRemoved(int index, OutputLine oldValue) {
			Destructible.close(outputText.remove(index));
			updateSizing();
		}
	}
}
