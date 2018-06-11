/*
 * Copyright 2016 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.rx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.diffplug.common.collect.ImmutableSet;
import com.diffplug.common.collect.Immutables;
import io.reactivex.Observable;

@SuppressWarnings("serial")
public class RxExample extends JFrame {

	public static void main(String[] args) {
		JFrame frame = new JFrame("DurianRx Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());

		// show instructions
		JLabel instructions = new JLabel("Click and Ctrl+click to manipulate the selection");
		frame.add(instructions, BorderLayout.NORTH);

		// show the Rx example
		frame.add(new RxGrid(), BorderLayout.CENTER);
		frame.setSize(CELL_SIZE * (NUM_CELLS + 1), CELL_SIZE * (NUM_CELLS + 2));
		frame.setVisible(true);
	}

	private static final int NUM_CELLS = 5;
	private static final int CELL_SIZE = 50;

	static class RxGrid extends JComponent {
		/** The cell which the mouse is over. */
		private RxGetter<Optional<Integer>> rxMouseOver;
		/** The selected cells. */
		private RxBox<ImmutableSet<Integer>> rxSelection;

		RxGrid() {
			// maintain the position of the mouse
			RxBox<Point> mousePosition = RxBox.of(new Point(0, 0));
			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					mousePosition.set(e.getPoint());
				}
			});

			// maintain the position of the mouse in model terms
			rxMouseOver = mousePosition.map(p -> {
				int x = p.x / CELL_SIZE;
				int y = p.y / CELL_SIZE;
				if (x < NUM_CELLS && y < NUM_CELLS) {
					return Optional.of(x + y * NUM_CELLS);
				} else {
					return Optional.empty();
				}
			});

			// maintain the selection state
			rxSelection = RxBox.of(ImmutableSet.of());
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					rxMouseOver.get().ifPresent(cell -> {
						rxSelection.modify(Immutables.mutatorSet(selection -> {
							if (e.isControlDown()) {
								// control => toggle mouseOver item in selection
								if (selection.contains(cell)) {
									selection.remove(cell);
								} else {
									selection.add(cell);
								}
							} else {
								// no control => set selection to mouseOver
								selection.clear();
								selection.add(cell);
							}
						}));
					});
				}
			});

			// trigger a repaint on any change
			Rx.subscribe(Observable.merge(rxMouseOver.asObservable(), rxSelection.asObservable()), anyChange -> {
				this.repaint();
			});
		}

		/** Paint a NUM_CELLS by NUM_CELLS grid. */
		@Override
		public void paint(Graphics g) {
			int mouseOver = rxMouseOver.get().orElse(-1);
			Set<Integer> selection = rxSelection.get();
			for (int i = 0; i < NUM_CELLS * NUM_CELLS; ++i) {
				int x = i % NUM_CELLS;
				int y = i / NUM_CELLS;

				boolean isSelected = selection.contains(i);
				boolean isMouseOver = mouseOver == i;

				// set the background color based on the selection status
				Color color;
				if (isSelected) {
					color = isMouseOver ? Color.CYAN : Color.CYAN.darker();
				} else {
					color = isMouseOver ? Color.WHITE : Color.WHITE.darker();
				}
				// fill the rectangle and draw its border
				g.setColor(color);
				g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				g.setColor(Color.BLACK);
				g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}
	}
}
