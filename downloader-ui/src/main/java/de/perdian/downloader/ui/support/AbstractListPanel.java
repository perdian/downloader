/*
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.downloader.ui.support;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.perdian.apps.downloader.core.DownloadJob;

/**
 * Base class for all panels displaying a list of children
 *
 * @author Christian Robert
 */

public abstract class AbstractListPanel<T, C extends JComponent> extends JPanel {

  static final long serialVersionUID = 1L;

  private Map<T, C> myItemPanelMap = new HashMap<>();
  private JPanel myListWrapperPanel = null;

  public AbstractListPanel() {
    this.setLayout(new BorderLayout());
    this.updateContent(new JLabel(this.createEmptyMessage(), SwingConstants.CENTER));
  }

  /**
   * Creates a message saying "this panel doesn't contain any children" or
   * something alike
   */
  protected abstract String createEmptyMessage();

  /**
   * Creates the detailed item for the given panel
   */
  protected abstract C createItemPanel(T item);

  public void insertItem(T item) {

    final C itemPanel = this.createItemPanel(item);

    synchronized(this) {

      this.getItemPanelMap().put(item, itemPanel);

      // Make sure the wrapper panel is visible
      if(this.getListWrapperPanel() == null) {

        JPanel dummyPanel = new JPanel();
        GridBagConstraints dummyConstraints = new GridBagConstraints();
        dummyConstraints.gridwidth = GridBagConstraints.REMAINDER;
        dummyConstraints.gridheight = GridBagConstraints.REMAINDER;
        dummyConstraints.fill = GridBagConstraints.VERTICAL;
        dummyConstraints.weighty = 1d;
        JPanel listWrapperPanel = new JPanel(new GridBagLayout());
        listWrapperPanel.add(dummyPanel, dummyConstraints);
        this.setListWrapperPanel(listWrapperPanel);

        JScrollPane listWrapperScroller = new JScrollPane(listWrapperPanel);
        listWrapperScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        listWrapperScroller.setBorder(null);
        listWrapperScroller.getVerticalScrollBar().setUnitIncrement(20);
        this.updateContent(listWrapperScroller);

      }

      // Now add the new job to the wrapper panel
      SwingUtilities.invokeLater(new Runnable() {
        @Override public void run() {
          synchronized(AbstractListPanel.this) {

            GridBagConstraints itemPanelConstraints = new GridBagConstraints();
            itemPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            itemPanelConstraints.gridwidth = GridBagConstraints.REMAINDER;
            itemPanelConstraints.weightx = 1d;

            JPanel listWrapperPanel = AbstractListPanel.this.getListWrapperPanel();
            listWrapperPanel.add(itemPanel, itemPanelConstraints, listWrapperPanel.getComponentCount() - 1);
            listWrapperPanel.revalidate();
            listWrapperPanel.repaint();

          }
        }
      });

    }

  }

  public void removeItem(DownloadJob job) {
    synchronized(this) {
      final C itemPanel = this.getItemPanelMap().remove(job);
      if(this.getItemPanelMap().isEmpty()) {
        this.updateContent(new JLabel(this.createEmptyMessage(), SwingConstants.CENTER));
        this.setListWrapperPanel(null);
      } else if(itemPanel != null) {
        final JPanel listWrapperPanel = this.getListWrapperPanel();
        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run() {
            synchronized(AbstractListPanel.this) {
              listWrapperPanel.remove(itemPanel);
              listWrapperPanel.revalidate();
              listWrapperPanel.repaint();
            }
          }
        });
      }
    }
  }

  // ---------------------------------------------------------------------------
  // --- Helper methods --------------------------------------------------------
  // ---------------------------------------------------------------------------

  void updateContent(final JComponent component) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run() {
        AbstractListPanel.this.removeAll();
        AbstractListPanel.this.add(component, BorderLayout.CENTER);
        AbstractListPanel.this.revalidate();
        AbstractListPanel.this.repaint();
      }
    });
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  Map<T, C> getItemPanelMap() {
    return this.myItemPanelMap;
  }
  void setItemPanelMap(Map<T, C> itemPanelMap) {
    this.myItemPanelMap = itemPanelMap;
  }

  JPanel getListWrapperPanel() {
    return this.myListWrapperPanel;
  }
  void setListWrapperPanel(JPanel listWrapperPanel) {
    this.myListWrapperPanel = listWrapperPanel;
  }

}